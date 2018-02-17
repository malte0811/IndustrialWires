package malte0811.industrialWires.blocks.converter;

import blusunrize.immersiveengineering.api.ApiUtils;
import com.google.common.collect.ImmutableSet;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.ISyncReceiver;
import malte0811.industrialWires.blocks.TileEntityIWMultiblock;
import malte0811.industrialWires.converter.IMBPartElectric;
import malte0811.industrialWires.converter.IMBPartElectric.Waveform;
import malte0811.industrialWires.converter.MechEnergy;
import malte0811.industrialWires.converter.MechMBPart;
import malte0811.industrialWires.network.MessageTileSyncIW;
import malte0811.industrialWires.util.LocalSidedWorld;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static blusunrize.immersiveengineering.common.IEContent.blockMetalDecoration0;
import static blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0.LIGHT_ENGINEERING;
import static malte0811.industrialWires.converter.IMBPartElectric.Waveform.*;
import static malte0811.industrialWires.util.MiscUtils.getOffset;
import static malte0811.industrialWires.util.MiscUtils.offset;
import static malte0811.industrialWires.util.NBTKeys.PARTS;
import static malte0811.industrialWires.util.NBTKeys.SPEED;

public class TileEntityMultiblockConverter extends TileEntityIWMultiblock implements ITickable, ISyncReceiver {
	private static final double DECAY_BASE = Math.exp(Math.log(.5)/(2*60*60*20));
	public static final double TICK_ANGLE_PER_SPEED = 180/20/Math.PI;
	private static final double SYNC_THRESHOLD = .95;
	public MechMBPart[] mechanical = null;
	public int[] offsets = null;

	private int[][] electricalStartEnd = null;

	public MechEnergy energyState;
	private double lastSyncedSpeed = 0;
	private double decay;
	public double angle;
	@SideOnly(Side.CLIENT)
	public List<BakedQuad> rotatingModel;
	private boolean shouldInitWorld;

	@Override
	public void update() {
		ApiUtils.checkForNeedlessTicking(this);
		if (world.isRemote&&mechanical!=null) {
			angle += energyState.getSpeed()*TICK_ANGLE_PER_SPEED;
			angle %= 360;
		}
		if (world.isRemote||isLogicDummy()||mechanical==null) {
			return;
		}
		if (shouldInitWorld) {
			int offset = 1;
			for (MechMBPart part:mechanical) {
				part.world.setWorld(world);
				part.world.setOrigin(offset(pos, facing, mirrored, 0, -offset, 0));
				offset += part.getLength();
			}
			shouldInitWorld = false;
		}
		// Mechanical
		for (MechMBPart part:mechanical) {
			part.createMEnergy(energyState);
		}
		double requestSum = 0;
		IdentityHashMap<MechMBPart, Double> individualRequests = new IdentityHashMap<>();
		for (MechMBPart part:mechanical) {
			double eForPart = part.requestMEnergy(energyState);
			requestSum += eForPart;
			individualRequests.put(part, eForPart);
		}
		double availableEnergy = energyState.getEnergy()/5;//prevent energy transmission without movement
		double factor = Math.min(availableEnergy/requestSum, 1);
		energyState.extractEnergy(Math.min(requestSum, availableEnergy));
		for (MechMBPart part:mechanical) {
			part.insertMEnergy(factor*individualRequests.get(part));
		}
		Set<MechMBPart> failed = new HashSet<>();
		for (MechMBPart part:mechanical) {
			if (energyState.getSpeed()>part.getMaxSpeed()) {
				failed.add(part);
			}
		}
		if (!failed.isEmpty()) {
			disassemble(failed);
			return;
		}

		//Electrical
		for (int[] section:electricalStartEnd) {
			double[] available = new double[section[1]-section[0]];
			double totalAvailable = 0;
			double[] availablePerWf = new double[Waveform.VALUES.length];
			double[] requested = new double[section[1]-section[0]];
			double totalRequested = 0;
			for (int i = section[0];i<section[1];i++) {
				IMBPartElectric electricalComp = ((IMBPartElectric) mechanical[i]);
				Waveform localWf = electricalComp.getProduced();
				if (localWf==NONE) {
					continue;
				}
				if (localWf == AC_ASYNC&&Math.abs(energyState.getSpeed() - ASYNC_SPEED) >= SYNC_TOLERANCE * ASYNC_SPEED) {
					localWf = AC_SYNC;
				}
				double availableLocal = electricalComp.getAvailableEEnergy();
				totalAvailable += availableLocal;
				available[i - section[0]] = availableLocal;
				availablePerWf[localWf.ordinal()] += availableLocal;
			}
			Waveform maxWf = NONE;
			{
				double max = 0;
				for (int i = 0;i<availablePerWf.length;i++) {
					if (availablePerWf[i]>max) {
						maxWf = Waveform.VALUES[i];
						max = availablePerWf[i];
					}
				}
			}
			for (int i = section[0];i<section[1];i++) {
				IMBPartElectric electricalComp = ((IMBPartElectric) mechanical[i]);
				double requestedLocal = electricalComp.requestEEnergy(maxWf);
				totalRequested += requestedLocal;
				requested[i - section[0]] = requestedLocal;
			}
			// this isn't ideal. It's a lot better than before though
			if (totalAvailable>0&&totalRequested>0) {
				for (int i = section[0]; i < section[1]; i++) {
					IMBPartElectric electricalComp = ((IMBPartElectric) mechanical[i]);
					int i0 = i-section[0];
					if (requested[i0] > 0 && totalAvailable!=available[i0]) {
						double otherAvailable = totalAvailable-available[i0];
						double ins = Math.min(requested[i0], otherAvailable);
						double extractFactor = ins/otherAvailable;
						electricalComp.insertEEnergy(ins, maxWf);
						for (int j = section[0];j<section[1];j++) {
							if (i!=j) {
								IMBPartElectric compJ = (IMBPartElectric) mechanical[j];
								double extractRaw = extractFactor * available[j - section[0]];
								compJ.extractEEnergy(extractRaw);
								available[j-section[0]] -= extractFactor * available[j - section[0]];
							}
						}
						totalAvailable -= ins;
					}
				}
			}
		}

		//General
		energyState.decaySpeed(decay);
		markDirty();
		if (lastSyncedSpeed<energyState.getSpeed()*SYNC_THRESHOLD||lastSyncedSpeed>energyState.getSpeed()/SYNC_THRESHOLD) {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setDouble(SPEED, energyState.getSpeed());
			IndustrialWires.packetHandler.sendToDimension(new MessageTileSyncIW(this, nbt), world.provider.getDimension());
			lastSyncedSpeed = energyState.getSpeed();
		}
	}
	@Override
    public void writeNBT(NBTTagCompound out, boolean updatePacket) {
        super.writeNBT(out, updatePacket);
		if (mechanical!=null) {
			NBTTagList mechParts = new NBTTagList();
			for (MechMBPart part:mechanical) {
				mechParts.appendTag(MechMBPart.toNBT(part));
			}
			out.setTag(PARTS, mechParts);
			out.setDouble(SPEED, energyState.getSpeed());
		}
    }

    @Override
    public void readNBT(NBTTagCompound in, boolean updatePacket) {
        super.readNBT(in, updatePacket);
        if (in.hasKey(PARTS, Constants.NBT.TAG_LIST)) {
			NBTTagList mechParts = in.getTagList(PARTS, Constants.NBT.TAG_COMPOUND);
			MechMBPart[] mech = new MechMBPart[mechParts.tagCount()];
			int offset = 1;
			for (int i = 0; i < mechParts.tagCount(); i++) {
				mech[i] = MechMBPart.fromNBT(mechParts.getCompoundTagAt(i), new LocalSidedWorld(world, offset(pos, facing, mirrored, 0, -offset, 0), facing, mirrored));
				offset += mech[i].getLength();
			}
			setMechanical(mech, in.getDouble(SPEED));
			if (world==null) {
				shouldInitWorld = true;
			}
		}
		rBB = null;
	}

	public void setMechanical(MechMBPart[] mech, double speed) {
		mechanical = mech;
		offsets = new int[mechanical.length];
		double weight = 0;
		int offset = 1;
		List<int[]> electrical = new ArrayList<>();
		int lastEStart = -1;
		for (int i = 0; i < mech.length; i++) {
			offsets[i] = offset;
			weight += mechanical[i].getInertia();
			offset += mechanical[i].getLength();
			if (lastEStart<0&&mechanical[i] instanceof IMBPartElectric) {
				lastEStart = i;
			} else if (lastEStart>=0&&!(mechanical[i] instanceof IMBPartElectric)) {
				electrical.add(new int[]{lastEStart, i});
				lastEStart = -1;
			}
		}
		if (lastEStart>=0) {
			electrical.add(new int[]{lastEStart, mechanical.length});
		}
		electricalStartEnd = electrical.toArray(new int[electrical.size()][]);
		decay = Math.pow(Math.exp(Math.log(.5)/(2*60*60*20)), mechanical.length);//TODO replace with DECAY_BASE
		energyState = new MechEnergy(weight, speed);
	}

	private int getPart(int offset, TileEntityMultiblockConverter master) {
		int pos = 1;
		MechMBPart[] mechMaster = master.mechanical;
		for (int i = 0, mechanical1Length = mechMaster.length; i < mechanical1Length; i++) {
			MechMBPart part = mechMaster[i];
			if (pos >= offset) {
				return i;
			}
			pos += part.getLength();
		}
		return -1;
	}

    @Nonnull
    @Override
    protected BlockPos getOrigin() {
        return pos;//Irrelevant, since this uses a custome disassembly method
    }

    @Override
    public IBlockState getOriginalBlock() {
        return Blocks.AIR.getDefaultState();//Mostly irrelevant, since this uses a custome disassembly method
    }

	@Override
	public void onSync(NBTTagCompound nbt) {
		energyState.setSpeed(nbt.getDouble(SPEED));
	}

	private AxisAlignedBB rBB;
	@Nonnull
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if (rBB==null) {
			if (isLogicDummy()) {
				rBB = new AxisAlignedBB(pos, pos);
			} else {
				rBB = new AxisAlignedBB(offset(pos, facing, mirrored, -1, 0, -1),
						offset(pos, facing, mirrored, 2, mechanical.length, 2));
			}
		}
		return rBB;
	}

	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
		Vec3i offsetDirectional = getOffset(BlockPos.ORIGIN, this.facing, mirrored, offset);
		TileEntityMultiblockConverter master = masterOr(this, this);
		int id = getPart(offsetDirectional.getY(), master);
		if (id<0) {
			return false;
		}
		MechMBPart part = master.mechanical[id];
		Vec3i offsetPart = new Vec3i(offsetDirectional.getX(), offsetDirectional.getY()-master.offsets[id], offsetDirectional.getZ());
		return part.hasCapability(capability, facing, offsetPart);
	}

	@Nullable
	@Override
	public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
		Vec3i offsetDirectional = getOffset(BlockPos.ORIGIN, this.facing, mirrored, offset);
		TileEntityMultiblockConverter master = masterOr(this, this);
		int id = getPart(offsetDirectional.getY(), master);
		if (id<0) {
			return null;
		}
		MechMBPart part = master.mechanical[id];
		Vec3i offsetPart = new Vec3i(offsetDirectional.getX(), offsetDirectional.getY()-master.offsets[id], offsetDirectional.getZ());
		return part.getCapability(capability, facing, offsetPart);
	}

	@Override
	public void disassemble() {
		if (formed) {
			TileEntityMultiblockConverter master = master(this);
			if (master!=null) {
				boolean disassembled = false;
				int part = master.getPart(offset.getX(), master);
				if (part>=0) {
					MechMBPart m = master.mechanical[part];
					if (master.energyState.getSpeed()>.05*m.getMaxSpeed()) {
						master.disassemble(ImmutableSet.of(m));
						disassembled = true;
					}
				}
				if (!disassembled)
					master.disassemble(ImmutableSet.of());
			}
		}
	}

	private void disassemble(Set<MechMBPart> failed) {
		if (!world.isRemote&&formed) {
			formed = false;
			world.setBlockState(pos,
					blockMetalDecoration0.getDefaultState().withProperty(blockMetalDecoration0.property, LIGHT_ENGINEERING));
			world.setBlockState(pos.down(),
					blockMetalDecoration0.getDefaultState().withProperty(blockMetalDecoration0.property, LIGHT_ENGINEERING));
			for (MechMBPart mech:mechanical) {
				mech.disassemble(failed.contains(mech), energyState);
				short pattern = mech.getFormPattern();
				for (int i = 0;i<9;i++) {
					if (((pattern>>i)&1)!=0) {
						BlockPos pos = new BlockPos(i%3-1, i/3-1, 0);
						if (mech.world.getBlockState(pos).getBlock()==IndustrialWires.mechanicalMB) {
							mech.world.setBlockState(pos, Blocks.AIR.getDefaultState());
						}
					}
				}
			}
			BlockPos otherEnd = offset(pos, facing.getOpposite(), mirrored, 0,
					offsets[offsets.length-1]+mechanical[mechanical.length-1].getLength(), 0);
			world.setBlockState(otherEnd,
					blockMetalDecoration0.getDefaultState().withProperty(blockMetalDecoration0.property, LIGHT_ENGINEERING));
			world.setBlockState(otherEnd.down(),
					blockMetalDecoration0.getDefaultState().withProperty(blockMetalDecoration0.property, LIGHT_ENGINEERING));
		}
	}
}

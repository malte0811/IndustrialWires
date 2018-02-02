package malte0811.industrialWires.blocks.converter;

import blusunrize.immersiveengineering.api.ApiUtils;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.ISyncReceiver;
import malte0811.industrialWires.blocks.TileEntityIWMultiblock;
import malte0811.industrialWires.converter.MechEnergy;
import malte0811.industrialWires.converter.MechMBPart;
import malte0811.industrialWires.network.MessageTileSyncIW;
import malte0811.industrialWires.util.MiscUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.IdentityHashMap;
import java.util.List;

import static malte0811.industrialWires.util.NBTKeys.PARTS;
import static malte0811.industrialWires.util.NBTKeys.SPEED;

public class TileEntityMultiblockConverter extends TileEntityIWMultiblock implements ITickable, ISyncReceiver {
	private static final double DECAY_BASE = .99;
	public static final double TICK_ANGLE_PER_SPEED = 180/20/Math.PI;
	private static final double SYNC_THRESHOLD = 1-1e-9;
	public MechMBPart[] mechanical;
	public MechEnergy energyState;
	private double lastSyncedSpeed = 0;
	private double decay;
	public double angle;
	@SideOnly(Side.CLIENT)
	public List<BakedQuad> rotatingModel;
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
		//energyState = new MechEnergy(energyState.weight, 10);
		for (MechMBPart part:mechanical) {
			part.produceRotation(energyState);
		}
		double requestSum = 0;
		IdentityHashMap<MechMBPart, Double> individualRequests = new IdentityHashMap<>();
		for (MechMBPart part:mechanical) {
			double eForPart = part.requestEnergy(energyState);
			requestSum += eForPart;
			individualRequests.put(part, eForPart);
		}
		double availableEnergy = energyState.getEnergy();
		double factor = (requestSum>availableEnergy)?availableEnergy/requestSum:1;
		energyState.extractEnergy(factor*requestSum);
		for (MechMBPart part:mechanical) {
			part.consumeRotation(factor*individualRequests.get(part));
		}
		//TODO check for "overspeed"

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
			for (int i = 0; i < mechParts.tagCount(); i++) {
				mech[i] = MechMBPart.fromNBT(mechParts.getCompoundTagAt(i), this);
			}
			setMechanical(mech, in.getDouble(SPEED));
		}
		rBB = null;
	}

	public void setMechanical(MechMBPart[] mech, double speed) {
		Minecraft.getMinecraft().mouseHelper.ungrabMouseCursor();
		mechanical = mech;
		double weight = 0;
		for (int i = 0; i < mech.length; i++) {
			weight += mechanical[i].getWeight();
		}
		decay = Math.pow(DECAY_BASE, mechanical.length);//TODO init
		energyState = new MechEnergy(weight, speed);
	}

    @Nonnull
    @Override
    protected BlockPos getOrigin() {
        return pos;
    }

    @Override
    public IBlockState getOriginalBlock() {
        return null;
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
				rBB = new AxisAlignedBB(MiscUtils.offset(pos, facing, mirrored, -1, 0, -1),
						MiscUtils.offset(pos, facing, mirrored, 2, mechanical.length, 2));
			}
		}
		return rBB;
	}
}

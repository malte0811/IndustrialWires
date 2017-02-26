/*
 * This file is part of Industrial Wires.
 * Copyright (C) 2016-2017 malte0811
 *
 * Industrial Wires is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Industrial Wires is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Industrial Wires.  If not, see <http://www.gnu.org/licenses/>.
 */

package malte0811.industrialWires.blocks;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import malte0811.industrialWires.IWConfig;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.network.MessageTileSyncIW;
import malte0811.industrialWires.util.Beziers;
import malte0811.industrialWires.util.DualEnergyStorage;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;

//TODO facing!
public class TileEntityJacobsLadder extends TileEntityIEBase implements ITickable, IEBlockInterfaces.IHasDummyBlocks, ISyncReceiver, IEnergySink {

	public static final SoundEvent sound = new SoundEvent(new ResourceLocation("industrialwires:jacobs_ladder"));
	DualEnergyStorage energy;
	public LadderSize size;

	public Vec3d[] controls;
	//first and last move along the "rails", only the middle points move in bezier curves
	public Vec3d[][] controlControls;
	// movement of the controls in blocks/tick
	public Vec3d[] controlMovement;
	private double t = 0;
	private int dummy = 0;
	public int timeTillActive = -1;
	private double tStep = 0;
	private double consumtionEU;
	private boolean addedToIC2Net = false;
	private int soundPhase;
	private Vec3d soundPos;

	public TileEntityJacobsLadder(LadderSize s) {
		size = s;
		initControl();
	}

	public TileEntityJacobsLadder() {
		size = LadderSize.HUGE;
		initControl();
	}

	private void initControl() {
		controls = new Vec3d[size.arcPoints];
		controlControls = new Vec3d[size.arcPoints - 2][size.movementPoints];
		controlMovement = new Vec3d[size.arcPoints];
		int sizeId = size.ordinal();
		consumtionEU = IWConfig.HVStuff.jacobsUsageEU[sizeId];
		energy = new DualEnergyStorage(20*consumtionEU, 2*consumtionEU);
	}

	@Override
	public void update() {
		if (isDummy()) {
			return;
		}
		if (!worldObj.isRemote) {
			if (!addedToIC2Net) {
				MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
				addedToIC2Net = true;
			}
			if ((controlControls[0][0] == null || timeTillActive==-1 || t >= 1)&&energy.getEnergyStoredEU()>=2*consumtionEU) {
				for (int j = 0; j < size.movementPoints; j++) {
					double y = j * (size.height + size.extraHeight) / (double) (size.movementPoints - 1) + size.innerPointOffset;
					double width = widthFromHeight(y);
					for (int i = 0; i < size.arcPoints - 2; i++) {
						double z = size.zMax * 2 * (worldObj.rand.nextDouble() - .5);
						double xMin = width * i / (double) (size.arcPoints - 1) - width / 4;
						double xDiff = width / (double) (size.arcPoints - 1);
						double x = worldObj.rand.nextDouble() * xDiff + xMin;
						controlControls[i][j] = new Vec3d(x, y, z);
					}
				}
				t = 0;
				timeTillActive = size.delay;
				tStep = 1D / (int) (.875 * size.tickToTop + worldObj.rand.nextInt(size.tickToTop / 4));
				IndustrialWires.packetHandler.sendToAll(new MessageTileSyncIW(this, writeArcStarter()));
			} else if (timeTillActive==0&&t<1) {
				double extracted = energy.extractEU(consumtionEU, false);
				if (extracted>=consumtionEU) {
					energy.extractEU(consumtionEU, true);
				} else {
					timeTillActive = -1-size.delay;
					NBTTagCompound nbt = new NBTTagCompound();
					nbt.setBoolean("cancel", true);
					IndustrialWires.packetHandler.sendToAll(new MessageTileSyncIW(this, nbt));
				}
			} else if (timeTillActive<-1) {
				//delay after energy was cut
				timeTillActive++;
			}
		} else  {
			if (timeTillActive==0&&t<1) {
				for (int i = 0; i < size.arcPoints; i++) {
					controls[i] = controls[i].add(controlMovement[i]);
				}
				for (int i = 1; i < size.arcPoints - 1; i++) {
					controlMovement[i] = Beziers.getPoint(t, controlControls[i - 1]).subtract(controls[i]);
				}
				if (t>=7*tStep&&soundPhase==0) {
					IndustrialWires.proxy.playJacobsLadderSound(this, 1, soundPos);
					soundPhase = 1;
				} else if (t>=1-(4*tStep)&&soundPhase==1) {
					IndustrialWires.proxy.playJacobsLadderSound(this, 2, soundPos);
					soundPhase = 2;
				}
			} else if (t>1) {
				timeTillActive = -1;
			}
		}
		if (timeTillActive > 0) {
			timeTillActive--;
		} else if (timeTillActive==0&&t<1) {
			t += tStep;
		}
	}

	private void initArc() {
		controls[0] = new Vec3d(0, 0, 0);
		controls[size.arcPoints - 1] = new Vec3d(size.bottomDistance, 0, 0);
		controlMovement[0] = new Vec3d(-(size.topDistance - size.bottomDistance) / (2*size.height * size.tickToTop), size.height / size.tickToTop, 0);
		controlMovement[size.arcPoints - 1] = new Vec3d((size.topDistance - size.bottomDistance) / (2*size.height * size.tickToTop), size.height / size.tickToTop, 0);
		t = 0;
		for (int i = 1; i < size.arcPoints - 1; i++) {
			controls[i] = Beziers.getPoint(0, controlControls[i - 1]);
			controlMovement[i] = Beziers.getPoint(tStep, controlControls[i - 1]).subtract(controls[i]);
		}
		double soundX = pos.getX()+.5;
		double soundY = pos.getY()+.5*size.dummyCount+size.heightOffset;
		double soundZ = pos.getZ()+.5;
		soundPos = new Vec3d(soundX, soundY, soundZ);
		IndustrialWires.proxy.playJacobsLadderSound(this, 0, soundPos);
		soundPhase = 0;
	}

	private double widthFromHeight(double h) {
		return size.bottomDistance + h * (size.topDistance - size.bottomDistance) / size.height;
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		LadderSize oldSize = size;
		size = LadderSize.values()[nbt.getInteger("size")];
		if (size != oldSize) {
			initControl();
		}
		dummy = nbt.getInteger("dummy");
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		nbt.setInteger("size", size.ordinal());
		nbt.setInteger("dummy", dummy);
	}
	private NBTTagCompound writeArcStarter() {
		NBTTagCompound nbt = new NBTTagCompound();
		NBTTagList ctrlCtrl = write2DVecArray(controlControls);
		nbt.setTag("ctrlCtrl", ctrlCtrl);
		nbt.setInteger("timeTillActive", timeTillActive);
		nbt.setDouble("tStep", tStep);
		return nbt;
	}
	private void readArcStarter(NBTTagCompound nbt) {
		controlControls = read2DVecArray(nbt.getTagList("ctrlCtrl", 9));
		timeTillActive = nbt.getInteger("timeTillActive");
		if (timeTillActive<=0) {
			timeTillActive = 1;
		}
		tStep = nbt.getDouble("tStep");
		Minecraft.getMinecraft().addScheduledTask(this::initArc);
	}
	private Vec3d[][] read2DVecArray(NBTTagList nbt) {
		Vec3d[][] ret = new Vec3d[nbt.tagCount()][];
		for (int i = 0;i<ret.length;i++) {
			ret[i] = readVecArray((NBTTagList)nbt.get(i));
		}
		return ret;
	}

	private Vec3d[] readVecArray(NBTTagList nbt) {
		Vec3d[] ret = new Vec3d[nbt.tagCount()];
		for (int i = 0;i<ret.length;i++) {
			NBTTagCompound vec = nbt.getCompoundTagAt(i);
			ret[i] = new Vec3d(vec.getDouble("x"), vec.getDouble("y"), vec.getDouble("z"));
		}
		return ret;
	}

	private NBTTagList write2DVecArray(Vec3d[][] array) {
		NBTTagList ret = new NBTTagList();
		for (Vec3d[] subArray:array) {
			ret.appendTag(writeVecArray(subArray));
		}
		return ret;
	}
	private NBTTagList writeVecArray(Vec3d[] array) {
		NBTTagList ret = new NBTTagList();
		for (Vec3d point:array) {
			NBTTagCompound vec = new NBTTagCompound();
			vec.setDouble("x", point.xCoord);
			vec.setDouble("y", point.yCoord);
			vec.setDouble("z", point.zCoord);
			ret.appendTag(vec);
		}
		return ret;
	}
	@Override
	public boolean isDummy() {
		return dummy != 0;
	}

	@Override
	public void placeDummies(BlockPos pos, IBlockState state, EnumFacing side, float hitX, float hitY, float hitZ) {
		for (int i = 1; i <= size.dummyCount; i++) {
			BlockPos pos2 = pos.offset(EnumFacing.UP, i);
			worldObj.setBlockState(pos2, state);
			TileEntity te = worldObj.getTileEntity(pos2);
			if (te instanceof TileEntityJacobsLadder) {
				((TileEntityJacobsLadder) te).size = size;
				((TileEntityJacobsLadder) te).dummy = i;
				//TODO facing ((TileEntityJacobsLadder)te).size = size;
			}
		}
	}

	@Override
	public void breakDummies(BlockPos pos, IBlockState state) {
		for (int i = 0; i <= size.dummyCount; i++) {
			if (i!=dummy&&worldObj.getTileEntity(pos.offset(EnumFacing.UP, i-dummy)) instanceof TileEntityJacobsLadder) {
				worldObj.setBlockToAir(pos.offset(EnumFacing.UP, i-dummy));
			}
		}
	}

	@Override
	public void onSync(NBTTagCompound nbt) {
		if (nbt.getBoolean("cancel")) {
			timeTillActive = -1;
		} else {
			readArcStarter(nbt);
		}
	}
	//ENERGY
	@Override
	public double getDemandedEnergy() {
		return energy.getEURequested();
	}

	@Override
	public int getSinkTier() {
		return 4;
	}

	@Override
	public double injectEnergy(EnumFacing dir, double amount, double voltage) {
		return amount-energy.insertEU(amount, true);
	}

	@Override
	public boolean acceptsEnergyFrom(IEnergyEmitter iEnergyEmitter, EnumFacing enumFacing) {
		return !isDummy()&&enumFacing!=EnumFacing.UP;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		if (isDummy()) {
			return false;
		}
		if (facing==EnumFacing.UP) {
			return false;
		}
		return capability== CapabilityEnergy.ENERGY;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if (hasCapability(capability, facing)) {
			if (capability==CapabilityEnergy.ENERGY) {
				return (T) new EnergyCap();
			}
		}
		return null;
	}

	@Override
	public void onChunkUnload() {
		if (!worldObj.isRemote&&addedToIC2Net)
			MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
		addedToIC2Net = false;
		super.onChunkUnload();
	}

	@Override
	public void invalidate() {
		if (!worldObj.isRemote&&addedToIC2Net) {
			MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
		} else if (worldObj.isRemote) {
			IndustrialWires.proxy.playJacobsLadderSound(this, -1, soundPos);
		}
		addedToIC2Net = false;
		super.invalidate();
	}


	public enum LadderSize implements IStringSerializable {
		SMALL(4, 4, .5, .375, .1, 20, .05, .2, .05, .5, 0, 5, 5, .03725),
		NORMAL(4, 4, 1.8, 1.125, .25, 30, .2, .5, .3, 1, 1, 5, 10, .075),
		HUGE(4, 5, 1.8, 2.25, .25, 30, .2, .5, .3, 1, 2, 5, 10, .125);
		public final int arcPoints;
		public final int movementPoints;
		// height of the electrodes
		public final double height;
		// distance between the electrodes at top and bottom
		public final double topDistance;
		public final double bottomDistance;
		// ticks it takes for the arc to reach the top of the ladder on average
		public final int tickToTop;
		// maximum z offset for the inner bezier points
		public final double zMax;
		// additional height for inner bezier points, added to normal height and scaled
		public final double extraHeight;
		// fixed additional height for inner bezier points
		public final double innerPointOffset;
		// offset for rendering the arc, e.g. a value of 1 means the arc starts 1 block above the TE
		public final double heightOffset;
		// ticks between the end of one arc and the start of the next
		public final int delay;
		public final int renderPoints;
		public final int dummyCount;
		public final double renderDiameter;

		private LadderSize(int arcP, int movP, double h, double topD, double bottomD, int ttTop, double zMax, double extraH,
						   double iOff, double hOff, int dummies, int delay, int points, double renderDia) {
			arcPoints = arcP;
			movementPoints = movP;
			height = h;
			topDistance = topD;
			bottomDistance = bottomD;
			tickToTop = ttTop;
			this.zMax = zMax;
			extraHeight = extraH;
			innerPointOffset = iOff;
			heightOffset = hOff;
			dummyCount = dummies;
			this.delay = delay;
			renderPoints = points;
			renderDiameter = renderDia;
		}

		@Override
		public String getName() {
			return name().toLowerCase();
		}
	}
	public class EnergyCap implements IEnergyStorage {

		@Override
		public int receiveEnergy(int maxReceive, boolean simulate) {
			return (int) energy.insertIF(maxReceive, !simulate);
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate) {
			return 0;
		}

		@Override
		public int getEnergyStored() {
			return (int) energy.getEnergyStoredIF();
		}

		@Override
		public int getMaxEnergyStored() {
			return (int) energy.getMaxStoredIF();
		}

		@Override
		public boolean canExtract() {
			return false;
		}

		@Override
		public boolean canReceive() {
			return true;
		}
	}
}

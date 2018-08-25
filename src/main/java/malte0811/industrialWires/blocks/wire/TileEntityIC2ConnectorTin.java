/*
 * This file is part of Industrial Wires.
 * Copyright (C) 2016-2018 malte0811
 * Industrial Wires is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Industrial Wires is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with Industrial Wires.  If not, see <http://www.gnu.org/licenses/>.
 */
package malte0811.industrialWires.blocks.wire;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.wires.*;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.AbstractConnection;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import malte0811.industrialWires.IMixedConnector;
import malte0811.industrialWires.IWConfig;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.IBlockBoundsIW;
import malte0811.industrialWires.compat.Compat;
import malte0811.industrialWires.util.ConversionUtil;
import malte0811.industrialWires.util.MiscUtils;
import malte0811.industrialWires.wires.EnergyType;
import malte0811.industrialWires.wires.MixedWireType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.Optional;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

import static malte0811.industrialWires.wires.EnergyType.*;
import static malte0811.industrialWires.wires.MixedWireType.TIN;

@Optional.InterfaceList({
		@Optional.Interface(iface = "ic2.api.energy.tile.IEnergySource", modid = "ic2"),
		@Optional.Interface(iface = "ic2.api.energy.tile.IEnergySink", modid = "ic2")
})
public class TileEntityIC2ConnectorTin extends TileEntityImmersiveConnectable implements IEnergySource, IEnergySink, IDirectionalTile,
		ITickable, IMixedConnector, IBlockBoundsIW {
	private static final double EPS = .1;
	private EnumFacing facing = EnumFacing.NORTH;
	private boolean relay;
	// external net to IE net buffer
	private double bufferToNet = 0;
	private double potentialIEInputInTick = 0;
	private double actualIEInputInTick = 0;
	private double maxToNet = 0;
	//IE net to external net buffer
	private double bufferToMachine = 0;
	private double externalInputInTick = 0;
	private double maxToMachine = 0;
	private EnergyType energyType = NONE;
	private boolean shouldBreak = false;
	private final double maxIO;
	private final MixedWireType wireType;
	private final int tier;
	private final double relayOffset;
	private final double connOffset;

	protected TileEntityIC2ConnectorTin(boolean relay, MixedWireType type, int tier, double relayLength, double connLength) {
		this.relay = relay;
		wireType = type;
		maxIO = type.getIORate();
		this.tier = tier;
		this.relayOffset = relayLength-.5;
		this.connOffset = connLength-.5;
	}

	public TileEntityIC2ConnectorTin(boolean relay) {
		this(relay, TIN, 1, .5, .5);
	}

	public TileEntityIC2ConnectorTin() {
		this(false);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		if (!world.isRemote&& IndustrialWires.hasIC2)
			Compat.loadIC2Tile.accept(this);
		ImmersiveNetHandler.INSTANCE.onTEValidated(this);
	}

	@Override
	public void update() {
		if (!world.isRemote) {
			if (shouldBreak) {
				Deque<BlockPos> open = new ArrayDeque<>();
				open.push(pos);
				Set<BlockPos> closed = new HashSet<>();
				closed.add(pos);
				while (!open.isEmpty()) {
					BlockPos next = open.pop();
					Set<Connection> conns = ImmersiveNetHandler.INSTANCE.getConnections(world, next);
					if (conns!=null) {
						for (Connection c:conns) {
							ImmersiveNetHandler.INSTANCE.getTransferedRates(world.provider.getDimension())
									.put(c, 2*c.cableType.getTransferRate());
							if (closed.add(c.end)) {
								open.push(c.end);
							}
						}
					}
				}
				for (BlockPos p:closed) {
					TileEntity tile = world.getTileEntity(p);
					if (tile instanceof IImmersiveConnectable && ((IImmersiveConnectable) tile).isEnergyOutput()) {
						world.createExplosion(null, p.getX()+.5, p.getY()+.5, p.getZ()+.5,
								3, true);
					}
				}
				return;
			}
			if (externalInputInTick ==0 && potentialIEInputInTick == 0 && bufferToNet == 0 && bufferToMachine == 0) {
				energyType = NONE;
			}
			if (bufferToNet < maxToNet) {
				maxToNet = bufferToNet;
			}
			if (externalInputInTick > maxToNet) {
				maxToNet = externalInputInTick;
			}
			externalInputInTick = 0;

			if (bufferToMachine < maxToMachine) {
				maxToMachine = bufferToMachine;
			}
			potentialIEInputInTick = Math.max(Math.max(potentialIEInputInTick, actualIEInputInTick), getMaxIO());
			if (potentialIEInputInTick > maxToMachine) {
				maxToMachine = potentialIEInputInTick;
			}
			potentialIEInputInTick = 0;
			actualIEInputInTick = 0;
			if (bufferToNet > EPS) {
				transferPowerToNet();
			}
			if (bufferToNet >EPS) {
				notifyAvailableEnergy(bufferToNet);
			}
			if (bufferToMachine > EPS && energyType==FE_AC) {
				transferPowerToFEMachine();
			}
		}
	}

	private void transferPowerToNet() {
		Set<AbstractConnection> conns = ImmersiveNetHandler.INSTANCE.getIndirectEnergyConnections(pos, world, true);
		Map<AbstractConnection, Pair<IMixedConnector, Double>> maxOutputs = new HashMap<>();
		double outputMax = Math.min(bufferToNet, maxToNet);
		double sum = 0;
		for (AbstractConnection c : conns) {
			if (c.isEnergyOutput) {
				IImmersiveConnectable iic = ApiUtils.toIIC(c.end, world);
				if (iic instanceof IMixedConnector) {
					double extract =
							outputMax - ((IMixedConnector) iic).insertEnergy(outputMax, true, energyType);
					if (extract > EPS) {
						maxOutputs.put(c, new ImmutablePair<>((IMixedConnector) iic, extract));
						sum += extract;
					}
				}
			}
		}
		if (sum > EPS) {
			HashMap<Connection, Integer> transferedPerConn = ImmersiveNetHandler.INSTANCE.getTransferedRates(world.provider.getDimension());
			for (Map.Entry<AbstractConnection, Pair<IMixedConnector, Double>> entry : maxOutputs.entrySet()) {
				Pair<IMixedConnector, Double> p = entry.getValue();
				AbstractConnection c = entry.getKey();
				double out = outputMax * p.getRight() / sum;
				double loss = energyType.getLoss(getAverageLossRate(c), bufferToNet, out);
				out = Math.min(out, bufferToNet -loss);
				if (out<=0)
					continue;
				double inserted = out - p.getLeft().insertEnergy(out, false, energyType);
				double energyAtConn = inserted + loss;
				bufferToNet -= energyAtConn;
				float intermediaryLoss = 0;
				HashSet<IImmersiveConnectable> passedConnectors = new HashSet<>();
				for (Connection sub : c.subConnections) {
					int transferredPerCon = transferedPerConn.getOrDefault(sub, 0);
					energyAtConn -= sub.cableType.getLossRatio() * sub.length;
					double wireLoad = energyAtConn/(energyType==FE_AC?IWConfig.wireRatio:1);
					transferedPerConn.put(sub, (int) (transferredPerCon + wireLoad));
					IImmersiveConnectable subStart = ApiUtils.toIIC(sub.start, world);
					IImmersiveConnectable subEnd = ApiUtils.toIIC(sub.end, world);
					if (subStart != null && passedConnectors.add(subStart))
						subStart.onEnergyPassthrough((int) (inserted - inserted * intermediaryLoss));
					if (subEnd != null && passedConnectors.add(subEnd))
						subEnd.onEnergyPassthrough((int) (inserted - inserted * intermediaryLoss));
				}
			}
		}
	}

	private void transferPowerToFEMachine() {
		BlockPos outPos = pos.offset(facing);
		TileEntity te = MiscUtils.getLoadedTE(world, outPos, TileEntity.class);
		if (te!=null && te.hasCapability(CapabilityEnergy.ENERGY, facing.getOpposite())) {
			IEnergyStorage handler = te.getCapability(CapabilityEnergy.ENERGY, facing.getOpposite());
			assert handler!=null;
			double outJoules = Math.min(bufferToMachine, maxToMachine*IWConfig.wireRatio);
			int outFE = MathHelper.floor(outJoules*ConversionUtil.ifPerJoule());
			int received = handler.receiveEnergy(outFE, false);
			bufferToMachine -= received*ConversionUtil.joulesPerIf();
		}
	}

	private void notifyAvailableEnergy(double storedNew)
	{
		Set<AbstractConnection> outputs = ImmersiveNetHandler.INSTANCE.getIndirectEnergyConnections(pos, world, true);
		for(AbstractConnection con : outputs)
		{
			IImmersiveConnectable end = ApiUtils.toIIC(con.end, world);
			if(con.cableType!=null && end!=null && end.allowEnergyToPass(null))
			{
				Pair<Float, Consumer<Float>> e = getEnergyForConnection(con, storedNew);
				end.addAvailableEnergy(e.getKey(), e.getValue());
			}
		}
		addAvailableEnergy(-1, null);
	}

	private Pair<Float, Consumer<Float>> getEnergyForConnection(@Nullable AbstractConnection c, double storedNew)
	{
		float loss = c!=null?c.getAverageLossRate():0;
		float max = (float) (storedNew-loss);
		Consumer<Float> extract = (energy)-> bufferToNet -= energy+loss;
		return new ImmutablePair<>(max, extract);
	}

	private double getAverageLossRate(AbstractConnection conn) {
		double f = 0;
		for (Connection c : conn.subConnections) {
			WireType type = c.cableType;
			if (type instanceof MixedWireType) {
				f += c.length * ((MixedWireType)type).getLoss(energyType);
			} else {
				f = Double.POSITIVE_INFINITY;
			}
		}
		return f;
	}

	//Input through the net
	@Override
	public double insertEnergy(double joules, boolean simulate, EnergyType type) {
		if (energyType==NONE) {
			energyType = type;
		} else if (energyType!=type) {
			shouldBreak = true;
			return 0;
		}
		double insert = Math.min(getMaxIO() - bufferToMachine, joules);
		insert = Math.min(getMaxIO()-actualIEInputInTick, insert);
		if (!simulate) {
			bufferToMachine += insert;
			actualIEInputInTick += insert;
		} else {
			//Yes, this is weird. But it works, otherwise the system can get stuck at a lower output rate with a full buffer
			potentialIEInputInTick += Math.min(joules, getMaxIO());
		}
		return joules - insert;
	}

	private double getMaxIO() {
		return maxIO*(energyType==FE_AC?IWConfig.wireRatio:1);
	}

	@Override
	public void invalidate() {
		if (!world.isRemote)
			MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
		super.invalidate();
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		if (!world.isRemote)
			MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
	}

	@Override
	public Vec3d getConnectionOffset(Connection con) {
		EnumFacing side = facing.getOpposite();
		double conRadius = con.cableType.getRenderDiameter() / 2;
		double length = relay?relayOffset:connOffset;
		return new Vec3d(.5 + ( length - conRadius) * side.getFrontOffsetX(),
				.5 + (length - conRadius) * side.getFrontOffsetY(),
				.5 + (length - conRadius) * side.getFrontOffsetZ());
	}

	@Override
	public boolean canConnect() {
		return true;
	}

	@Override
	public boolean isEnergyOutput() {
		return !relay;
	}

	@Override
	public boolean canConnectCable(WireType cableType, TargetingInfo target, Vec3i offset) {
		return (limitType == null || this.isRelay()) && WireApi.canMix(cableType, wireType);
	}

	@Override
	protected boolean isRelay() {
		return relay;
	}

	@Override
	@Optional.Method(modid="ic2")
	public boolean emitsEnergyTo(IEnergyAcceptor receiver, EnumFacing side) {
		return !relay && side == facing;
	}

	@Override
	@Optional.Method(modid="ic2")
	public boolean acceptsEnergyFrom(IEnergyEmitter emitter, EnumFacing side) {
		return !relay && side == facing;
	}

	@Override
	@Optional.Method(modid="ic2")
	public double getDemandedEnergy() {
		double ret = (getMaxIO() - bufferToNet) *ConversionUtil.euPerJoule() + .5;
		if (ret < .1)
			ret = 0;
		return ret;
	}

	@Override
	@Optional.Method(modid="ic2")
	public int getSinkTier() {
		return tier;
	}

	@Override
	@Optional.Method(modid="ic2")
	public double injectEnergy(EnumFacing directionFrom, double amount, double voltage) {
		return amount-ConversionUtil.euPerJoule()*addToIn(ConversionUtil.joulesPerEu()*amount, false, EU_DC);
	}

	@Override
	@Optional.Method(modid="ic2")
	public double getOfferedEnergy() {
		return Math.min(maxToMachine, bufferToMachine) * ConversionUtil.euPerJoule();
	}

	@Override
	@Optional.Method(modid="ic2")
	public void drawEnergy(double amount) {
		bufferToMachine -= amount*ConversionUtil.joulesPerEu();
		markDirty();
	}

	@Nullable
	protected Pair<Float,Consumer<Float>> getOwnEnergy()
	{
		if (isRelay())
			return null;
		return new ImmutablePair<>((float) bufferToNet, (d)-> bufferToNet -= d);
	}
	@Override
	protected float getBaseDamage(ImmersiveNetHandler.Connection c) {
		return 1/64F;
	}

	@Override
	@Optional.Method(modid="ic2")
	public int getSourceTier() {
		return tier;
	}

	// Returns amount of energy consumed
	private double addToIn(double joules, boolean simulate, EnergyType type) {
		if (energyType==NONE) {
			energyType = type;
		} else if (energyType!=type) {
			shouldBreak = true;
		}
		joules = Math.min(getMaxIO()-externalInputInTick, joules);
		if (bufferToNet < getMaxIO()) {
			if (!simulate) {
				bufferToNet += joules;
				externalInputInTick += joules;
				notifyAvailableEnergy(joules);
			}
			markDirty();
			return joules;
		}
		return 0;
	}

	@Override
	public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
		super.readCustomNBT(nbt, descPacket);
		facing = EnumFacing.getFront(nbt.getInteger("facing"));
		relay = nbt.getBoolean("relay");
		int version = nbt.getInteger("version");
		bufferToNet = nbt.getDouble("inBuffer");
		bufferToMachine = nbt.getDouble("outBuffer");
		if (nbt.hasKey("maxToNet")) {
			maxToNet = nbt.getDouble("maxToNet");
		} else {
			maxToNet = bufferToNet;
		}
		if (nbt.hasKey("maxToMachine")) {
			maxToMachine = nbt.getDouble("maxToMachine");
		} else {
			maxToMachine = bufferToMachine;
		}
		energyType = EnergyType.values()[nbt.getInteger("energyType")];
		if (version==0) {
			bufferToNet *= ConversionUtil.joulesPerEu();
			bufferToMachine *= ConversionUtil.joulesPerEu();
			maxToNet *= ConversionUtil.joulesPerEu();
			maxToMachine *= ConversionUtil.joulesPerEu();
		}
	}

	@Override
	public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
		super.writeCustomNBT(nbt, descPacket);
		nbt.setInteger("facing", facing.getIndex());
		nbt.setBoolean("relay", relay);
		nbt.setDouble("inBuffer", bufferToNet);
		nbt.setDouble("outBuffer", bufferToMachine);
		nbt.setDouble("maxToNet", maxToNet);
		nbt.setDouble("maxToMachine", maxToMachine);
		nbt.setInteger("energyType", energyType.ordinal());
		nbt.setInteger("version", 1);
	}

	@Nonnull
	@Override
	public EnumFacing getFacing() {
		return facing;
	}

	@Override
	public void setFacing(@Nonnull EnumFacing facing) {
		this.facing = facing;
	}

	@Override
	public int getFacingLimitation() {
		return 0;
	}

	@Override
	public boolean mirrorFacingOnPlacement(@Nonnull EntityLivingBase placer) {
		return true;
	}

	@Override
	public boolean canHammerRotate(@Nonnull EnumFacing side, float hitX, float hitY, float hitZ, @Nonnull EntityLivingBase entity) {
		return false;
	}

	@Override
	public AxisAlignedBB getBoundingBox() {
		double length = .5+(relay?relayOffset:connOffset);
		double wMin = .3125;
		double wMax = .6875;
		switch (facing.getOpposite()) {
		case UP:
			return new AxisAlignedBB(wMin, 0, wMin, wMax, length, wMax);
		case DOWN:
			return new AxisAlignedBB(wMin, 1 - length, wMin, wMax, 1, wMax);
		case SOUTH:
			return new AxisAlignedBB(wMin, wMin, 0, wMax, wMax, length);
		case NORTH:
			return new AxisAlignedBB(wMin, wMin, 1 - length, wMax, wMax, 1);
		case EAST:
			return new AxisAlignedBB(0, wMin, wMin, length, wMax, wMax);
		case WEST:
			return new AxisAlignedBB(1 - length, wMin, wMin, 1, wMax, wMax);
		}
		return new AxisAlignedBB(0, 0, 0, 1, 1, 1);
	}

	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
		if (capability==CapabilityEnergy.ENERGY) {
			return !isRelay() && facing == this.facing;
		}
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability==CapabilityEnergy.ENERGY) {
			if (!isRelay() && facing == this.facing) {
				return CapabilityEnergy.ENERGY.cast(energyHandler);
			}
			return null;
		}
		return super.getCapability(capability, facing);
	}

	/*
	 * regarding equals+hashCode
	 * TE's are considered equal if they have the same pos+dimension id
	 * This is necessary to work around a weird bug causing a lot of log spam (100GB and above are well possible).
	 * For further information see #1 (https://github.com/malte0811/IndustrialWires/issues/1)
	 */
	@Override
	public int hashCode() {
		if (world==null)
			return 0;
		int ret = world.provider.getDimension();
		ret = 31 * ret + pos.hashCode();
		return ret;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof TileEntityIC2ConnectorTin)) {
			return false;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		TileEntityIC2ConnectorTin te = (TileEntityIC2ConnectorTin) obj;
		if (!te.pos.equals(pos)) {
			return false;
		}
		if (te.world.provider.getDimension() != world.provider.getDimension()) {
			return false;
		}
		return true;
	}

	@Override
	public boolean canRotate(@Nonnull EnumFacing axis) {
		return false;
	}

	private EnergyHandler energyHandler = new EnergyHandler();

	private class EnergyHandler implements IEnergyStorage {

		@Override
		public int receiveEnergy(int maxReceive, boolean simulate) {
			if (bufferToNet>=getMaxIO()) {
				return 0;
			}
			double joules = maxReceive*ConversionUtil.joulesPerIf();
			double accepted = addToIn(joules, simulate, FE_AC);
			return MathHelper.ceil(accepted*ConversionUtil.ifPerJoule());
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate) {
			if (energyType!=FE_AC) {
				return 0;
			}
			double joules = maxExtract*ConversionUtil.joulesPerIf();
			if (joules>maxToMachine) {
				joules = maxToMachine;
			}
			if (joules>bufferToMachine) {
				joules = bufferToMachine;
			}
			if (!simulate) {
				bufferToMachine -= joules;
			}
			return MathHelper.floor(ConversionUtil.ifPerJoule()*joules);
		}

		@Override
		public int getEnergyStored() {
			return (int)((bufferToMachine+bufferToNet)*ConversionUtil.ifPerJoule());
		}

		@Override
		public int getMaxEnergyStored() {
			return (int) (2* getMaxIO() *ConversionUtil.ifPerJoule());
		}

		@Override
		public boolean canExtract() {
			return true;
		}

		@Override
		public boolean canReceive() {
			return true;
		}
	}
}

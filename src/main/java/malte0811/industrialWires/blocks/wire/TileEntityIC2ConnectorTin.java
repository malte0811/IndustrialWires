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
package malte0811.industrialWires.blocks.wire;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.AbstractConnection;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.wires.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.util.Utils;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import malte0811.industrialWires.IIC2Connector;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.IBlockBoundsIW;
import malte0811.industrialWires.wires.IC2Wiretype;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Optional;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import reborncore.api.power.IEnergyInterfaceTile;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Optional.InterfaceList({
		@Optional.Interface(iface = "ic2.api.energy.tile.IEnergySource", modid = "ic2"),
		@Optional.Interface(iface = "ic2.api.energy.tile.IEnergySink", modid = "ic2")
})
public class TileEntityIC2ConnectorTin extends TileEntityImmersiveConnectable implements IEnergySource, IEnergySink, IDirectionalTile,
		ITickable, IIC2Connector, IBlockBoundsIW {
	private static final double MIN_ENERGY = 1e-5;
	EnumFacing facing = EnumFacing.NORTH;
	boolean relay;
	private boolean first = true;
	//IC2 net to IE net buffer
	double inBuffer = 0;
	//IE net to IC2 net buffer
	double outBuffer = 0;
	double maxStored = IC2Wiretype.IC2_TYPES[0].getTransferRate() / 8;
	int tier = 1;

	TileEntityIC2ConnectorTin(boolean rel) {
		relay = rel;
	}

	public TileEntityIC2ConnectorTin() {
	}

	@Override
	public void update() {
		if (first) {
			if (!world.isRemote&& IndustrialWires.hasIC2)
				MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
			first = false;
		}
		if (!world.isRemote) {
			if (inBuffer > 2*MIN_ENERGY) {
				transferPower();
			}
			if (outBuffer>.1&&IndustrialWires.hasTechReborn) {
				TileEntity output = Utils.getExistingTileEntity(world, pos.offset(facing));
				if (output instanceof IEnergyInterfaceTile) {
					IEnergyInterfaceTile out = (IEnergyInterfaceTile) output;
					if (out.canAcceptEnergy(facing.getOpposite())) {
						outBuffer -= out.addEnergy(outBuffer);
					}
				}
			}
		}
	}

	private void transferPower() {
		Set<AbstractConnection> conns = new HashSet<>(ImmersiveNetHandler.INSTANCE.getIndirectEnergyConnections(pos, world));
		Map<AbstractConnection, Pair<IIC2Connector, Double>> maxOutputs = new HashMap<>();
		double sum = 0;
		for (AbstractConnection c : conns) {
			IImmersiveConnectable iic = ApiUtils.toIIC(c.end, world);
			if (iic instanceof IIC2Connector) {
				double tmp = inBuffer - ((IIC2Connector) iic).insertEnergy(inBuffer, true);
				if (tmp > MIN_ENERGY) {
					maxOutputs.put(c, new ImmutablePair<>((IIC2Connector) iic, tmp));
					sum += tmp;
				}
			}
		}
		if (sum < MIN_ENERGY) {
			return;
		}
		final double oldInBuf = inBuffer;
		HashMap<Connection, Integer> transferedPerConn = ImmersiveNetHandler.INSTANCE.getTransferedRates(world.provider.getDimension());
		for (AbstractConnection c : maxOutputs.keySet()) {
			Pair<IIC2Connector, Double> p = maxOutputs.get(c);
			double out = oldInBuf * p.getRight() / sum;
			double loss = getAverageLossRate(c);
			double inserted = out - p.getLeft().insertEnergy(out - loss, false);
			this.inBuffer -= inserted;
			float intermediaryLoss = 0;
			HashSet<IImmersiveConnectable> passedConnectors = new HashSet<>();
			double energyAtConn = inserted + loss;
			for (Connection sub : c.subConnections) {
				int transferredPerCon = transferedPerConn.getOrDefault(sub, 0);
				energyAtConn -= sub.cableType.getLossRatio() * sub.length;
				transferedPerConn.put(sub, (int) (transferredPerCon + energyAtConn));
				IImmersiveConnectable subStart = ApiUtils.toIIC(sub.start, world);
				IImmersiveConnectable subEnd = ApiUtils.toIIC(sub.end, world);
				if (subStart != null && passedConnectors.add(subStart))
					subStart.onEnergyPassthrough((int) (inserted - inserted * intermediaryLoss));
				if (subEnd != null && passedConnectors.add(subEnd))
					subEnd.onEnergyPassthrough((int) (inserted - inserted * intermediaryLoss));
			}
		}
	}

	private double getAverageLossRate(AbstractConnection conn) {
		double f = 0;
		for (Connection c : conn.subConnections) {
			f += c.length * c.cableType.getLossRatio();
		}
		return f;
	}

	//Input through the IE net
	@Override
	public double insertEnergy(double eu, boolean simulate) {
		final double insert = simulate?Math.min(Math.max(0, eu-outBuffer), maxStored-outBuffer):eu;
		if (!simulate) {
			outBuffer += insert;
		}
		return eu - insert;
	}

	@Override
	public void invalidate() {
		if (!world.isRemote && !first)
			MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
		first = true;
		super.invalidate();
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		if (!world.isRemote && !first)
			MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
		first = true;
	}

	@Override
	public Vec3d getRaytraceOffset(IImmersiveConnectable link) {
		EnumFacing side = facing.getOpposite();
		return new Vec3d(.5 + side.getFrontOffsetX() * .0625, .5 + side.getFrontOffsetY() * .0625, .5 + side.getFrontOffsetZ() * .0625);
	}

	@Override
	public Vec3d getConnectionOffset(Connection con) {
		EnumFacing side = facing.getOpposite();
		double conRadius = con.cableType.getRenderDiameter() / 2;
		return new Vec3d(.5 - conRadius * side.getFrontOffsetX(), .5 - conRadius * side.getFrontOffsetY(), .5 - conRadius * side.getFrontOffsetZ());
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
	public boolean canConnectCable(WireType cableType, TargetingInfo target) {
		return (limitType == null || (this.isRelay() && limitType == cableType)) && canConnect(cableType);
	}

	public boolean canConnect(WireType t) {
		return t == IC2Wiretype.IC2_TYPES[0];
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
		double ret = maxStored;
		if (ret-inBuffer < MIN_ENERGY)
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
		double consume = Math.min(Math.max(0, amount-inBuffer), maxStored-inBuffer);
		if (consume>0) {
			inBuffer += consume;
		}
		return amount-consume-amount*1e-9;
	}

	@Override
	@Optional.Method(modid="ic2")
	public double getOfferedEnergy() {
		return outBuffer;
	}

	@Override
	@Optional.Method(modid="ic2")
	public void drawEnergy(double amount) {
		outBuffer -= amount;
		markDirty();
	}

	@Override
	@Optional.Method(modid="ic2")
	public int getSourceTier() {
		return tier;
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		super.readCustomNBT(nbt, descPacket);
		facing = EnumFacing.getFront(nbt.getInteger("facing"));
		relay = nbt.getBoolean("relay");
		inBuffer = nbt.getDouble("inBuffer");
		outBuffer = nbt.getDouble("outBuffer");
		if (nbt.hasKey("maxToNet")) {
			inBuffer = Math.min(nbt.getDouble("maxToNet"), inBuffer);
		}
		if (nbt.hasKey("maxToMachine")) {
			outBuffer = Math.min(nbt.getDouble("maxToMachine"), outBuffer);
		}
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		super.writeCustomNBT(nbt, descPacket);
		nbt.setInteger("facing", facing.getIndex());
		nbt.setBoolean("relay", relay);
		nbt.setDouble("inBuffer", inBuffer);
		nbt.setDouble("outBuffer", outBuffer);
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
		float length = this instanceof TileEntityIC2ConnectorHV ? (relay ? .875f : .75f) : this instanceof TileEntityIC2ConnectorGold ? .5625f : .5f;
		float wMin = .3125f;
		float wMax = .6875f;
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

	/*
	 * regarding equals+hashCode
	 * TE's are considered equal if they have the same pos+dimension id
	 * This is necessary to work around a weird bug causing a lot of log spam (100GB and above are well possible).
	 * For further information see #1 (https://github.com/malte0811/IndustrialWires/issues/1)
	 */
	@Override
	public int hashCode() {
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
}

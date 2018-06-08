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
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import malte0811.industrialWires.IIC2Connector;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.IBlockBoundsIW;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Optional;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static malte0811.industrialWires.wires.IC2Wiretype.IC2_TIN_CAT;
import static malte0811.industrialWires.wires.IC2Wiretype.TIN;

@Optional.InterfaceList({
		@Optional.Interface(iface = "ic2.api.energy.tile.IEnergySource", modid = "ic2"),
		@Optional.Interface(iface = "ic2.api.energy.tile.IEnergySink", modid = "ic2")
})
public class TileEntityIC2ConnectorTin extends TileEntityImmersiveConnectable implements IEnergySource, IEnergySink, IDirectionalTile,
		ITickable, IIC2Connector, IBlockBoundsIW {
	private static final double EPS = .1;
	EnumFacing facing = EnumFacing.NORTH;
	boolean relay;
	private boolean first = true;
	//IC2 net to IE net buffer
	double inBuffer = 0;
	double maxToNet = 0;
	private double inputInTick = 0;
	//IE net to IC2 net buffer
	double outBuffer = 0;
	double maxToMachine = 0;
	double maxStored = TIN.getTransferRate() / TIN.getFactor();
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
			ImmersiveNetHandler.INSTANCE.onTEValidated(this);
			first = false;
		}
		if (inBuffer < maxToNet) {
			maxToNet = inBuffer;
		}
		if (inputInTick>maxToNet) {
			maxToNet = inputInTick;
		}
		inputInTick = 0;

		if (!world.isRemote) {
			if (inBuffer > EPS) {
				transferPower();
			}
			if (inBuffer>EPS) {
				notifyAvailableEnergy(inBuffer);
			}
		}
	}

	private void transferPower() {
		Set<AbstractConnection> conns = ImmersiveNetHandler.INSTANCE.getIndirectEnergyConnections(pos, world, true);
		Map<AbstractConnection, Pair<IIC2Connector, Double>> maxOutputs = new HashMap<>();
		double outputMax = Math.min(inBuffer, maxToNet);
		double sum = 0;
		for (AbstractConnection c : conns) {
			if (c.isEnergyOutput) {
				IImmersiveConnectable iic = ApiUtils.toIIC(c.end, world);
				if (iic instanceof IIC2Connector) {
					double extract =
							outputMax - ((IIC2Connector) iic).insertEnergy(outputMax, true);
					if (extract > EPS) {
						maxOutputs.put(c, new ImmutablePair<>((IIC2Connector) iic, extract));
						sum += extract;
					}
				}
			}
		}
		if (sum > EPS) {
			HashMap<Connection, Integer> transferedPerConn = ImmersiveNetHandler.INSTANCE.getTransferedRates(world.provider.getDimension());
			for (Map.Entry<AbstractConnection, Pair<IIC2Connector, Double>> entry : maxOutputs.entrySet()) {
				Pair<IIC2Connector, Double> p = entry.getValue();
				AbstractConnection c = entry.getKey();
				double out = outputMax * p.getRight() / sum;
				double loss = getAverageLossRate(c);
				out = Math.min(out, inBuffer-loss);
				if (out<=0)
					continue;
				double inserted = out - p.getLeft().insertEnergy(out, false);
				double energyAtConn = inserted + loss;
				inBuffer -= energyAtConn;
				float intermediaryLoss = 0;
				HashSet<IImmersiveConnectable> passedConnectors = new HashSet<>();
				for (Connection sub : c.subConnections) {
					int transferredPerCon = transferedPerConn.getOrDefault(sub, 0);
					energyAtConn -= sub.cableType.getLossRatio() * sub.length;
					ImmersiveNetHandler.INSTANCE.getTransferedRates(world.provider.getDimension()).put(sub, (int) (transferredPerCon + energyAtConn));
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
		Consumer<Float> extract = (energy)->{
			inBuffer -= energy+loss;
		};
		return new ImmutablePair<>(max, extract);
	}

	@Override
	public float getDamageAmount(Entity e, Connection c) {
		return (float) Math.ceil(super.getDamageAmount(e, c));
	}

	private double getAverageLossRate(AbstractConnection conn) {
		double f = 0;
		for (Connection c : conn.subConnections) {
			f += c.length * c.cableType.getLossRatio();
		}
		return f;
	}

	//Input through the net
	@Override
	public double insertEnergy(double eu, boolean simulate) {
		final double insert = Math.min(maxStored - outBuffer, eu);
		if (insert > 0) {
			if (outBuffer < maxToMachine) {
				maxToMachine = outBuffer;
			}
			if (eu > maxToMachine) {
				maxToMachine = eu;
			}
		}
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
	public boolean canConnectCable(WireType cableType, TargetingInfo target, Vec3i offset) {
		return (limitType == null || (this.isRelay() && WireApi.canMix(cableType, limitType))) && canConnect(cableType);
	}

	public boolean canConnect(WireType t) {
		return IC2_TIN_CAT.equals(t.getCategory());
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
		double ret = maxStored + .5 - inBuffer;
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
		if (inBuffer < maxStored) {
			addToIn(amount);
			markDirty();
			return 0;
		}
		return amount;
	}

	@Override
	@Optional.Method(modid="ic2")
	public double getOfferedEnergy() {
		return Math.min(maxToMachine, outBuffer);
	}

	@Override
	@Optional.Method(modid="ic2")
	public void drawEnergy(double amount) {
		outBuffer -= amount;
		markDirty();
	}

	@Nullable
	protected Pair<Float,Consumer<Float>> getOwnEnergy()
	{
		if (isRelay())
			return null;
		return new ImmutablePair<>((float)inBuffer, (d)->inBuffer -= d);
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

	private void addToIn(double amount) {
		inBuffer += amount;
		inputInTick += amount;
		notifyAvailableEnergy(amount);
	}

	@Override
	public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
		super.readCustomNBT(nbt, descPacket);
		facing = EnumFacing.getFront(nbt.getInteger("facing"));
		relay = nbt.getBoolean("relay");
		inBuffer = nbt.getDouble("inBuffer");
		outBuffer = nbt.getDouble("outBuffer");
		if (nbt.hasKey("maxToNet")) {
			maxToNet = nbt.getDouble("maxToNet");
		} else {
			maxToNet = inBuffer;
		}
		if (nbt.hasKey("maxToMachine")) {
			maxToMachine = nbt.getDouble("maxToMachine");
		} else {
			maxToMachine = outBuffer;
		}
	}

	@Override
	public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
		super.writeCustomNBT(nbt, descPacket);
		nbt.setInteger("facing", facing.getIndex());
		nbt.setBoolean("relay", relay);
		nbt.setDouble("inBuffer", inBuffer);
		nbt.setDouble("outBuffer", outBuffer);
		nbt.setDouble("maxToNet", maxToNet);
		nbt.setDouble("maxToMachine", maxToMachine);
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
}

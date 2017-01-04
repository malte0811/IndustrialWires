/*******************************************************************************
 * This file is part of Industrial Wires.
 * Copyright (C) 2016 malte0811
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
 *******************************************************************************/
package malte0811.industrialWires.blocks.wire;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.AbstractConnection;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.wires.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import malte0811.industrialWires.IIC2Connector;
import malte0811.industrialWires.wires.IC2Wiretype;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;

public class TileEntityIC2ConnectorTin extends TileEntityImmersiveConnectable implements IEnergySource, IEnergySink, IDirectionalTile, ITickable, IIC2Connector, IBlockBounds {
	EnumFacing f = EnumFacing.NORTH;
	boolean relay;
	boolean first = true;
	//IC2 net to IE net buffer
	double inBuffer = 0;
	double maxToNet = 0;
	//IE net to IC2 net buffer
	double outBuffer = 0;
	double maxToMachine = 0;
	double maxStored = IC2Wiretype.IC2_TYPES[0].getTransferRate()/8;
	int tier = 1;
	public TileEntityIC2ConnectorTin(boolean rel) {
		relay = rel;
	}
	public TileEntityIC2ConnectorTin() {}
	@Override
	public void update() {
		if (first) {
			if (!worldObj.isRemote)
				MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
			first = false;
		}
		if (!worldObj.isRemote&&inBuffer>.1)
			transferPower();
	}
	public void transferPower() {
		Set<AbstractConnection> conns = new HashSet<>(ImmersiveNetHandler.INSTANCE.getIndirectEnergyConnections(pos, worldObj));
		Map<AbstractConnection, Pair<IIC2Connector, Double>> maxOutputs = new HashMap<>();
		double outputMax = Math.min(inBuffer, maxToNet);
		double sum = 0;
		for (AbstractConnection c:conns) {
			IImmersiveConnectable iic = ApiUtils.toIIC(c.end, worldObj);
			if (iic instanceof IIC2Connector) {
				double tmp = inBuffer-((IIC2Connector)iic).insertEnergy(outputMax, true);
				if (tmp>.00000001) {
					maxOutputs.put(c, new ImmutablePair<>((IIC2Connector)iic, tmp));
					sum+=tmp;
				}
			}
		}
		if (sum<.0001) {
			return;
		}
		final double oldInBuf = outputMax;
		HashMap<Connection, Integer> transferedPerConn = ImmersiveNetHandler.INSTANCE.getTransferedRates(worldObj.provider.getDimension());
		for (AbstractConnection c:maxOutputs.keySet()) {
			Pair<IIC2Connector, Double> p = maxOutputs.get(c);
			double out = oldInBuf*p.getRight()/sum;
			double loss = getAverageLossRate(c);
			double inserted = out-p.getLeft().insertEnergy(out-loss, false);
			inBuffer-=inserted;
			float intermediaryLoss = 0;
			HashSet<IImmersiveConnectable> passedConnectors = new HashSet<>();
			double energyAtConn = inserted+loss;
			for(Connection sub : c.subConnections)
			{
				int transferredPerCon = transferedPerConn.containsKey(sub)?transferedPerConn.get(sub):0;
				energyAtConn-=sub.cableType.getLossRatio()*sub.length;
				ImmersiveNetHandler.INSTANCE.getTransferedRates(worldObj.provider.getDimension()).put(sub,(int)(transferredPerCon+energyAtConn));
				IImmersiveConnectable subStart = ApiUtils.toIIC(sub.start,worldObj);
				IImmersiveConnectable subEnd = ApiUtils.toIIC(sub.end,worldObj);
				if(subStart!=null && passedConnectors.add(subStart))
					subStart.onEnergyPassthrough((int)(inserted-inserted*intermediaryLoss));
				if(subEnd!=null && passedConnectors.add(subEnd))
					subEnd.onEnergyPassthrough((int)(inserted-inserted*intermediaryLoss));
			}
		}
	}
	public double getAverageLossRate(AbstractConnection conn) {
		double f = 0;
		for(Connection c : conn.subConnections) {
			f += c.length*c.cableType.getLossRatio();
		}
		return f;
	}
	//Input through the net
	@Override
	public double insertEnergy(double eu, boolean simulate) {
		final double insert = Math.min(maxStored-outBuffer, eu);
		if (insert>0) {
			if (outBuffer<maxToMachine) {
				maxToMachine = outBuffer;
			}
			if (eu>maxToMachine) {
				maxToMachine = eu;
			}
		}
		if (!simulate) {
			outBuffer+=insert;
		}
		return eu-insert;
	}
	@Override
	public void invalidate() {
		if (!worldObj.isRemote&&!first)
			MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
		first = true;
		super.invalidate();
	}
	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		if (!worldObj.isRemote&&!first)
			MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
		first = true;
	}

	@Override
	public Vec3d getRaytraceOffset(IImmersiveConnectable link) {
		EnumFacing side = f.getOpposite();
		return new Vec3d(.5+side.getFrontOffsetX()*.0625, .5+side.getFrontOffsetY()*.0625, .5+side.getFrontOffsetZ()*.0625);
	}
	@Override
	public Vec3d getConnectionOffset(Connection con) {
		EnumFacing side = f.getOpposite();
		double conRadius = con.cableType.getRenderDiameter()/2;
		return new Vec3d(.5-conRadius*side.getFrontOffsetX(), .5-conRadius*side.getFrontOffsetY(), .5-conRadius*side.getFrontOffsetZ());
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
		return (limitType==null||(this.isRelay() && limitType==cableType))&&canConnect(cableType);
	}
	public boolean canConnect(WireType t) {
		return t==IC2Wiretype.IC2_TYPES[0];
	}

	@Override
	protected boolean isRelay() {
		return relay;
	}

	@Override
	public boolean emitsEnergyTo(IEnergyAcceptor receiver, EnumFacing side) {
		return !relay&&side==f;
	}

	@Override
	public boolean acceptsEnergyFrom(IEnergyEmitter emitter, EnumFacing side) {
		return !relay&&side==f;
	}

	@Override
	public double getDemandedEnergy() {
		double ret = maxStored-inBuffer;
		if (ret<.1)
			ret = 0;
		return ret;
	}

	@Override
	public int getSinkTier() {
		return tier;
	}

	@Override
	public double injectEnergy(EnumFacing directionFrom, double amount, double voltage) {
		if (inBuffer<maxStored) {
			if (inBuffer<maxToNet) {
				maxToNet = inBuffer;
			}
			inBuffer += amount;
			if (amount>maxToNet) {
				maxToNet = amount;
			}
			markDirty();
			return 0;
		}
		return amount;
	}

	@Override
	public double getOfferedEnergy() {
		return Math.min(maxToMachine, outBuffer);
	}

	@Override
	public void drawEnergy(double amount) {
		outBuffer -= amount;
		markDirty();
	}

	@Override
	public int getSourceTier() {
		return tier;
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		super.readCustomNBT(nbt, descPacket);
		f = EnumFacing.getFront(nbt.getInteger("facing"));
		relay = nbt.getBoolean("relay");
		inBuffer = nbt.getDouble("inBuffer");
		outBuffer = nbt.getDouble("outBuffer");
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		super.writeCustomNBT(nbt, descPacket);
		nbt.setInteger("facing", f.getIndex());
		nbt.setBoolean("relay", relay);
		nbt.setDouble("inBuffer", inBuffer);
		nbt.setDouble("outBuffer", outBuffer);
	}

	@Override
	public EnumFacing getFacing() {
		return f;
	}

	@Override
	public void setFacing(EnumFacing facing) {
		f = facing;
	}

	@Override
	public int getFacingLimitation() {
		return 0;
	}

	@Override
	public boolean mirrorFacingOnPlacement(EntityLivingBase placer) {
		return true;
	}

	@Override
	public boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity) {
		return false;
	}
	@Override
	public float[] getBlockBounds() {
		float length = this instanceof TileEntityIC2ConnectorHV?(relay?.875f:.75f): this instanceof TileEntityIC2ConnectorGold?.5625f: .5f;
		float wMin = .3125f;
		float wMax = .6875f;
		switch(f.getOpposite() )
		{
		case UP:
			return new float[]{wMin,0,wMin,  wMax,length,wMax};
		case DOWN:
			return new float[]{wMin,1-length,wMin,  wMax,1,wMax};
		case SOUTH:
			return new float[]{wMin,wMin,0,  wMax,wMax,length};
		case NORTH:
			return new float[]{wMin,wMin,1-length,  wMax,wMax,1};
		case EAST:
			return new float[]{0,wMin,wMin,  length,wMax,wMax};
		case WEST:
			return new float[]{1-length,wMin,wMin,  1,wMax,wMax};
		}
		return new float[]{0,0,0,1,1,1};
	}
	/*
	 * regarding equals+hashCode
	 * TE's are considered equal if they have the same pos+dimension id
	 * This is necessary to work around a weird bug causing a lot of log spam (100GB and above are well possible).
	 * For further information see #1 (https://github.com/malte0811/IndustrialWires/issues/1)
	 */
	@Override
	public int hashCode() {
		int ret = worldObj.provider.getDimension();
		ret = 31*ret+pos.hashCode();
		return ret;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj==this) {
			return true;
		}
		if (!(obj instanceof TileEntityIC2ConnectorTin)) {
			return false;
		}
		if (obj.getClass()!=getClass()) {
			return false;
		}
		TileEntityIC2ConnectorTin te = (TileEntityIC2ConnectorTin) obj;
		if (!te.pos.equals(pos)) {
			return false;
		}
		if (te.worldObj.provider.getDimension()!=worldObj.provider.getDimension()) {
			return false;
		}
		return true;
	}
}

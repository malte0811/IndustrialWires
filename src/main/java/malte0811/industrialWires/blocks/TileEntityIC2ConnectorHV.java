package malte0811.industrialWires.blocks;

import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import malte0811.industrialWires.wires.IC2Wiretype;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

public class TileEntityIC2ConnectorHV extends TileEntityIC2ConnectorTin {

	public TileEntityIC2ConnectorHV(boolean rel) {
		super(rel);
	}

	public TileEntityIC2ConnectorHV() {} 

	{
		tier = 4;
		maxStored = IC2Wiretype.IC2_TYPES[3].getTransferRate()/8;
	}
	@Override
	public boolean canConnect(WireType t) {
		return t==IC2Wiretype.IC2_TYPES[3];
	}

	@Override
	public Vec3d getRaytraceOffset(IImmersiveConnectable link) {
		EnumFacing side = f.getOpposite();
		if (relay) {
			return new Vec3d(.5+side.getFrontOffsetX()*.4375, .5+side.getFrontOffsetY()*.4375, .5+side.getFrontOffsetZ()*.4375);
		} else {
			return new Vec3d(.5+side.getFrontOffsetX()*.3125, .5+side.getFrontOffsetY()*.3125, .5+side.getFrontOffsetZ()*.3125);
		}
	}
	@Override
	public Vec3d getConnectionOffset(Connection con) {
		EnumFacing side = f.getOpposite();
		double conRadius = con.cableType.getRenderDiameter()/2;
		if (relay) {
			return new Vec3d(.5+side.getFrontOffsetX()*(.375-conRadius), .5+side.getFrontOffsetY()*(.375-conRadius), .5+side.getFrontOffsetZ()*(.375-conRadius));
		} else {
			return new Vec3d(.5+side.getFrontOffsetX()*(.25-conRadius), .5+side.getFrontOffsetY()*(.25-conRadius), .5+side.getFrontOffsetZ()*(.25-conRadius));
		}
	}
}

package malte0811.industrialWires.blocks;

import blusunrize.immersiveengineering.api.energy.wires.WireType;
import malte0811.industrialWires.wires.IC2Wiretype;

public class TileEntityIC2ConnectorGlass extends TileEntityIC2ConnectorHV {
	public TileEntityIC2ConnectorGlass(boolean rel) {
		super(rel);
	}

	public TileEntityIC2ConnectorGlass() {} 

	{
		tier = 5;
		maxStored = IC2Wiretype.IC2_TYPES[4].getTransferRate()/8;
	}
	@Override
	public boolean canConnect(WireType t) {
		return t==IC2Wiretype.IC2_TYPES[4];
	}
}

package malte0811.industrialWires.blocks;

import blusunrize.immersiveengineering.api.energy.wires.WireType;
import malte0811.industrialWires.wires.IC2Wiretype;

public class TileEntityIC2ConnectorCopper extends TileEntityIC2ConnectorTin {

	public TileEntityIC2ConnectorCopper(boolean rel) {
		super(rel);
	}

	public TileEntityIC2ConnectorCopper() {}
	
	{
		tier = 2;
		maxStored = IC2Wiretype.IC2_TYPES[1].getTransferRate()/8;
	}
	@Override
	public boolean canConnect(WireType t) {
		return t==IC2Wiretype.IC2_TYPES[1];
	}

}

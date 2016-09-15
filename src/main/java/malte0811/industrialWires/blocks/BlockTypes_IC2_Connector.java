package malte0811.industrialWires.blocks;

import blusunrize.immersiveengineering.common.blocks.BlockIEBase.IBlockEnum;

public enum BlockTypes_IC2_Connector implements IBlockEnum {
	TIN_CONN,
	TIN_RELAY,
	COPPER_CONN,
	COPPER_RELAY,
	GOLD_CONN,
	GOLD_RELAY,
	HV_CONN,
	HV_RELAY;
	@Override
	public String getName() {
		return toString().toLowerCase();
	}

	@Override
	public int getMeta() {
		return ordinal();
	}

	@Override
	public boolean listForCreative() {
		return true;
	}

}

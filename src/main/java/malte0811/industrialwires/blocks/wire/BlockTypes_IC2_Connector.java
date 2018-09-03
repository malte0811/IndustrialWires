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
package malte0811.industrialwires.blocks.wire;

import blusunrize.immersiveengineering.common.blocks.BlockIEBase.IBlockEnum;

public enum BlockTypes_IC2_Connector implements IBlockEnum {
	TIN_CONN,
	TIN_RELAY,
	COPPER_CONN,
	COPPER_RELAY,
	GOLD_CONN,
	GOLD_RELAY,
	HV_CONN,
	HV_RELAY,
	GLASS_CONN,
	GLASS_RELAY;

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

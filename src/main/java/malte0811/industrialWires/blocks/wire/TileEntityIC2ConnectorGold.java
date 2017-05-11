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

import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import malte0811.industrialWires.wires.IC2Wiretype;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

public class TileEntityIC2ConnectorGold extends TileEntityIC2ConnectorTin {

	public TileEntityIC2ConnectorGold(boolean rel) {
		super(rel);
	}

	public TileEntityIC2ConnectorGold() {
	}

	{
		tier = 3;
		maxStored = IC2Wiretype.IC2_TYPES[2].getTransferRate() / 8;
	}

	@Override
	public boolean canConnect(WireType t) {
		return t == IC2Wiretype.IC2_TYPES[2];
	}

	@Override
	public Vec3d getRaytraceOffset(IImmersiveConnectable link) {
		EnumFacing side = f.getOpposite();
		return new Vec3d(.5 + side.getFrontOffsetX() * .125, .5 + side.getFrontOffsetY() * .125, .5 + side.getFrontOffsetZ() * .125);
	}

	@Override
	public Vec3d getConnectionOffset(Connection con) {
		EnumFacing side = f.getOpposite();
		double conRadius = con.cableType.getRenderDiameter() / 2;
		return new Vec3d(.5 + side.getFrontOffsetX() * (.0625 - conRadius), .5 + side.getFrontOffsetY() * (.0625 - conRadius), .5 + side.getFrontOffsetZ() * (.0625 - conRadius));
	}
}

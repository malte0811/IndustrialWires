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

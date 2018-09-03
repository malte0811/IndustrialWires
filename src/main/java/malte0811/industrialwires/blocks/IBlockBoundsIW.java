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

package malte0811.industrialwires.blocks;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import malte0811.industrialwires.util.MiscUtils;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

public interface IBlockBoundsIW {
	AxisAlignedBB getBoundingBox();

	interface IBlockBoundsDirectional extends IBlockBoundsIW, IEBlockInterfaces.IDirectionalTile {

		@Override
		default AxisAlignedBB getBoundingBox() {
			EnumFacing dir = getFacing();
			Matrix4 mat = new Matrix4();
			mat.translate(.5, 0, .5);
			mat.rotate((-dir.getHorizontalAngle()+180)*Math.PI/180, 0, 1, 0);
			mat.translate(-.5, 0, -.5);
			return MiscUtils.apply(mat, getBoundingBoxNoRot());
		}

		AxisAlignedBB getBoundingBoxNoRot();
	}
}

/*
 * This file is part of Industrial Wires.
 * Copyright (C) 2016-2017 malte0811
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

package malte0811.industrialWires.util;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LocalSidedWorld {
	private World world;
	private BlockPos origin;
	private EnumFacing facing;
	private boolean mirror;
	public LocalSidedWorld(World world, BlockPos origin, EnumFacing facing, boolean mirror) {
		this.world = world;
		this.facing = facing;
		this.mirror = mirror;
		this.origin = origin;
	}

	public World getWorld() {
		return world;
	}

	public void setFacing(EnumFacing facing) {
		this.facing = facing;
	}

	public void setOrigin(BlockPos origin) {
		this.origin = origin;
	}

	public void setMirror(boolean mirror) {
		this.mirror = mirror;
	}

	public BlockPos getOrigin() {
		return origin;
	}

	public IBlockState getBlockState(BlockPos pos) {
		return world.getBlockState(getRealPos(pos));
	}

	public TileEntity getTileEntity(BlockPos pos) {
		return world.getTileEntity(getRealPos(pos));
	}

	public boolean setBlockState(BlockPos pos, IBlockState setTo) {
		return world.setBlockState(getRealPos(pos), setTo);
	}

	public boolean isAir(BlockPos pos) {
		return world.isAirBlock(getRealPos(pos));
	}

	public BlockPos getRealPos(BlockPos relative) {
		return MiscUtils.offset(origin, facing, mirror, relative);
	}

	public void setWorld(World world) {
		this.world = world;
	}
}

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

package malte0811.industrialWires.util;

import malte0811.industrialWires.IndustrialWires;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class LocalSidedWorld {
	private World world;
	private BlockPos origin;
	private EnumFacing facing;
	private boolean mirror;
	public boolean isRemote;

	public LocalSidedWorld(World world, BlockPos origin, EnumFacing facing, boolean mirror) {
		this.world = world;
		this.facing = facing;
		this.mirror = mirror;
		this.origin = origin;
		if (world!=null) {
			isRemote = world.isRemote;
		}
	}

	public IBlockState getBlockState(BlockPos pos) {
		return world.getBlockState(getRealPos(pos));
	}

	public TileEntity getTileEntity(BlockPos pos) {
		return world.getTileEntity(getRealPos(pos));
	}

	public boolean isAir(BlockPos pos) {
		return world.isAirBlock(getRealPos(pos));
	}

	public boolean setBlockState(BlockPos pos, IBlockState setTo) {
		return world.setBlockState(getRealPos(pos), setTo);
	}

	public void spawnEntity(Entity e) {
		Vec3d pos = getRealPos(e.getPositionVector());
		IndustrialWires.logger.info("Spawning at {} (relative), {} (absolute)", e.getPositionVector(), pos);
		e.setPosition(pos.x, pos.y, pos.z);
		Vec3d motion = getRealDirection(new Vec3d(e.motionX, e.motionY, e.motionZ));
		e.motionX = motion.x;
		e.motionY = motion.y;
		e.motionZ = motion.z;
		world.spawnEntity(e);
	}

	public void setTileEntity(BlockPos pos, TileEntity setTo) {
		world.setTileEntity(getRealPos(pos), setTo);
	}


	public void markForUpdate(BlockPos rel) {
		BlockPos pos = getRealPos(rel);
		IBlockState state = world.getBlockState(pos);
		world.notifyBlockUpdate(pos, state, state, 3);
		world.notifyNeighborsOfStateChange(pos, state.getBlock(), true);
	}

	//Transformation utils
	public BlockPos getRealPos(BlockPos relative) {
		return MiscUtils.offset(origin, facing, mirror, relative);
	}

	public Vec3d getRealPos(Vec3d relative) {
		return MiscUtils.offset(new Vec3d(origin), facing, mirror, relative);
	}

	public Vec3d getRealDirection(Vec3d dir) {
		return MiscUtils.offset(Vec3d.ZERO, facing, mirror, dir);
	}

	public EnumFacing realToTransformed(@Nullable EnumFacing f) {
		if (f==null||f.getAxis()== EnumFacing.Axis.Y) {
			return f;
		}
		return EnumFacing.getHorizontal(f.getHorizontalIndex()-facing.getHorizontalIndex()+2);
	}

	public EnumFacing transformedToReal(@Nullable EnumFacing f) {
		if (f==null||f.getAxis()== EnumFacing.Axis.Y) {
			return f;
		}
		return EnumFacing.getHorizontal(f.getHorizontalIndex()+facing.getHorizontalIndex()+2);
	}

	//Getters+Setters
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

	public void setWorld(World world) {
		this.world = world;
		if (world!=null) {
			isRemote = world.isRemote;
		}
	}

	public boolean isMirrored() {
		return mirror;
	}

	public EnumFacing getFacing() {
		return facing;
	}
}

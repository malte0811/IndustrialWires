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

package malte0811.industrialWires.util;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;

public final class MiscUtils {
	private MiscUtils() {
	}

	public static Set<ImmersiveNetHandler.Connection> genConnBlockstate(Set<ImmersiveNetHandler.Connection> conns, World world) {
		if (conns == null)
			return ImmutableSet.of();
		Set<ImmersiveNetHandler.Connection> ret = new HashSet<ImmersiveNetHandler.Connection>() {
			@Override
			public boolean equals(Object o) {
				if (o == this)
					return true;
				if (!(o instanceof HashSet))
					return false;
				HashSet<ImmersiveNetHandler.Connection> other = (HashSet<ImmersiveNetHandler.Connection>) o;
				if (other.size() != this.size())
					return false;
				for (ImmersiveNetHandler.Connection c : this)
					if (!other.contains(c))
						return false;
				return true;
			}
		};
		for (ImmersiveNetHandler.Connection c : conns) {
			IImmersiveConnectable end = ApiUtils.toIIC(c.end, world, false);
			if (end == null)
				continue;
			// generate subvertices
			c.getSubVertices(world);
			ret.add(c);
		}

		return ret;
	}

	public static List<BlockPos> discoverLocal(World w, BlockPos here, BiPredicate<BlockPos, Integer> isValid) {
		List<BlockPos> ret = new ArrayList<>();
		List<BlockPos> open = new ArrayList<>();
		open.add(here);
		while (!open.isEmpty()) {
			BlockPos curr = open.get(0);
			ret.add(curr);
			open.remove(0);
			for (EnumFacing f : EnumFacing.VALUES) {
				BlockPos next = curr.offset(f);
				if (!open.contains(next) && !ret.contains(next) && isValid.test(next, ret.size())) {
					open.add(next);
				}
			}
		}
		return ret;
	}

	/**
	 * @param mirror inverts right
	 */
	public static BlockPos offset(BlockPos p, EnumFacing f, boolean mirror, int right, int forward, int up) {
		if (mirror) {
			right *= -1;
		}
		return p.offset(f, forward).offset(f.rotateY(), right).add(0, up, 0);
	}

	/**
	 * Calculates the parameters for offset to generate here from origin
	 * @return right, forward, up
	 */
	public static BlockPos getOffset(BlockPos origin, EnumFacing f, boolean mirror, BlockPos here) {
		int dX = origin.getZ()-here.getZ();
		int dZ = origin.getX()-here.getX();
		int forward = 0;
		int right = 0;
		int up = here.getY()-origin.getY();
		switch (f) {
			case NORTH:
				forward = dZ;
				right = -dX;
				break;
			case SOUTH:
				forward = -dZ;
				right = dX;
				break;
			case WEST:
				right = dZ;
				forward = dX;
				break;
			case EAST:
				right = -dZ;
				forward = -dX;
				break;
		}
		if (mirror) {
			right *= -1;
		}
		return new BlockPos(right, forward, up);
	}
	@Nonnull
	public static AxisAlignedBB apply(@Nonnull Matrix4 mat, @Nonnull AxisAlignedBB in) {
		Vec3d min = new Vec3d(in.minX, in.minY, in.minZ);
		Vec3d max = new Vec3d(in.maxX, in.maxY, in.maxZ);
		min = mat.apply(min);
		max = mat.apply(max);
		return new AxisAlignedBB(min.x, min.y, min.z, max.x, max.y, max.z);
	}

	public static ItemStack getItemStack(IBlockState origState, World w, BlockPos pos) {
		if (origState.getBlock() instanceof IEBlockInterfaces.IIEMetaBlock) {
			int meta = origState.getBlock().getMetaFromState(origState);
			return new ItemStack(origState.getBlock(), 1, meta);
		}
		return origState.getBlock().getPickBlock(origState, null, w, pos, null);
	}

	public static float[] interpolate(double a, float[] cA, double b, float[] cB) {
		float[] ret = new float[cA.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = (float) (a * cA[i] + b * cB[i]);
		}
		return ret;
	}
}

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
import com.google.common.collect.ImmutableSet;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
}

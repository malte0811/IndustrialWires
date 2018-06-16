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

import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static blusunrize.immersiveengineering.api.IEEnums.SideConfig.*;

public class MBSideConfig {
	private static final SideConfig[] CONFIG_VALUES = SideConfig.values();
	public final Map<BlockFace, SideConfig> sides = new HashMap<>();
	

	public MBSideConfig(List<BlockFace> sides) {
		for (BlockFace side:sides) {
			updateConfig(side, INPUT);
		}
	}

	public MBSideConfig(List<BlockFace> sides, NBTTagList nbt) {
		for (int i = 0; i < sides.size(); i++) {
			BlockFace side = sides.get(i);
			updateConfig(side, i<nbt.tagCount()?CONFIG_VALUES[nbt.getIntAt(i)]:INPUT);
		}
	}

	public void updateConfig(BlockFace s, SideConfig state) {
		sides.put(s, state);
	}
	
	public SideConfig getConfigForFace(BlockFace s) {
		if (s.face==null) {
			//Temporary solution, I hope
			for (BlockFace f:sides.keySet()) {
				if (f.offset.equals(s.offset)) {
					return sides.get(f);
				}
			}
		}
		return sides.getOrDefault(s, NONE);
	}
	
	public void cycleSide(BlockFace s) {
		updateConfig(s, next(getConfigForFace(s)));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		MBSideConfig that = (MBSideConfig) o;

		return sides.equals(that.sides);
	}

	@Override
	public int hashCode() {
		return sides.hashCode();
	}

	@Override
	public String toString() {
		return sides.toString();
	}

	public boolean isValid(BlockFace s) {
		return sides.containsKey(s);
	}

	public NBTTagList toNBT(List<BlockFace> order) {
		NBTTagList ret = new NBTTagList();
		for (BlockFace f:order) {
			ret.appendTag(new NBTTagInt(getConfigForFace(f).ordinal()));
		}
		return ret;
	}

	public MBSideConfig copy() {
		MBSideConfig ret = new MBSideConfig(ImmutableList.of());
		ret.sides.putAll(sides);
		return ret;
	}

	public static class BlockFace {
		@Nonnull
		public final BlockPos offset;
		@Nullable
		public final EnumFacing face;

		public BlockFace(@Nonnull BlockPos offset, @Nullable EnumFacing face) {
			this.offset = offset;
			this.face = face;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			BlockFace blockFace = (BlockFace) o;

			if (!offset.equals(blockFace.offset)) return false;
			return face == blockFace.face;
		}

		@Override
		public int hashCode() {
			int result = offset.hashCode();
			result = 31 * result + (face != null ? face.hashCode() : 0);
			return result;
		}
	}
}


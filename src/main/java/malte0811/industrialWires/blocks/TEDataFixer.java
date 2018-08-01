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

package malte0811.industrialWires.blocks;

import malte0811.industrialWires.IndustrialWires;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.IFixableData;

import javax.annotation.Nonnull;

public class TEDataFixer implements IFixableData {
	private static final String PREFIX = "minecraft:"+IndustrialWires.MODID;
	private static final int PREFIX_LEN = PREFIX.length();
	@Nonnull
	@Override
	public NBTTagCompound fixTagCompound(@Nonnull NBTTagCompound compound) {
		String id = compound.getString("id");
		if (id.startsWith(PREFIX)) {
			compound.setString("id", IndustrialWires.MODID+":"+id.substring(PREFIX_LEN));
		}
		return compound;
	}

	@Override
	public int getFixVersion() {
		return 0;
	}
}

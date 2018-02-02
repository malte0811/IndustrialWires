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

package malte0811.industrialWires.converter;

import blusunrize.immersiveengineering.common.util.Utils;
import malte0811.industrialWires.util.LocalSidedWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.oredict.OreDictionary;

public enum Material {
	//TODO max speed
	COPPER(8.96, 1e3),
	ALUMINUM(2.7, 1e3),
	LEAD(11.34, 1e3),
	SILVER(10.49, 1e3),
	NICKEL(8.908, 1e3),
	GOLD(19.3, 1e3),
	URANIUM(19.1, 1e3),// This is a bit silly. But why not.
	CONSTANTAN(8.885, 1e3),
	ELECTRUM((SILVER.density + GOLD.density) / 2, 1e3),
	STEEL(7.874, 1e4),
	IRON(7.874, 1e3);
	//in kg/m^3
	public double density;
	public double maxSpeed;

	//density as parameter: g/cm^3
	Material(double density, double maxSpeed) {
		this.density = density*1e3;
		this.maxSpeed = maxSpeed;
	}

	public boolean matchesBlock(ItemStack block, String prefix) {
		int[] ids = OreDictionary.getOreIDs(block);
		for (int i : ids) {
			if (OreDictionary.getOreName(i).equalsIgnoreCase(prefix + oreName())) {
				return true;
			}
		}
		return false;
	}

	private String oreName() {
		return name().substring(0, 1)+name().substring(1).toLowerCase();
	}

	public boolean matchesBlock(LocalSidedWorld w, BlockPos relative, String prefix) {
		return Utils.isOreBlockAt(w.getWorld(), w.getRealPos(relative), prefix+oreName());
	}
}

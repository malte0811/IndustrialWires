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

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.util.Utils;
import malte0811.industrialWires.util.LocalSidedWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.oredict.OreDictionary;

public enum Material {
	//TODO max speed
	COPPER(8.96, 220, "blocks/storage_copper"),
	ALUMINUM(2.7, 45, "blocks/storage_aluminum"),
	LEAD(11.34, 12, "blocks/storage_lead"),
	SILVER(10.49, 170, "blocks/storage_silver"),
	NICKEL(8.908, 165, "blocks/storage_nickel"),
	GOLD(19.3, 100, new ResourceLocation("minecraft", "blocks/gold_block")),
	URANIUM(19.1, 400, "blocks/storage_uranium_side"),// This is a bit silly. But why not.
	CONSTANTAN(8.885, 600, "blocks/storage_constantan"),
	ELECTRUM((SILVER.density + GOLD.density) / 2, (SILVER.tensileStrength + GOLD.tensileStrength) / 2, "blocks/storage_electrum"),//Tensile strength is a guess ((GOLD+SILVER)/2), if anyone has better data I'll put it in
	STEEL(7.874, 1250, "blocks/storage_steel"),
	IRON(7.874, 350, new ResourceLocation("minecraft", "blocks/iron_block")),
	DIAMOND(3.5, 2800, new ResourceLocation("minecraft", "blocks/diamond_block"));
	//in kg/m^3
	public final double density;
	public final double tensileStrength;
	public final ResourceLocation blockTexture;

	// density as parameter: g/cm^3
	// tStrength: MPa
	// assumes that resource domain is IE
	Material(double density, double tensileStrength, String path) {
		this.density = density*1e3;
		this.tensileStrength = tensileStrength*1e6;
		this.blockTexture = new ResourceLocation(ImmersiveEngineering.MODID, path);
	}
	Material(double density, double tensileStrength, ResourceLocation loc) {
		this.density = density*1e3;
		this.tensileStrength = tensileStrength*1e6;
		this.blockTexture = loc;
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

	public String oreName() {
		return name().substring(0, 1)+name().substring(1).toLowerCase();
	}

	public boolean matchesBlock(LocalSidedWorld w, BlockPos relative, String prefix) {
		return Utils.isOreBlockAt(w.getWorld(), w.getRealPos(relative), prefix+oreName());
	}
}

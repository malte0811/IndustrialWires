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

package malte0811.industrialwires.crafting;

import blusunrize.immersiveengineering.api.ApiUtils;
import malte0811.industrialwires.IEObjects;
import malte0811.industrialwires.IndustrialWires;
import malte0811.industrialwires.compat.Compat;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.oredict.OreIngredient;
import techreborn.api.TechRebornAPI;

import java.util.HashSet;
import java.util.Set;

public final class IC2TRHelper {
	public static Ingredient getStack(String type, String variant) {
		if (type.equals("crafting")&&variant.equals("rubber")) {
			if (ApiUtils.isExistingOreName("itemRubber")) {
				return new OreIngredient("itemRubber");
			}
		}
		Set<ItemStack> stacks = new HashSet<>(Compat.getIC2Item.apply(type, variant));
		if (IndustrialWires.hasTechReborn) {
			switch (type) {
				case "cable":
					stacks.add(getTRCable(variant));
					break;
				case "crafting":
					switch (variant) {
						case "alloy":
							stacks.add(TechRebornAPI.subItemRetriever.getPlateByName("advanced_alloy"));
							break;
					}
					break;
				case "te":
					if (variant.equals("mv_transformer")) {
						stacks.add(new ItemStack(TechRebornAPI.getBlock("MV_TRANSFORMER")));
					}
			}
		}
		stacks.removeIf(ItemStack::isEmpty);
		if (stacks.isEmpty()) {
			switch (type) {
				case "cable":
					return getIECable(variant.substring("type:".length(), variant.indexOf(',')));
				case "crafting":
					switch (variant) {
						case "coil":
							stacks.add(new ItemStack(IEObjects.blockMetalDecoration0));
							break;
						case "alloy":
							return new OreIngredient("plateConstantan");
						case "electric_motor":
							stacks.add(new ItemStack(IEObjects.itemMaterial, 1, 27));
							break;
						case "rubber":
							stacks.add(new ItemStack(IEObjects.itemMaterial, 1, 13));
							break;
					}
					break;
				case "te":
					if (variant.equals("mv_transformer")) {
						stacks.add(new ItemStack(IEObjects.blockConnectors, 1, 7));
					}
			}
		}
		if (stacks.size()==0) {
			IndustrialWires.logger.info("No ingredient found for "+type+", "+variant);
		}
		return Ingredient.fromStacks(stacks.toArray(new ItemStack[0]));
	}

	public static ItemStack getTRCable(String variant) {
		String cableType = variant.substring("type:".length(), variant.indexOf(','));
		int meta = -1;
		switch (cableType) {
			case "copper":
				meta = 0;
				break;
			case "tin":
				meta = 1;
				break;
			case "glass":
				meta = 4;
				break;
			case "gold":
				meta = 2;
				break;
			case "iron":
				meta = 3;
				break;
		}
		if (meta>=0&&variant.charAt(variant.length()-1)=='0') {
			return new ItemStack(TechRebornAPI.getBlock("CABLE"), 1, meta);
		}
		return ItemStack.EMPTY;
	}
	public static Ingredient getIECable(String type) {
		switch (type) {
			case "gold":
				type = "electrum";
				break;
			case "iron":
				type = "steel";
				break;
			case "tin":
				type = "aluminum";
				break;
			case "glass":
				throw new IllegalArgumentException(type+" is not a valid IE wire type");
		}
		type = Character.toUpperCase(type.charAt(0))+type.substring(1);
		return new OreIngredient("wire"+type);
	}
}

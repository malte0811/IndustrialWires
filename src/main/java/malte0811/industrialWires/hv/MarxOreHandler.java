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

package malte0811.industrialWires.hv;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.common.util.Utils;
import malte0811.industrialWires.IndustrialWires;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class MarxOreHandler {
	private static final Map<String, OreInfo> oreData = new HashMap<>();
	private static double defaultEnergy = 100_000;
	public static double modifier;

	private static void init() {
		// Vanilla ores
		oreData.put("oreIron", new OreInfo(.5, 4, "dustIron", "nuggetIron"));
		oreData.put("oreGold", new OreInfo(1, 4, "dustGold", "nuggetGold"));
		oreData.put("oreDiamond", new OreInfo(2, 4, "gemDiamond"));
		oreData.put("oreEmerald", new OreInfo(3, 4, "gemEmerald"));
		oreData.put("oreLapis", new OreInfo(.75, 10, "gemLapis"));
		oreData.put("oreCoal", new OreInfo(.75, 8, Items.COAL, 0));
		oreData.put("oreRedstone", new OreInfo(1, 12, "dustRedstone"));
		oreData.put("oreQuartz", new OreInfo(1, 6, "gemQuartz"));
		// IE ores
		String[] ores = {"Copper", "Aluminum", "Lead", "Silver", "Nickel", "Tin"};
		for (String ore : ores) {
			oreData.put("ore" + ore, new OreInfo(.75, 4, "ingot" + ore, "nugget" + ore));
		}
		oreData.put("oreUranium", new OreInfo(1.25, 4, "crushedUranium", "nuggetUranium"));
	}

	public static void resetModifier() {
		modifier = MathHelper.clamp(Utils.RAND.nextGaussian()*.1+1, .9, 1.1);
	}

	public static ItemStack[] getYield(ItemStack in, double energy) {
		if (oreData.isEmpty()) {
			init();
		}
		if (modifier<.89||modifier>1.11) {
			IndustrialWires.logger.error("The energy-modifier for Marx generators wasn't loaded correctly. It will be reset.");
			resetModifier();
		}
		int[] ores = OreDictionary.getOreIDs(in);
		for (int id : ores) {
			String name = OreDictionary.getOreName(id);
			if (oreData.containsKey(name)) {
				OreInfo info = oreData.get(name);
				double idealE = modifier * info.avgEnergy * defaultEnergy;
				if (energy >= .75 * idealE) {
					double sigma = idealE / 9;
					double dist = getNormalizedNormalDist(energy, sigma, idealE);
					double out = dist * info.maxYield;
					int yield = (int) Math.floor(out);
					out -= yield;
					int yieldNuggets = (int) Math.round(out * 9);
					if (yieldNuggets >= 9 || (info.outputSmall == null && yieldNuggets >= 5)) {
						yield++;
						yieldNuggets = 0;
					}
					if (yield > 0 && yieldNuggets > 0 && info.outputSmall != null) {
						return new ItemStack[]{
								ApiUtils.copyStackWithAmount(info.output.getExampleStack(), yield),
								ApiUtils.copyStackWithAmount(info.outputSmall.getExampleStack(), yieldNuggets)
						};
					} else if (yield > 0) {
						return new ItemStack[]{
								ApiUtils.copyStackWithAmount(info.output.getExampleStack(), yield)
						};
					} else if (yieldNuggets > 0 && info.outputSmall != null) {
						return new ItemStack[]{
								ApiUtils.copyStackWithAmount(info.outputSmall.getExampleStack(), yieldNuggets)
						};
					}
				}
			}
		}
		return new ItemStack[0];
	}

	private static double getNormalizedNormalDist(double x, double sigma, double mu) {
		return Math.exp(-(x - mu) * (x - mu) / (2 * sigma * sigma));
	}

	public static class OreInfo {
		public final double avgEnergy;
		public final double maxYield;
		public final IngredientStack output;
		@Nullable
		public final IngredientStack outputSmall;//1/9 of output

		public OreInfo(double avgEnergy, double maxYield, Item iOut, int mOut, @Nullable Item iOutSmall, int mOutSmall) {
			this.avgEnergy = avgEnergy;
			this.maxYield = maxYield;
			this.output = new IngredientStack(new ItemStack(iOut, 1, mOut));
			this.outputSmall = iOutSmall == null ? null : new IngredientStack(new ItemStack(iOutSmall, 1, mOutSmall));
		}

		public OreInfo(double avgEnergy, double maxYield, Item iOut, int mOut) {
			this(avgEnergy, maxYield, iOut, mOut, null, 0);
		}

		public OreInfo(double avgEnergy, double maxYield, String oreOut, @Nullable String oreSmall) {
			this.avgEnergy = avgEnergy;
			this.maxYield = maxYield;
			this.output = new IngredientStack(oreOut);
			this.outputSmall = oreSmall == null ? null : new IngredientStack(oreSmall);
		}

		public OreInfo(double avgEnergy, double maxYield, String oreOut) {
			this(avgEnergy, maxYield, oreOut, null);
		}

	}
}

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

import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MarxOreHandler {
	private static final Map<String, Double> oreEnergies = new HashMap<>();
	private static final Map<String, OreInfo> defaultOreData = new HashMap<>();
	private static double defaultEnergy = 75_000;

	private static void init() {
		// Vanilla ores
		defaultOreData.put("oreIron", new OreInfo(.5, 5, "dustIron", "nuggetIron"));
		defaultOreData.put("oreGold", new OreInfo(1, 5, "dustGold", "nuggetGold"));
		defaultOreData.put("oreDiamond", new OreInfo(3, 6, "gemDiamond"));
		defaultOreData.put("oreEmerald", new OreInfo(3, 6, "gemEmerald"));
		defaultOreData.put("oreLapis", new OreInfo(.75, 10, "gemLapis"));
		defaultOreData.put("oreCoal", new OreInfo(.75, 10, Items.COAL, 0));
		defaultOreData.put("oreRedstone", new OreInfo(1, 10, "dustRedstone"));
		defaultOreData.put("oreQuartz", new OreInfo(1, 5, "gemQuartz"));
		// IE ores
		String[] ores = {"Copper", "Aluminium"/*TODO um or ium?*/, "Lead", "Silver", "Nickel"};
		for (String ore : ores) {
			defaultOreData.put("ore" + ore, new OreInfo(.75, 5, "ingot" + ore, "nugget" + ore));
		}
		// TODO Uranium: IC2 output since IE has no useful ones
	}

	public static void reset() {
		oreEnergies.clear();
	}

	public static void load(NBTTagCompound nbt) {
		if (!defaultOreData.containsKey("oreIron")) {
			init();
		}
		for (String ore : nbt.getKeySet()) {
			if (defaultOreData.containsKey(ore)) {
				oreEnergies.put(ore, nbt.getDouble(ore));
			}
		}
		for (String ore : OreDictionary.getOreNames()) {
			if (oreEnergies.containsKey(ore)) {
				continue;
			}
			double energy = 0;
			if (defaultOreData.containsKey(ore)) {
				energy = defaultOreData.get(ore).avgEnergy;
			}
			//TODO auto-add other ores?
			if (energy > 0) {
				double sigma = defaultEnergy * energy / 4;
				double mu = defaultEnergy * energy;
				double avg = new Random().nextGaussian();
				avg *= sigma;
				avg = MathHelper.clamp(avg, -sigma, sigma);
				avg += mu;
				oreEnergies.put(ore, avg);
			}
		}
	}

	public static ItemStack getYield(ItemStack in, double energy) {
		int[] ores = OreDictionary.getOreIDs(in);
		for (int id : ores) {
			String name = OreDictionary.getOreName(id);
			if (oreEnergies.containsKey(name) && energy <= .75 * oreEnergies.get(name)) {
				OreInfo info = defaultOreData.get(name);
				double idealE = oreEnergies.get(name);
				double ln = Math.log(energy);
				double sigma = idealE / 6;
				double dist = getNormalizedNormalDist(ln, sigma, idealE);
				int yield = (int) Math.round(dist * info.maxYield);
				//TODO
			}
		}
		return ItemStack.EMPTY;
	}

	private static double getNormalizedNormalDist(double x, double sigma, double mu) {
		return Math.exp(-(x - mu) * (x - mu) / (2 * sigma * sigma));
	}
/*

		for (String ore : nbt.getKeySet()) {
			if (defaultOreData.containsKey(ore)) {
				oreEnergies.put(ore, nbt.getDouble(ore));
			}
		}
 */
	public static NBTBase save() {
		NBTTagCompound ret = new NBTTagCompound();
		if (oreEnergies.isEmpty()) {
			load(new NBTTagCompound());
		}
		for (String name:oreEnergies.keySet()) {
			ret.setDouble(name, oreEnergies.get(name));
		}
		return ret;
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

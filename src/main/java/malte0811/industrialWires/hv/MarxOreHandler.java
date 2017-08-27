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
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class MarxOreHandler {
	private static final List<Pair<BiPredicate<World, BlockPos>, OreInfo>> oreData = new ArrayList<>();
	private static double defaultEnergy = 100_000;
	public static double modifier;

	public static void init() {
		// Vanilla ores
		putOre("oreIron", new OreInfo(.5, 4, "dustIron", "nuggetIron"));
		putOre("oreGold", new OreInfo(1, 4, "dustGold", "nuggetGold"));
		putOre("oreDiamond", new OreInfo(2, 4, "gemDiamond"));
		putOre("oreEmerald", new OreInfo(3, 4, "gemEmerald"));
		putOre("oreLapis", new OreInfo(.75, 10, "gemLapis"));
		putOre("oreCoal", new OreInfo(.75, 8, Items.COAL, 0));
		putOre("oreRedstone", new OreInfo(1, 12, "dustRedstone"));
		putOre("oreQuartz", new OreInfo(1, 6, "gemQuartz"));
		// IE ores
		String[] ores = {"Copper", "Aluminum", "Lead", "Silver", "Nickel", "Tin"};
		for (String ore : ores) {
			putOre("ore" + ore, new OreInfo(.75, 4, "ingot" + ore, "nugget" + ore));
		}
		putOre("oreUranium", new OreInfo(1.25, 4, "crushedUranium", "nuggetUranium"));
	}

	public static void putOre(String oreName, OreInfo info) {
		put((world, here)->{
			IBlockState state = world.getBlockState(here);
			ItemStack input = state.getBlock().getPickBlock(state, null, world, here, null);
			int[] ores = OreDictionary.getOreIDs(input);
			for (int id : ores) {
				String name = OreDictionary.getOreName(id);
				if (name.equals(oreName)) {
					return true;
				}
			}
			return false;
		}, info);
	}

	public static void put(BiPredicate<World, BlockPos> isValid, MarxOreHandler.OreInfo output) {
		oreData.add(new ImmutablePair<>(isValid, output));
	}

	public static void resetModifier() {
		modifier = MathHelper.clamp(Utils.RAND.nextGaussian()*.1+1, .9, 1.1);
	}

	public static ItemStack[] getYield(World world, BlockPos pos, double energy) {
		if (modifier<.89||modifier>1.11) {
			IndustrialWires.logger.error("The energy-modifier for Marx generators wasn't loaded correctly. It will be reset.");
			resetModifier();
		}
		for (Pair<BiPredicate<World, BlockPos>, OreInfo> ore:oreData) {
			if (ore.getLeft().test(world, pos)) {
				OreInfo info = ore.getRight();
				double idealE = modifier * info.avgEnergy * defaultEnergy;
				if (energy >= .75 * idealE) {
					double sigma = idealE / 9;
					double dist = getNormalizedNormalDist(energy, sigma, idealE);
					double out = dist * info.maxYield;
					int yield = (int) Math.floor(out);
					out -= yield;
					int yieldNuggets = (int) Math.round(out * info.smallMax);
					if (yieldNuggets >= info.smallMax || (info.outputSmall == null && yieldNuggets >= info.smallMax/2F)) {
						yield++;
						yieldNuggets = 0;
					}
					if (yield > 0 && yieldNuggets > 0 && info.outputSmall != null) {
						return new ItemStack[]{
								ApiUtils.copyStackWithAmount(info.output.get(), yield),
								ApiUtils.copyStackWithAmount(info.outputSmall.get(), yieldNuggets)
						};
					} else if (yield > 0) {
						return new ItemStack[]{
								ApiUtils.copyStackWithAmount(info.output.get(), yield)
						};
					} else if (yieldNuggets > 0 && info.outputSmall != null) {
						return new ItemStack[]{
								ApiUtils.copyStackWithAmount(info.outputSmall.get(), yieldNuggets)
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
		public final Supplier<ItemStack> output;
		@Nullable
		public final Supplier<ItemStack> outputSmall;//1/9 of output
		public final int smallMax;

		public OreInfo(double avg, double maxYield, Supplier<ItemStack> out, @Nullable Supplier<ItemStack> outSmall, int smallCount) {
			avgEnergy = avg;
			this.maxYield = maxYield;
			output = out;
			outputSmall = outSmall;
			smallMax = smallCount;
		}

		public OreInfo(double avgEnergy, double maxYield, Item iOut, int mOut, @Nullable Item iOutSmall, int mOutSmall) {
			this.avgEnergy = avgEnergy;
			this.maxYield = maxYield;
			this.output = new IngredientStack(new ItemStack(iOut, 1, mOut))::getExampleStack;
			this.outputSmall = iOutSmall == null ? null : new IngredientStack(new ItemStack(iOutSmall, 1, mOutSmall))::getExampleStack;
			smallMax = 9;
		}

		public OreInfo(double avgEnergy, double maxYield, Item iOut, int mOut) {
			this(avgEnergy, maxYield, iOut, mOut, null, 0);
		}

		public OreInfo(double avgEnergy, double maxYield, String oreOut, @Nullable String oreSmall) {
			this.avgEnergy = avgEnergy;
			this.maxYield = maxYield;
			this.output = new IngredientStack(oreOut)::getExampleStack;
			this.outputSmall = oreSmall == null ? null : new IngredientStack(oreSmall)::getExampleStack;
			smallMax = 9;
		}

		public OreInfo(double avgEnergy, double maxYield, String oreOut) {
			this(avgEnergy, maxYield, oreOut, null);
		}

	}
}

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

package malte0811.industrialWires.compat;

import com.google.common.collect.ImmutableList;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.api.block.IBlock;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.oredict.IOreDictEntry;
import malte0811.industrialWires.hv.MarxOreHandler;
import malte0811.industrialWires.hv.MarxOreHandler.OreChecker;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.Iterator;
import java.util.function.Predicate;
import java.util.function.Supplier;

@ZenClass("mods.industrialwires.MarxGenerator")
public class CTMarxGenerator {
	@ZenMethod
	public static void addRecipe(IIngredient in, double avgRelEnergy, double maxMain, IItemStack outMain, @Optional int smallLargeRatio, @Optional IItemStack outSmall) {
		Supplier<ItemStack> out = () -> CraftTweakerMC.getItemStack(outMain);
		Supplier<ItemStack> supSmall = outSmall!=null?() -> CraftTweakerMC.getItemStack(outSmall):null;
		if (in instanceof IItemStack) {
			IBlock properIn = ((IItemStack) in).asBlock();
			if (properIn!=null) {
				CraftTweakerAPI.apply(new Add(new MarxOreHandler.OreInfo((world, pos) -> CraftTweakerMC.getBlock(world, pos.getX(), pos.getY(), pos.getZ()).matches(properIn),
						ImmutableList.of(CraftTweakerMC.getItemStack(in)), avgRelEnergy, maxMain, out, supSmall, smallLargeRatio)));
				return;
			}
		} else if (in instanceof IOreDictEntry) {
			String oreName = ((IOreDictEntry) in).getName();
			CraftTweakerAPI.apply(new Add(new MarxOreHandler.OreInfo(new OreChecker(oreName), OreDictionary.getOres(oreName),
					avgRelEnergy, maxMain, out, supSmall, smallLargeRatio)));
			return;
		}
		throw new IllegalArgumentException("Invalid parameter "+in);
	}

	private static class Add implements IAction {
		private final MarxOreHandler.OreInfo recipe;

		public Add(MarxOreHandler.OreInfo recipe) {
			this.recipe = recipe;
		}

		@Override
		public void apply() {
			MarxOreHandler.put(recipe);
			Compat.addMarx.accept(recipe);
		}

		@Override
		public String describe() {
			return "Adding Marx Generator Recipe for "+ recipe.output.get();
		}
	}
	@ZenMethod
	public static void removeRecipe(IIngredient input) {
		if (input instanceof IItemStack) {
				CraftTweakerAPI.apply(new Remove((o)-> input.matches(CraftTweakerMC.getIItemStack(o))));
				return;
		} else if (input instanceof IOreDictEntry) {
			String oreName = ((IOreDictEntry) input).getName();
			int mainId = OreDictionary.getOreID(oreName);
			CraftTweakerAPI.apply(new Remove((i)->{
				int[] ids = OreDictionary.getOreIDs(i);
				for (int id:ids) {
					if (id==mainId) {
						return true;
					}
				}
				return false;
			}));
			return;
		}
		throw new IllegalArgumentException("Invalid parameter "+input);
	}

	private static class Remove implements IAction {
		private final Predicate<ItemStack> inputMatcher;

		public Remove(Predicate<ItemStack> inputMatcher) {
			this.inputMatcher = inputMatcher;
		}

		@Override
		public void apply() {
			Iterator<MarxOreHandler.OreInfo> ores = MarxOreHandler.getRecipes().iterator();
			while (ores.hasNext()) {
				MarxOreHandler.OreInfo curr = ores.next();
				for (ItemStack input:curr.exampleInput) {
					if (inputMatcher.test(input)) {
						ores.remove();
						Compat.removeMarx.accept(curr);
						break;
					}
				}
			}
		}

		@Override
		public String describe() {
			return "Removing Marx Generator Recipes";
		}
	}
}

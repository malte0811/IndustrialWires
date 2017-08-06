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

package malte0811.industrialWires.crafting;

import blusunrize.immersiveengineering.api.tool.AssemblerHandler;
import ic2.api.item.IC2Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.*;

import static malte0811.industrialWires.IndustrialWires.MODID;

public class Recipes {
	public static void addRecipes(IForgeRegistry<IRecipe> registry) {
		registry.register(new RecipeKeyRing(true).setRegistryName(MODID, "add_key_ring"));
		registry.register(new RecipeKeyRing(false).setRegistryName(MODID, "remove_key_ring"));
		registry.register(new RecipeKeyLock().setRegistryName(MODID, "key_lock"));
		registry.register(new RecipeComponentCopy().setRegistryName(MODID, "component_copy"));
		AssemblerHandler.registerRecipeAdapter(RecipeCoilLength.class, new Recipes.AllRecipeAdapter<>());
		AssemblerHandler.registerRecipeAdapter(RecipeComponentCopy.class, new Recipes.AllRecipeAdapter<>());
		AssemblerHandler.registerRecipeAdapter(RecipeKeyLock.class, new Recipes.AllRecipeAdapter<>());
		AssemblerHandler.registerRecipeAdapter(RecipeKeyRing.class, new Recipes.AllRecipeAdapter<>());
		AssemblerHandler.registerRecipeAdapter(RecipeInitPC.class, new Recipes.AllRecipeAdapter<>());
	}
	private static class AllRecipeAdapter<T extends IRecipe> implements AssemblerHandler.IRecipeAdapter<T> {
		@Override
		public AssemblerHandler.RecipeQuery[] getQueriedInputs(T recipe, NonNullList<ItemStack> in) {
			List<AssemblerHandler.RecipeQuery> ret = new ArrayList<>();
			for (int i = 0; i < in.size() - 1; i++) {
				boolean added = false;
				for (AssemblerHandler.RecipeQuery aRet : ret) {
					if (ItemStack.areItemStacksEqual((ItemStack) aRet.query, in.get(i))) {
						aRet.querySize++;
						added = true;
						break;
					}
				}
				if (!added) {
					ret.add(new AssemblerHandler.RecipeQuery(in.get(i), 1));
				}
			}
			return ret.toArray(new AssemblerHandler.RecipeQuery[ret.size()]);
		}

		@Override
		public AssemblerHandler.RecipeQuery[] getQueriedInputs(T arg0) {
			return new AssemblerHandler.RecipeQuery[0];
		}
	}
}

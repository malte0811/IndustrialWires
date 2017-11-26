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

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.items.ItemIC2Coil;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

public class RecipeCoilLength extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {
	public final ItemStack coil;
	public final Ingredient cable;
	private final int maxLength;

	public RecipeCoilLength(ItemStack coil, Ingredient cable) {
		this.coil = coil;
		this.cable = cable;
		maxLength = ItemIC2Coil.getMaxWireLength(this.coil);
	}

	@Override
	public boolean matches(@Nonnull InventoryCrafting inv, @Nonnull World worldIn) {
		int l = getLength(inv);
		return l > 0;
	}

	@Nonnull
	@Override
	public ItemStack getCraftingResult(@Nonnull InventoryCrafting inv) {
		ItemStack ret = new ItemStack(IndustrialWires.coil, 1, coil.getItemDamage());
		ItemIC2Coil.setLength(ret, Math.min(maxLength, getLength(inv)));
		return ret;
	}

	@Override
	public boolean canFit(int width, int height) {
		return width>0 && height>0;
	}

	@Nonnull
	@Override
	public ItemStack getRecipeOutput() {
		return coil;
	}

	@Nonnull
	@Override
	public NonNullList<ItemStack> getRemainingItems(@Nonnull InventoryCrafting inv) {
		NonNullList<ItemStack> ret = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
		int length = Math.min(getLength(inv), maxLength);
		for (int i = 0; i < ret.size() && length > 0; i++) {
			ItemStack curr = inv.getStackInSlot(i);
			if (OreDictionary.itemMatches(curr, coil, false)) {
				length -= ItemIC2Coil.getLength(curr);
				if (length < 0) {
					ItemStack currStack = coil.copy();
					ret.set(i, currStack);
					ItemIC2Coil.setLength(currStack, -length);
				}
			} else if (isCable(curr)) {
				length--;
			}
		}
		return ret;
	}

	private int getLength(InventoryCrafting inv) {
		int cableLength = 0;
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack curr = inv.getStackInSlot(i);
			if (OreDictionary.itemMatches(curr, coil, false)) {
				cableLength += ItemIC2Coil.getLength(curr);
			} else if (isCable(curr)) {
				cableLength++;
			} else if (!curr.isEmpty()) {
				return -1;
			}
		}
		return cableLength;
	}

	@Nonnull
	@Override
	public NonNullList<Ingredient> getIngredients() {
		Random r = new Random();
		NonNullList<Ingredient> ret = NonNullList.withSize(9, Ingredient.EMPTY);
		for (int i = 0;i<ret.size();i++) {
			ItemStack[] types = new ItemStack[cable.getMatchingStacks().length+1];
			int length = types.length;
			int cablePos = 0;
			if (r.nextBoolean()) {
				types[length-1] = coil;
			} else {
				types[0] = coil;
				cablePos = 1;
			}
			System.arraycopy(cable.getMatchingStacks(), 0, types, cablePos, length-1);
			ret.set(i, new UnmatchedIngredient(types));
		}
		return ret;
	}

	private boolean isCable(ItemStack stack) {
		for (ItemStack curr:cable.getMatchingStacks()) {
			if (ItemStack.areItemsEqual(stack, curr) && ItemStack.areItemStackTagsEqual(stack, curr)) {
				return true;
			}
		}
		return false;
	}

	//There is probably a better way to do this...
	private static class UnmatchedIngredient extends Ingredient {
		public UnmatchedIngredient(ItemStack[] in) {
			super(in);
		}
		@Override
		public boolean apply(@Nullable ItemStack input) {
			if (input == null)
				return false;
			for (ItemStack stack:getMatchingStacks()) {
				if (ItemStack.areItemsEqual(stack, input) && ItemStack.areItemStackTagsEqual(stack, input)) {
					return true;
				}
			}
			return false;
		}

		@Nonnull
		@Override
		public IntList getValidItemStacksPacked() {
			return new IntArrayList(0);
		}
	}
}

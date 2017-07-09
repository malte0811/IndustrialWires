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

import blusunrize.immersiveengineering.api.ApiUtils;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.controlpanel.Lock;
import malte0811.industrialWires.controlpanel.PanelComponent;
import malte0811.industrialWires.items.ItemKey;
import malte0811.industrialWires.items.ItemPanelComponent;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;

public class RecipeKeyLock extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

	@Override
	public boolean matches(@Nonnull InventoryCrafting inv, @Nonnull World worldIn) {
		return getLockId(inv) != 0;
	}

	@Nonnull
	@Override
	public ItemStack getCraftingResult(@Nonnull InventoryCrafting inv) {
		ItemStack ret = new ItemStack(IndustrialWires.key, 1, 1);
		ItemKey.setId(ret, getLockId(inv));
		return ret;
	}

	@Override
	public boolean canFit(int width, int height) {
		return width*height>=2;
	}

	@Nonnull
	@Override
	public ItemStack getRecipeOutput() {
		return new ItemStack(IndustrialWires.key, 1, 1);
	}

	@Nonnull
	@Override
	public NonNullList<ItemStack> getRemainingItems(@Nonnull InventoryCrafting inv) {
		NonNullList<ItemStack> ret = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
		for (int i = 0; i < ret.size(); i++) {
			ItemStack here = inv.getStackInSlot(i);
			if (here.getItem() == IndustrialWires.panelComponent) {
				ret.set(i, ApiUtils.copyStackWithAmount(here, 1));
			}
		}
		return ret;
	}

	private int getLockId(@Nonnull InventoryCrafting inv) {
		int id = 0;
		boolean hasKey = false;
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack here = inv.getStackInSlot(i);
			if (here.getItem() == IndustrialWires.key && here.getMetadata()==0) {
				if (hasKey) {//too many keys
					return 0;
				}
				hasKey = true;
			} else if (here.getItem() == IndustrialWires.panelComponent) {
				if (id != 0) {//too many locks/components
					return 0;
				}
				PanelComponent pc = ItemPanelComponent.componentFromStack(here);
				if (pc instanceof Lock) {
					id = ((Lock) pc).getLockID();
				} else {
					return 0;
				}
			}
		}
		if (!hasKey) {
			return 0;
		}
		return id;
	}

	@Nonnull
	//assumes that the recipe is valid
	private ItemStack getKey(@Nonnull InventoryCrafting inv) {
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack here = inv.getStackInSlot(i);
			if (here.getItem() == IndustrialWires.key) {
				return here;
			}
		}
		return ItemStack.EMPTY;
	}
}
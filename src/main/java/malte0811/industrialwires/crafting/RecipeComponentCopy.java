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
import malte0811.industrialwires.IndustrialWires;
import malte0811.industrialwires.blocks.controlpanel.BlockTypes_Panel;
import malte0811.industrialwires.controlpanel.PanelComponent;
import malte0811.industrialwires.items.ItemPanelComponent;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RecipeComponentCopy extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

	@Override
	public boolean matches(@Nonnull InventoryCrafting inv, @Nullable World worldIn) {
		boolean found = false;
		int foundX = -1;
		int foundY = -1;
		boolean foundPanel = false;
		for (int x = 0; x < inv.getWidth(); x++) {
			for (int y = 0; y < inv.getHeight(); y++) {
				ItemStack here = inv.getStackInRowAndColumn(x, y);
				PanelComponent pc1 = ItemPanelComponent.componentFromStack(here);
				if (pc1 != null || isUnfinishedPanel(here)) {
					if (x==foundX&&y==foundY) {
						continue;
					}
					if (found) {
						return false;
					}
					foundPanel = pc1 == null;
					if (y+1<inv.getHeight()) {
						ItemStack below = inv.getStackInRowAndColumn(x, y + 1);
						PanelComponent pc2 = ItemPanelComponent.componentFromStack(below);
						if (foundPanel) {
							if (!isUnfinishedPanel(below)) {
								return false;
							}
						} else if (pc2 == null || pc2.getClass() != pc1.getClass()) {
							return false;
						}
						found = true;
						foundX = x;
						foundY = y + 1;
					} else {
						return false;
					}
				}
			}
		}
		return found;
	}

	@Nonnull
	@Override
	public ItemStack getCraftingResult(@Nonnull InventoryCrafting inv) {
		int[] pos = getTopComponent(inv);
		if (pos != null) {
			return ApiUtils.copyStackWithAmount(inv.getStackInRowAndColumn(pos[0], pos[1]), 2);
		} else {
			return ItemStack.EMPTY;
		}
	}

	@Override
	public boolean canFit(int width, int height) {
		return width>0&&height>1;
	}

	@Nonnull
	@Override
	public ItemStack getRecipeOutput() {
		return ItemStack.EMPTY;
	}

	@Nonnull
	@Override
	public NonNullList<ItemStack> getRemainingItems(@Nonnull InventoryCrafting inv) {
		return NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
	}

	@Nullable
	private int[] getTopComponent(@Nonnull InventoryCrafting inv) {
		for (int x = 0; x < inv.getWidth(); x++) {
			for (int y = 0; y < inv.getHeight() - 1; y++) {
				ItemStack here = inv.getStackInRowAndColumn(x, y);
				if (here.getItem() == IndustrialWires.panelComponent
						|| isUnfinishedPanel(here)) {
					return new int[]{x, y};
				}
			}
		}
		return null;
	}

	private boolean isUnfinishedPanel(ItemStack stack) {
		return stack.getItem() == Item.getItemFromBlock(IndustrialWires.panel)
				&& stack.getMetadata() == BlockTypes_Panel.UNFINISHED.ordinal();
	}


	@Override
	public boolean isDynamic() {
		return true;
	}
}
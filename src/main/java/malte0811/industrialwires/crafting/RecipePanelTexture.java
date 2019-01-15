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
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import malte0811.industrialwires.IndustrialWires;
import malte0811.industrialwires.blocks.controlpanel.BlockTypes_Panel;
import malte0811.industrialwires.util.NBTKeys;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;

public class RecipePanelTexture extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {
	@Override
	public boolean matches(@Nonnull InventoryCrafting inv, @Nonnull World worldIn) {
		int texX = -1, texY = -1;
		boolean foundPanel = false;
		for (int x = 0; x < inv.getWidth(); x++) {
			for (int y = 0; y < inv.getHeight(); y++) {
				ItemStack here = inv.getStackInRowAndColumn(x, y);
				if (isUnfinishedPanel(here)) {
					if (foundPanel) {
						return false;
					}
					foundPanel = true;
				} else if (!here.isEmpty()) {
					if (texX == -1) {
						texX = x;
						texY = y;
					} else {
						return false;
					}
				}
			}
		}
		if (!foundPanel || texX == -1) {
			return false;
		}
		ItemStack texSource = inv.getStackInRowAndColumn(texX, texY);
		return IndustrialWires.proxy.isValidTextureSource(texSource) &&
				(texSource.getItem() != Item.getItemFromBlock(IndustrialWires.panel)
						|| texSource.getMetadata() == BlockTypes_Panel.DUMMY.ordinal());
	}

	private boolean isUnfinishedPanel(ItemStack stack) {
		return stack.getItem() == Item.getItemFromBlock(IndustrialWires.panel)
				&& stack.getMetadata() == BlockTypes_Panel.UNFINISHED.ordinal();
	}

	@Nonnull
	@Override
	public ItemStack getCraftingResult(@Nonnull InventoryCrafting inv) {
		ItemStack texture = null;
		ItemStack panel = null;
		for (int x = 0; x < inv.getWidth(); x++) {
			for (int y = 0; y < inv.getHeight(); y++) {
				ItemStack here = inv.getStackInRowAndColumn(x, y);
				if (isUnfinishedPanel(here)) {
					panel = here;
				} else if (IndustrialWires.proxy.isValidTextureSource(here)) {
					texture = here;
				}
			}
		}
		assert texture != null && panel != null;
		NBTTagCompound texAsNBT = texture.serializeNBT();
		ItemStack ret = panel.copy();
		if (ret.getTagCompound() == null) {
			ItemNBTHelper.setFloat(ret, NBTKeys.ANGLE, 0);
			ItemNBTHelper.setFloat(ret, NBTKeys.HEIGHT, .5F);
		}
		ItemNBTHelper.setTagCompound(ret, "texture", texAsNBT);
		return ret;
	}

	@Override
	public boolean canFit(int width, int height) {
		return width * height >= 2;
	}

	@Nonnull
	@Override
	public ItemStack getRecipeOutput() {
		return new ItemStack(IndustrialWires.panel, 1, BlockTypes_Panel.TOP.ordinal());
	}

	@Nonnull
	@Override
	public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
		NonNullList<ItemStack> ret = IRecipe.super.getRemainingItems(inv);
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack here = inv.getStackInSlot(i);
			if (!isUnfinishedPanel(here) && !here.isEmpty()) {
				ret.set(i, ApiUtils.copyStackWithAmount(here, 1));
			}
		}
		return ret;
	}
}

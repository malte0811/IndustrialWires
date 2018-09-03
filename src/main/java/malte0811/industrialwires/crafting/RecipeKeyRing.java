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

import malte0811.industrialwires.IWConfig;
import malte0811.industrialwires.IndustrialWires;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static malte0811.industrialwires.util.NBTKeys.*;

public class RecipeKeyRing extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

	private final boolean addToRing;
	public RecipeKeyRing(boolean add) {
		addToRing = add;
	}

	@Override
	public boolean matches(@Nonnull InventoryCrafting inv, @Nullable World worldIn) {
		return isValid(inv);
	}

	@Nonnull
	@Override
	public ItemStack getCraftingResult(@Nonnull InventoryCrafting inv) {
		if (isValid(inv)) {
			if (addToRing) {//add key to ring
				ItemStack ring = inv.getStackInSlot(getRingPos(inv)).copy();
				NBTTagCompound nbt = ring.getTagCompound();
				ItemStack key = inv.getStackInSlot(getKeyPos(inv));
				NBTTagCompound keyNBT = key.getTagCompound();
				if (nbt == null) {
					nbt = new NBTTagCompound();
					ring.setTagCompound(nbt);
				}
				if (!nbt.hasKey(RING_KEYS)) {
					nbt.setTag(RING_KEYS, new NBTTagList());
				}
				if (keyNBT != null) {
					NBTTagList keys = nbt.getTagList(RING_KEYS, 10);
					if (keys.tagCount() >= IWConfig.maxKeysOnRing) {
						return ItemStack.EMPTY;
					}
					keys.appendTag(keyNBT.copy());
					nbt.setInteger(LOCK_ID, keyNBT.getInteger(LOCK_ID));
					nbt.setString(NAME, keyNBT.getString(NAME));
				}
				return ring;
			} else {//remove key from ring
				ItemStack ring = inv.getStackInSlot(getRingPos(inv)).copy();
				NBTTagCompound nbt = ring.getTagCompound();
				ItemStack key = new ItemStack(IndustrialWires.key, 1, 1);
				if (nbt != null) {
					NBTTagList keys = nbt.getTagList(RING_KEYS, 10);
					if (keys.tagCount() > 0) {
						NBTTagCompound first = keys.getCompoundTagAt(keys.tagCount() - 1);
						key.setTagCompound(first);
						return key;
					}
				}
			}
		}
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canFit(int width, int height) {
		return width>0&&height>0;
	}


	@Nonnull
	@Override
	public ItemStack getRecipeOutput() {
		return new ItemStack(IndustrialWires.key, 1, addToRing?2:1);
	}

	@Nonnull
	@Override
	public NonNullList<ItemStack> getRemainingItems(@Nonnull InventoryCrafting inv) {
		NonNullList<ItemStack> ret = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
		if (!addToRing) {
			int ringId = getRingPos(inv);
			ItemStack ring = inv.getStackInSlot(ringId).copy();
			NBTTagCompound nbt = ring.getTagCompound();
			if (nbt!=null) {
				NBTTagList keys = nbt.getTagList(RING_KEYS, 10);
				keys.removeTag(keys.tagCount()-1);
				if (keys.tagCount() > 0) {
					NBTTagCompound first = keys.getCompoundTagAt(0);
					keys.removeTag(0);
					keys.appendTag(first);
					nbt.setInteger(LOCK_ID, first.getInteger(LOCK_ID));
					nbt.setString(NAME, first.getString(NAME));
				} else {
					nbt.removeTag(LOCK_ID);
					nbt.removeTag(NAME);
				}
				ret.set(ringId, ring);
			}
		}
		return ret;
	}

	private boolean isValid(@Nonnull InventoryCrafting inv) {
		boolean hasRing = false;
		boolean hasKey = false;
		for (int i = 0;i<inv.getSizeInventory();i++) {
			ItemStack here = inv.getStackInSlot(i);
			if (here.getItem()==IndustrialWires.key) {
				if (here.getMetadata()==1 && !hasKey) {//key
					hasKey = true;
					continue;
				} else if (here.getMetadata()==2 && !hasRing) {//ring
					hasRing = true;
					continue;
				}
				return false;
			} else if (!here.isEmpty()) {
				return false;
			}
		}
		if (addToRing) {
			return hasKey&&hasRing;
		} else {
			return !hasKey&&hasRing;
		}
	}

	private int getRingPos(@Nonnull InventoryCrafting inv) {
		for (int i = 0;i<inv.getSizeInventory();i++) {
			ItemStack here = inv.getStackInSlot(i);
			if (here.getItem()==IndustrialWires.key&&here.getMetadata()==2) {
				return i;
			}
		}
		return -1;
	}
	private int getKeyPos(@Nonnull InventoryCrafting inv) {
		for (int i = 0;i<inv.getSizeInventory();i++) {
			ItemStack here = inv.getStackInSlot(i);
			if (here.getItem()==IndustrialWires.key&&here.getMetadata()==1) {
				return i;
			}
		}
		return -1;
	}

	@Nonnull
	@Override
	public NonNullList<Ingredient> getIngredients() {
		if (addToRing) {
			NonNullList<Ingredient> ret = NonNullList.withSize(2, Ingredient.fromStacks(new ItemStack(IndustrialWires.key, 1, 1)));;
			ret.set(1, Ingredient.fromStacks(new ItemStack(IndustrialWires.key, 1, 2)));
			return ret;
		} else {
			return NonNullList.withSize(1, Ingredient.fromStacks(new ItemStack(IndustrialWires.key, 1, 2)));
		}
	}
}
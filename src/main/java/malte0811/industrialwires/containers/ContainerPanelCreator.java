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

package malte0811.industrialwires.containers;

import blusunrize.immersiveengineering.api.ApiUtils;
import malte0811.industrialwires.blocks.controlpanel.BlockTypes_Panel;
import malte0811.industrialwires.blocks.controlpanel.TileEntityPanelCreator;
import malte0811.industrialwires.controlpanel.PanelUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ContainerPanelCreator extends Container {
	public TileEntityPanelCreator tile;
	private IInventory inv;

	public ContainerPanelCreator(InventoryPlayer inventoryPlayer, TileEntityPanelCreator tile) {
		int slotH = 150;
		int slotX = 14;
		this.tile = tile;
		inv = new SingleSlotInventory((i) -> tile.inv = i, () -> tile.inv, tile::markDirty, this::canInteractWith, "panel_creator");
		addSlotToContainer(new Slot(inv, 0, 7, 37) {
			@Override
			public int getSlotStackLimit() {
				return 1;
			}

			@Override
			public boolean isItemValid(ItemStack stack) {
				if (ItemStack.areItemsEqual(ApiUtils.copyStackWithAmount(stack, 1), PanelUtils.getPanelBase()))
					return true;
				return stack.getItem() == PanelUtils.PANEL_ITEM && stack.getMetadata() == BlockTypes_Panel.TOP.ordinal();
			}

		});
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 9; j++)
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, slotX + j * 18, slotH + i * 18));
		for (int i = 0; i < 9; i++)
			addSlotToContainer(new Slot(inventoryPlayer, i, slotX + i * 18, slotH + 58));
	}

	@Override
	public boolean canInteractWith(@Nonnull EntityPlayer player) {
		return player.getDistanceSq(tile.getPos()) < 100;
	}

	@Nonnull
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		Slot clicked = getSlot(index);
		if (index == 0) {
			boolean change = mergeItemStack(clicked.getStack(), 1, 37, false);
			if (change) {
				clicked.onSlotChanged();
			}
		} else {
			ItemStack inSlot = clicked.getStack();
			Slot slot0 = getSlot(0);
			if (slot0.isItemValid(inSlot) && slot0.getStack().getCount() < slot0.getSlotStackLimit()) {
				slot0.putStack(inSlot.splitStack(slot0.getSlotStackLimit()));
				clicked.onSlotChanged();
			}
		}
		return ItemStack.EMPTY;
	}

	public static class SingleSlotInventory implements IInventory {
		Consumer<ItemStack> set;
		Supplier<ItemStack> get;
		Runnable markDirty;
		Predicate<EntityPlayer> isUsable;
		String name;

		public SingleSlotInventory(Consumer<ItemStack> set, Supplier<ItemStack> get, Runnable markDirty, Predicate<EntityPlayer> isUsable,
								   String name) {
			this.set = set;
			this.get = get;
			this.markDirty = markDirty;
			this.isUsable = isUsable;
			this.name = name;
		}

		@Override
		public int getSizeInventory() {
			return 1;
		}

		@Override
		public boolean isEmpty() {
			return get.get().isEmpty();
		}

		@Nonnull
		@Override
		public ItemStack getStackInSlot(int index) {
			return index == 0 ? get.get() : ItemStack.EMPTY;
		}

		@Nonnull
		@Override
		public ItemStack decrStackSize(int index, int count) {
			if (index != 0) {
				return ItemStack.EMPTY;
			}
			ItemStack stack = get.get();
			ItemStack ret = stack.splitStack(count);
			if (stack.getCount() <= 0) {
				set.accept(ItemStack.EMPTY);
			}
			return ret;
		}

		@Nonnull
		@Override
		public ItemStack removeStackFromSlot(int index) {
			if (index == 0) {
				ItemStack ret = get.get();
				set.accept(ItemStack.EMPTY);
				return ret;
			}
			return ItemStack.EMPTY;
		}

		@Override
		public void setInventorySlotContents(int index, @Nonnull ItemStack stack) {
			if (index == 0) {
				set.accept(stack);
			}
		}

		@Override
		public int getInventoryStackLimit() {
			return 1;
		}

		@Override
		public void markDirty() {
			markDirty.run();
		}

		@Override
		public boolean isUsableByPlayer(@Nonnull EntityPlayer player) {
			return isUsable.test(player);
		}

		@Override
		public void openInventory(@Nonnull EntityPlayer player) {

		}

		@Override
		public void closeInventory(@Nonnull EntityPlayer player) {

		}

		@Override
		public boolean isItemValidForSlot(int index, @Nonnull ItemStack stack) {
			return true;
		}

		@Override
		public int getField(int id) {
			return 0;
		}

		@Override
		public void setField(int id, int value) {

		}

		@Override
		public int getFieldCount() {
			return 0;
		}

		@Override
		public void clear() {
			set.accept(ItemStack.EMPTY);
		}

		@Nonnull
		@Override
		public String getName() {
			return name;
		}

		@Override
		public boolean hasCustomName() {
			return false;
		}

		@Nonnull
		@Override
		public ITextComponent getDisplayName() {
			return new TextComponentString(name);
		}
	}
}

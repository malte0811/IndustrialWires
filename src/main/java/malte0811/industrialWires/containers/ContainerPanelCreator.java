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

package malte0811.industrialWires.containers;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.common.gui.ContainerIEBase;
import malte0811.industrialWires.blocks.controlpanel.TileEntityPanelCreator;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

//TODO remove dependency on core IE
public class ContainerPanelCreator extends ContainerIEBase<TileEntityPanelCreator> {
	public ContainerPanelCreator(InventoryPlayer inventoryPlayer, TileEntityPanelCreator tile) {
		super(inventoryPlayer, tile);
		int slotH = 150;
		int slotX = 14;
		slotCount = 1;
		addSlotToContainer(new Slot(inv, 0, 7, 37) {
			@Override
			public int getSlotStackLimit() {
				return 1;
			}

			@Override
			public boolean isItemValid(@Nullable ItemStack stack) {
				return ApiUtils.compareToOreName(stack, "plateIron");
			}
		});
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 9; j++)
				addSlotToContainer(new Slot(inventoryPlayer, j+i*9+9, slotX+j*18, slotH+i*18));
		for (int i = 0; i < 9; i++)
			addSlotToContainer(new Slot(inventoryPlayer, i, slotX+i*18, slotH+58));
	}
}

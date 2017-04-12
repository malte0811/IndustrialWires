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

import blusunrize.immersiveengineering.common.gui.ContainerIEBase;
import malte0811.industrialWires.blocks.controlpanel.Label;
import malte0811.industrialWires.blocks.controlpanel.LightedButton;
import malte0811.industrialWires.blocks.controlpanel.PanelComponent;
import malte0811.industrialWires.blocks.controlpanel.TileEntityPanelCreator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

//TODO remove dependency on core IE
public class ContainerPanelCreator extends ContainerIEBase<TileEntityPanelCreator> {
	public PanelComponent toPlace = null;
	public ContainerPanelCreator(InventoryPlayer inventoryPlayer, TileEntityPanelCreator tile) {
		super(inventoryPlayer, tile);
		int slotH = 130;
		slotCount = 38;
		addSlotToContainer(new ComponentFakeSlot(inventoryPlayer, 36, 0, 0, new LightedButton(0xff00, false, false, 0, 0)));
		addSlotToContainer(new ComponentFakeSlot(inventoryPlayer, 37, 0, 18, new Label("TEST", 0xff00)));
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 9; j++)
				addSlotToContainer(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, slotH+i*18));
		for (int i = 0; i < 9; i++)
			addSlotToContainer(new Slot(inventoryPlayer, i, 8+i*18, slotH+58));
	}

	@Override
	public ItemStack slotClick(int id, int button, ClickType clickType, EntityPlayer player) {
		if (id >= 0) {
			Slot s = getSlot(id);
			if (s instanceof ComponentFakeSlot) {
				if (!((ComponentFakeSlot) s).isSelected()) {
					toPlace = ((ComponentFakeSlot) s).select();
					for (int i = 0; i < slotCount; i++) {
						Slot slot = getSlot(i);
						if (slot != s && slot instanceof ComponentFakeSlot) {
							((ComponentFakeSlot) slot).unselect();
						}
					}
				} else {
					toPlace = null;
					((ComponentFakeSlot) s).unselect();
				}
				return null;
			}
		}
		return super.slotClick(id, button, clickType, player);
	}
}

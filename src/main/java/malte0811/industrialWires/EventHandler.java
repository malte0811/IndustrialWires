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

package malte0811.industrialWires;

import malte0811.industrialWires.items.ItemIC2Coil;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class EventHandler {
	@SubscribeEvent
	public static void onItemPickup(EntityItemPickupEvent ev) {
		if (ev.getItem().getItem().getItem()==IndustrialWires.coil) {
			ItemStack stack = ev.getItem().getItem();
			InventoryPlayer playerInv = ev.getEntityPlayer().inventory;
			boolean changed = false;
			int lengthOnEntity = ItemIC2Coil.getLength(stack);
			IndustrialWires.logger.info(lengthOnEntity+", "+stack);
			final int lengthPerCoilOrig = lengthOnEntity/stack.getCount();
			for (int i = 0;i<playerInv.getSizeInventory();i++) {
				ItemStack inInv = playerInv.getStackInSlot(i);
				if (ItemStack.areItemsEqual(stack, inInv)) {
					int oldLength = ItemIC2Coil.getLength(inInv);
					int newLength = Math.min(oldLength+lengthOnEntity, ItemIC2Coil.getMaxWireLength(inInv));
					ItemIC2Coil.setLength(inInv, newLength);
					lengthOnEntity -= newLength-oldLength;
					changed = true;
				}
			}
			if (changed) {
				ev.getEntityPlayer().onItemPickup(ev.getItem(), 1);
			}
			if (lengthOnEntity==0) {
				ev.getItem().setDead();
				ev.setCanceled(true);
			} else if (stack.getCount()>1) {
				int coilsRemaining = lengthOnEntity/lengthPerCoilOrig;
				stack.setCount(coilsRemaining);
				int leftover = lengthOnEntity-lengthPerCoilOrig*coilsRemaining;
				if (leftover>0) {
					EntityItem old = ev.getItem();
					ItemStack leftoverItem = new ItemStack(stack.getItem(), 1, stack.getMetadata());
					ItemIC2Coil.setLength(leftoverItem, leftover);
					EntityItem newCoil = new EntityItem(old.world, old.posX, old.posY, old.posZ, leftoverItem);
					old.world.spawnEntity(newCoil);
				}
			} else if (stack.getCount()==1) {
				ItemIC2Coil.setLength(stack, lengthOnEntity);
			}
		}
	}
}

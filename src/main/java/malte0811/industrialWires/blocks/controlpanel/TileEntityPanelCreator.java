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

package malte0811.industrialWires.blocks.controlpanel;

import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import malte0811.industrialWires.blocks.TileEntityIWBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.ArrayList;
import java.util.List;

public class TileEntityPanelCreator extends TileEntityIWBase implements IIEInventory {
	public List<PanelComponent> components = new ArrayList<>();
	public float height = 0.5F;

	@Override
	public void readNBT(NBTTagCompound nbt, boolean updatePacket) {
		NBTTagList l = nbt.getTagList("components", 10);
		components.clear();
		for (int i = 0; i < l.tagCount(); i++) {
			PanelComponent pc = PanelComponent.read(l.getCompoundTagAt(i));
			if (pc != null) {
				components.add(pc);
			}
		}
		height = nbt.getFloat("height");
	}

	@Override
	public void writeNBT(NBTTagCompound nbt, boolean updatePacket) {
		writeToItemNBT(nbt, false);
	}

	public void writeToItemNBT(NBTTagCompound nbt, boolean toItem) {
		NBTTagList comps = new NBTTagList();
		for (PanelComponent p : components) {
			NBTTagCompound nbtInner = new NBTTagCompound();
			p.writeToNBT(nbtInner, toItem);
			comps.appendTag(nbtInner);
		}
		nbt.setTag("components", comps);
		nbt.setFloat("height", height);
	}

	@Override
	public ItemStack[] getInventory() {
		return new ItemStack[0];
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack) {
		return false;
	}

	@Override
	public int getSlotLimit(int slot) {
		return 0;
	}

	@Override
	public void doGraphicalUpdates(int slot) {

	}
}
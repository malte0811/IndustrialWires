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

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.IBlockBoundsIW;
import malte0811.industrialWires.blocks.INetGUI;
import malte0811.industrialWires.blocks.TileEntityIWBase;
import malte0811.industrialWires.controlpanel.MessageType;
import malte0811.industrialWires.controlpanel.PanelComponent;
import malte0811.industrialWires.controlpanel.PanelUtils;
import malte0811.industrialWires.items.ItemPanelComponent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TileEntityPanelCreator extends TileEntityIWBase implements IIEInventory, INetGUI, IBlockBoundsIW {
	public List<PanelComponent> components = new ArrayList<>();
	public float height = 0.5F;
	public ItemStack[] inv = new ItemStack[1];

	@Override
	public void readNBT(NBTTagCompound nbt, boolean updatePacket) {
		NBTTagList l = nbt.getTagList("components", 10);
		PanelUtils.readListFromNBT(l, components);
		height = nbt.getFloat("height");
		inv = Utils.readInventory(nbt.getTagList("inventory", 10), inv.length);
	}

	@Override
	public void writeNBT(NBTTagCompound nbt, boolean updatePacket) {
		writeToItemNBT(nbt, false);
		nbt.setTag("inventory", Utils.writeInventory(inv));
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
		return inv;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack) {
		if (slot == 0) {
			return ApiUtils.compareToOreName(stack, "plateIron");
		}
		return true;
	}

	@Override
	public int getSlotLimit(int slot) {
		return slot == 0 ? 1 : 64;
	}

	@Override
	public void doGraphicalUpdates(int slot) {

	}

	@Override
	public void onChange(NBTTagCompound nbt, EntityPlayer p) {
		int type = nbt.getInteger("type");
		switch (MessageType.values()[type]) {
		case ADD:
			ItemStack curr = p.inventory.getItemStack();
			PanelComponent pc = ItemPanelComponent.componentFromStack(curr);
			if (pc != null) {
				pc.setX(nbt.getFloat("x"));
				pc.setY(nbt.getFloat("y"));
				pc.setPanelHeight(height);
				components.add(pc);
				if (curr != null) {
					curr.stackSize--;
					if (curr.stackSize <= 0) {
						p.inventory.setItemStack(null);
					}
					p.inventory.markDirty();
				}
			} else {
				IELogger.info("(IndustrialWires) Failed to load panel component send by " + p.getDisplayNameString());
			}
			break;
		case REMOVE:
			int id = nbt.getInteger("id");
			if (id >= 0 && id < components.size() && p.inventory.getItemStack() == null) {
				PanelComponent removed = components.get(id);
				ItemStack remItem = ItemPanelComponent.stackFromComponent(removed);
				p.inventory.setItemStack(remItem);
				p.inventory.markDirty();
				components.remove(id);
			}
			break;
		case CREATE_PANEL:
			if (ApiUtils.compareToOreName(inv[0], "plateIron")) {
				NBTTagCompound panelNBT = new NBTTagCompound();
				writeToItemNBT(panelNBT, true);
				ItemStack panel = new ItemStack(IndustrialWires.panel, 1, BlockTypes_Panel.TOP.ordinal());
				panel.setTagCompound(panelNBT);
				inv[0] = panel;
				components.clear();
			}
			break;
		case REMOVE_ALL:
			Iterator<PanelComponent> it = components.iterator();
			while (it.hasNext()) {
				PanelComponent next = it.next();
				ItemStack nextStack = ItemPanelComponent.stackFromComponent(next);
				if (nextStack != null) {
					if (p.inventory.addItemStackToInventory(nextStack)) {
						it.remove();
					}
				} else {
					it.remove();
				}
			}
			break;
		}
		markDirty();
		IBlockState state = worldObj.getBlockState(pos);
		worldObj.notifyBlockUpdate(pos, state, state, 3);
	}

	private static final AxisAlignedBB aabb = new AxisAlignedBB(0, 0,0, 1, 14/16D, 1);
	@Override
	public AxisAlignedBB getBoundingBox() {
		return aabb;
	}
}
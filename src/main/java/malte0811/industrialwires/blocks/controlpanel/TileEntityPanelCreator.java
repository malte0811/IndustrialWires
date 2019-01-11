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

package malte0811.industrialwires.blocks.controlpanel;

import malte0811.industrialwires.IndustrialWires;
import malte0811.industrialwires.blocks.IBlockBoundsIW;
import malte0811.industrialwires.blocks.INetGUI;
import malte0811.industrialwires.blocks.TileEntityIWBase;
import malte0811.industrialwires.controlpanel.MessageType;
import malte0811.industrialwires.controlpanel.PanelComponent;
import malte0811.industrialwires.controlpanel.PanelUtils;
import malte0811.industrialwires.items.ItemPanelComponent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TileEntityPanelCreator extends TileEntityIWBase implements INetGUI, IBlockBoundsIW {
	public List<PanelComponent> components = new ArrayList<>();
	@Nonnull
	public ItemStack inv = ItemStack.EMPTY;

	@Override
	public void readNBT(NBTTagCompound nbt, boolean updatePacket) {
		NBTTagList l = nbt.getTagList("components", 10);
		PanelUtils.readListFromNBT(l, components);
		NBTTagCompound invTag;
		if (nbt.hasKey("inventory", 9)) {
			invTag = nbt.getTagList("inventory", 10).getCompoundTagAt(0);
		} else {
			invTag = nbt.getCompoundTag("inventory");
		}
		inv = new ItemStack(invTag);
	}

	@Override
	public void writeNBT(NBTTagCompound nbt, boolean updatePacket) {
		writeToItemNBT(nbt, false);
		nbt.setTag("inventory", inv.serializeNBT());
	}

	public void writeToItemNBT(NBTTagCompound nbt, boolean toItem) {
		NBTTagList comps = new NBTTagList();
		for (PanelComponent p : components) {
			NBTTagCompound nbtInner = new NBTTagCompound();
			p.writeToNBT(nbtInner, toItem);
			comps.appendTag(nbtInner);
		}
		nbt.setTag("components", comps);
		nbt.setFloat("height", PanelUtils.getHeight(inv));
		nbt.setFloat("angle", PanelUtils.getAngle(inv));
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
				components.add(pc);
				if (!curr.isEmpty()) {
					curr.shrink(1);
					if (curr.getCount() <= 0) {
						p.inventory.setItemStack(ItemStack.EMPTY);
					}
					p.inventory.markDirty();
				}
			} else {
				IndustrialWires.logger.info("(IndustrialWires) Failed to load panel component send by " + p.getDisplayNameString());
			}
			break;
		case REMOVE:
			int id = nbt.getInteger("id");
			if (id >= 0 && id < components.size() && p.inventory.getItemStack().isEmpty()) {
				PanelComponent removed = components.get(id);
				ItemStack remItem = ItemPanelComponent.stackFromComponent(removed);
				p.inventory.setItemStack(remItem);
				p.inventory.markDirty();
				components.remove(id);
			}
			break;
		case CREATE_PANEL:
			if (ItemStack.areItemsEqual(PanelUtils.getPanelBase(), inv) && !components.isEmpty()) {
				float height = PanelUtils.getHeight(inv);
				float angle = PanelUtils.getAngle(inv);
				boolean valid = true;
				for (PanelComponent comp : components) {
					if (!comp.isValidPos(components, height, angle)) {
						valid = false;
						break;
					}
				}
				if (valid) {
					NBTTagCompound panelNBT;
					if (inv.hasTagCompound()) {
						panelNBT = inv.getTagCompound().copy();
					} else {
						panelNBT = new NBTTagCompound();
					}
					writeToItemNBT(panelNBT, true);
					ItemStack panel = new ItemStack(IndustrialWires.panel, 1, BlockTypes_Panel.TOP.ordinal());
					panel.setTagCompound(panelNBT);
					inv = panel;
					components.clear();
				}
			}
			break;
		case REMOVE_ALL:
			Iterator<PanelComponent> it = components.iterator();
			while (it.hasNext()) {
				PanelComponent next = it.next();
				ItemStack nextStack = ItemPanelComponent.stackFromComponent(next);
				if (!nextStack.isEmpty()) {
					if (p.inventory.addItemStackToInventory(nextStack)) {
						it.remove();
					}
				} else {
					it.remove();
				}
			}
			break;
		case DISASSEMBLE:
			if (components.size() == 0 && inv.getItem() == PanelUtils.PANEL_ITEM) {
				TileEntityPanel te = new TileEntityPanel();
				te.readFromItemNBT(inv.getTagCompound());
				components = new ArrayList<>(te.getComponents());
				inv = ItemStack.EMPTY;
			}
			break;
		}
		markDirty();
		IBlockState state = world.getBlockState(pos);
		world.notifyBlockUpdate(pos, state, state, 3);
	}

	private static final AxisAlignedBB aabb = new AxisAlignedBB(0, 0, 0, 1, 14 / 16D, 1);

	@Override
	public AxisAlignedBB getBoundingBox() {
		return aabb;
	}
}
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

package malte0811.industrialWires.client.gui;

import blusunrize.immersiveengineering.client.ClientUtils;
import com.google.common.collect.ImmutableList;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.controlpanel.TileEntityPanelCreator;
import malte0811.industrialWires.containers.ContainerPanelCreator;
import malte0811.industrialWires.controlpanel.MessageType;
import malte0811.industrialWires.controlpanel.PanelComponent;
import malte0811.industrialWires.items.ItemPanelComponent;
import malte0811.industrialWires.network.MessageGUIInteract;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;

import java.io.IOException;
import java.util.List;

public class GuiPanelCreator extends GuiContainer {
	public int panelSize = 128;
	private ContainerPanelCreator container;
	private boolean snapToGrid = false;
	private ResourceLocation textureLoc = new ResourceLocation(IndustrialWires.MODID, "textures/gui/panel_creator.png");

	public GuiPanelCreator(InventoryPlayer ip, TileEntityPanelCreator te) {
		super(new ContainerPanelCreator(ip, te));
		container = (ContainerPanelCreator) inventorySlots;
		ySize = 231;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1, 1, 1, 1);
		mc.getTextureManager().bindTexture(textureLoc);
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		int x0 = getX0();
		int y0 = getY0();
		int xRel = mouseX - x0;
		int yRel = mouseY - y0;
		if (snapToGrid) {
			xRel = (int) Math.floor(xRel * 16 / panelSize) * panelSize / 16;
			yRel = (int) Math.floor(yRel * 16 / panelSize) * panelSize / 16;
		}
		for (PanelComponent pc : container.tile.components) {
			drawPanelComponent(pc, -1, -1);
		}
		PanelComponent curr = getFloatingPC();
		if (curr != null && 0 <= xRel && xRel <= panelSize && 0 <= yRel && yRel <= panelSize) {
			drawPanelComponent(curr, xRel, yRel);
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		String tooltip = null;
		if (buttonList.get(0).isMouseOver()) {
			tooltip = I18n.format(IndustrialWires.MODID + ".desc.create_panel");
			ClientUtils.drawHoveringText(ImmutableList.of("Create a new panel"), mouseX, mouseY, mc.fontRenderer);
		} else if (buttonList.get(1).isMouseOver()) {
			tooltip = I18n.format(IndustrialWires.MODID + ".desc.remove_all");
		} else if (buttonList.get(2).isMouseOver()) {
			if (snapToGrid) {
				tooltip = I18n.format(IndustrialWires.MODID + ".desc.disable_snap");
			} else {
				tooltip = I18n.format(IndustrialWires.MODID + ".desc.enable_snap");
			}
		} else if (buttonList.get(3).isMouseOver()) {
			tooltip = I18n.format(IndustrialWires.MODID + ".desc.disassemble");
		}
		if (tooltip != null) {
			ClientUtils.drawHoveringText(ImmutableList.of(tooltip), mouseX, mouseY, mc.fontRenderer);
		}
	}

	private void drawPanelComponent(PanelComponent pc, int x, int y) {
		if (x >= 0 && y >= 0) {
			pc.setX(x / (float) panelSize);
			pc.setY(y / (float) panelSize);
		}
		if (!pc.isValidPos(container.tile.components)) {
			AxisAlignedBB aabb = pc.getBlockRelativeAABB();
			int left = (int) (getX0() + aabb.minX * panelSize) - 1;
			int top = (int) (getY0() + aabb.minZ * panelSize) - 1;
			int right = (int) (getX0() + aabb.maxX * panelSize) + 1;
			int bottom = (int) (getY0() + aabb.maxZ * panelSize) + 1;
			Gui.drawRect(left, top, right, bottom, 0xffff0000);
		}
		pc.renderInGUI(this);
		GlStateManager.color(1, 1, 1);
	}

	@Override
	public void initGui() {
		super.initGui();
		buttonList.clear();
		int buttonTop = guiTop + 62;
		buttonList.add(new GuiButton(0, guiLeft + 2, buttonTop, 20, 20, "C"));
		buttonList.add(new GuiButton(1, guiLeft + 2, buttonTop + 22, 20, 20, "R"));
		buttonList.add(new GuiButton(2, guiLeft + 2, buttonTop + 44, 20, 20, "S"));
		buttonList.add(new GuiButton(3, guiLeft + 2, buttonTop - 54, 20, 20, "D"));
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		int x0 = getX0();
		int y0 = getY0();
		int xRel = mouseX - x0;
		int yRel = mouseY - y0;
		PanelComponent curr = getFloatingPC();
		if (0 <= xRel && xRel <= panelSize && 0 <= yRel && yRel <= panelSize) {
			List<PanelComponent> components = container.tile.components;
			if (curr != null) {
				if (curr.isValidPos(components)) {
					NBTTagCompound nbt = new NBTTagCompound();
					nbt.setFloat("x", curr.getX());
					nbt.setFloat("y", curr.getY());
					nbt.setInteger("type", MessageType.ADD.ordinal());
					IndustrialWires.packetHandler.sendToServer(new MessageGUIInteract(container.tile, nbt));
					components.add(curr.copyOf());
					ItemStack currStack = mc.player.inventory.getItemStack();
					if (!currStack.isEmpty()) {
						currStack.shrink(1);
						if (currStack.getCount() <= 0) {
							mc.player.inventory.setItemStack(ItemStack.EMPTY);

						}
					}
				}
			} else if (mc.player.inventory.getItemStack().isEmpty()) {
				float xRelFloat = xRel / (float) panelSize;
				float yRelFloat = yRel / (float) panelSize;
				for (int i = 0; i < components.size(); i++) {
					PanelComponent pc = components.get(i);
					AxisAlignedBB aabb = pc.getBlockRelativeAABB();
					if (aabb.minX <= xRelFloat && aabb.maxX > xRelFloat && aabb.minZ <= yRelFloat && aabb.maxZ > yRelFloat) {
						PanelComponent removed = components.get(i);
						ItemStack remItem = ItemPanelComponent.stackFromComponent(removed);
						mc.player.inventory.setItemStack(remItem);
						NBTTagCompound nbt = new NBTTagCompound();
						nbt.setInteger("type", MessageType.REMOVE.ordinal());
						nbt.setInteger("id", i);
						IndustrialWires.packetHandler.sendToServer(new MessageGUIInteract(container.tile, nbt));
						break;
					}
				}
			}
		}
	}

	public int getX0() {
		return 30 + guiLeft;
	}

	public int getY0() {
		return 6 + guiTop;
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		NBTTagCompound nbt = new NBTTagCompound();
		switch (button.id) {
		case 0://create panel
			nbt.setInteger("type", MessageType.CREATE_PANEL.ordinal());
			break;
		case 1:// Delete all
			nbt.setInteger("type", MessageType.REMOVE_ALL.ordinal());
			break;
		case 2:
			snapToGrid = !snapToGrid;
			break;
		case 3:
			nbt.setInteger("type", MessageType.DISASSEMBLE.ordinal());
			break;
		}
		if (!nbt.hasNoTags()) {
			IndustrialWires.packetHandler.sendToServer(new MessageGUIInteract(container.tile, nbt));
		}
	}

	private ItemStack lastFloating = ItemStack.EMPTY;
	private PanelComponent lastFloatingPC;

	private PanelComponent getFloatingPC() {
		ItemStack floating = mc.player.inventory.getItemStack();
		if (floating.isEmpty() || floating.getItem() != IndustrialWires.panelComponent) {
			return null;
		}
		if (ItemStack.areItemStacksEqual(floating, lastFloating)) {
			return lastFloatingPC;
		}
		lastFloating = floating.copy();
		lastFloatingPC = ItemPanelComponent.componentFromStack(floating);
		return lastFloatingPC;
	}
}
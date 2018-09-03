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

package malte0811.industrialwires.client.gui;

import blusunrize.immersiveengineering.client.ClientUtils;
import com.google.common.collect.ImmutableList;
import malte0811.industrialwires.IndustrialWires;
import malte0811.industrialwires.blocks.controlpanel.TileEntityPanelCreator;
import malte0811.industrialwires.containers.ContainerPanelCreator;
import malte0811.industrialwires.controlpanel.MessageType;
import malte0811.industrialwires.controlpanel.PanelComponent;
import malte0811.industrialwires.controlpanel.PanelUtils;
import malte0811.industrialwires.items.ItemPanelComponent;
import malte0811.industrialwires.network.MessageGUIInteract;
import net.minecraft.client.Minecraft;
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
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class GuiPanelCreator extends GuiContainer {
	public int panelSize = 128;
	private ContainerPanelCreator container;
	private int snapToGrid = 0;
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
		for (PanelComponent pc : container.tile.components) {
			drawPanelComponent(pc, -1, -1);
		}
		int x0 = getX0();
		int y0 = getY0();
		int xRel = mouseX - x0;
		int yRel = mouseY - y0;
		PanelComponent curr = getFloatingPC();
		if (curr != null && 0 <= xRel && xRel <= panelSize && 0 <= yRel && yRel <= panelSize) {
			Runnable after = ()->{};
			if (snapToGrid != 0) {
				curr.setX(xRel/(float)panelSize);
				curr.setY(yRel/(float)panelSize);
				BiFunction<Integer, Integer, Integer> right = (a, b)->b;
				BiFunction<Integer, Integer, Integer> left = (a, b)->a;
				Function<PanelComponent, Double> xSize = (pc)->{
					AxisAlignedBB aabb = pc.getBlockRelativeAABB();
					return aabb.maxX-aabb.minX;
				};
				Function<PanelComponent, Double> ySize = (pc)->{
					AxisAlignedBB aabb = pc.getBlockRelativeAABB();
					return aabb.maxZ-aabb.minZ;
				};
				Pair<Integer, Runnable> xSnap = snapToGrid(xRel, curr, PanelComponent::getX, PanelComponent::getY,
						xSize, ySize, left, right);
				xRel = xSnap.getLeft();
				Pair<Integer, Runnable> ySnap = snapToGrid(yRel, curr, PanelComponent::getY, PanelComponent::getX,
						ySize, xSize, right, left);

				yRel = ySnap.getLeft();
				after = ()->{
					xSnap.getRight().run();
					ySnap.getRight().run();
				};
			}
			drawPanelComponent(curr, xRel, yRel);
			after.run();
		}
	}

	private Pair<Integer, Runnable> snapToGrid(int mouse, PanelComponent toPlace, Function<PanelComponent, Float> pos, Function<PanelComponent, Float> pos2,
											   Function<PanelComponent, Double> size,Function<PanelComponent, Double> size2,
											   BiFunction<Integer, Integer, Integer> getY, BiFunction<Integer, Integer, Integer> getX) {
		List<PanelComponent> components = container.tile.components;
		if (snapToGrid==2&&!components.isEmpty()) {
			List<Pair<PanelComponent, Double>> compLefts = new ArrayList<>(components.size());
			List<Pair<PanelComponent, Double>> compCenters = new ArrayList<>(components.size());
			List<Pair<PanelComponent, Double>> compRights = new ArrayList<>(components.size());
			for (PanelComponent pc : components) {
				double compLeft = pos.apply(pc);
				double compSize = size.apply(pc);
				compLefts.add(new ImmutablePair<>(pc, compLeft));
				compRights.add(new ImmutablePair<>(pc, compLeft + compSize));
				compCenters.add(new ImmutablePair<>(pc, compLeft + compSize / 2));
			}
			double mainLeft = pos.apply(toPlace);
			double mainSize = size.apply(toPlace);
			double mainRight = mainLeft + mainSize;
			double mainCenter = (mainRight + mainLeft) / 2;
			Triple<PanelComponent, ComponentSnapType, Double> min = getMinDist(compLefts, mainLeft, mainCenter, mainRight);

			{
				Triple<PanelComponent, ComponentSnapType, Double> tmpMin = getMinDist(compCenters, mainLeft, mainCenter, mainRight);
				if (Math.abs(tmpMin.getRight()) < Math.abs(min.getRight())) {
					min = tmpMin;
				}

				tmpMin = getMinDist(compRights, mainLeft, mainCenter, mainRight);
				if (Math.abs(tmpMin.getRight()) < Math.abs(min.getRight())) {
					min = tmpMin;
				}
			}

			if (Math.abs(min.getRight())<.5/16) {
				int ret = (int)(mouse+min.getRight()*panelSize);
				PanelComponent snappedTo = min.getLeft();
				ComponentSnapType type = min.getMiddle();
				return new ImmutablePair<>(ret,()->{
					int hor1, hor2;
					float posOther = pos2.apply(toPlace);
					hor1 = Math.round(Math.min(posOther, pos2.apply(snappedTo))*panelSize);
					hor2 = (int) Math.round(Math.max(posOther+size2.apply(toPlace), pos2.apply(snappedTo)+size2.apply(snappedTo))*panelSize);
					int vert1 = (int) (ret+(.5*type.ordinal())*mainSize*panelSize);
					int vert2 = vert1+1;
					int x0 = getX0(), y0 = getY0();
					drawRect(x0+getX.apply(hor1, vert1), y0+getY.apply(hor1, vert1), x0+getX.apply(hor2, vert2),
							y0+getY.apply(hor2, vert2), 0xff666666);
				});
			}
		}
		if (snapToGrid!=0) {
			mouse = Math.round(mouse * 16 / panelSize) * panelSize / 16;
		}
		return new ImmutablePair<>(mouse, ()->{});
	}


	private Triple<PanelComponent, ComponentSnapType, Double> getMinDist(List<Pair<PanelComponent, Double>> comps,
																		 double left, double center, double right) {
		Pair<PanelComponent, Double> tmpMin = Collections.min(comps, Comparator.comparingDouble(a -> Math.abs(a.getRight() - left)));
		Triple<PanelComponent, ComponentSnapType, Double> totalMin = new ImmutableTriple<>(tmpMin.getLeft(), ComponentSnapType.LEFT,
				tmpMin.getRight()-left);

		tmpMin = Collections.min(comps, Comparator.comparingDouble(a -> Math.abs(a.getRight() - center)));
		if (Math.abs(tmpMin.getRight() - center)<Math.abs(totalMin.getRight())) {
			totalMin = new ImmutableTriple<>(tmpMin.getLeft(), ComponentSnapType.CENTER,
					tmpMin.getRight()-center);
		}

		tmpMin = Collections.min(comps, Comparator.comparingDouble(a -> Math.abs(a.getRight() - right)));
		if (Math.abs(tmpMin.getRight() - right)<Math.abs(totalMin.getRight())) {
			totalMin = new ImmutableTriple<>(tmpMin.getLeft(), ComponentSnapType.RIGHT,
					tmpMin.getRight()-right);
		}
		return totalMin;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.renderHoveredToolTip(mouseX, mouseY);
		String tooltip = null;
		if (buttonList.get(0).isMouseOver()) {
			tooltip = I18n.format(IndustrialWires.MODID + ".desc.create_panel");
			ClientUtils.drawHoveringText(ImmutableList.of("Create a new panel"), mouseX, mouseY, mc.fontRenderer);
		} else if (buttonList.get(1).isMouseOver()) {
			tooltip = I18n.format(IndustrialWires.MODID + ".desc.remove_all");
		} else if (buttonList.get(2).isMouseOver()) {
			tooltip = I18n.format(IndustrialWires.MODID + ".desc.snap"+snapToGrid);
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
		ItemStack unfinishedPanel = container.getInventory().get(0);
		boolean red = (512 & (Minecraft.getSystemTime())) != 0;
		if (red && !pc.isValidPos(container.tile.components, PanelUtils.getHeight(unfinishedPanel), PanelUtils.getAngle(unfinishedPanel))) {
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
				ItemStack unfinishedPanel = container.getInventory().get(0);
				if (curr.isValidPos(components, PanelUtils.getHeight(unfinishedPanel), PanelUtils.getAngle(unfinishedPanel))) {
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
			snapToGrid = (snapToGrid+1)%3;
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

	private enum ComponentSnapType {
		LEFT,
		CENTER,
		RIGHT;
	}
}
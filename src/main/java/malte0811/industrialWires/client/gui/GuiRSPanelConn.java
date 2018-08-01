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

package malte0811.industrialWires.client.gui;

import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.controlpanel.TileEntityRSPanel;
import malte0811.industrialWires.client.gui.elements.GuiIntChooser;
import malte0811.industrialWires.containers.ContainerRSPanelConn;
import malte0811.industrialWires.network.MessageGUIInteract;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class GuiRSPanelConn extends GuiContainer {
	private TileEntityRSPanel te;
	private GuiIntChooser chooser;

	public GuiRSPanelConn(TileEntityRSPanel tile) {
		super(new ContainerRSPanelConn(tile));
		te = tile;
	}

	@Override
	public void initGui() {
		super.initGui();
		chooser = new GuiIntChooser((width - 32) / 2, (height - 4) / 2, false, te.getRsId(), 2);
		xSize = 64;
		ySize = 64;
		guiLeft = (width - xSize) / 2;
		guiTop = (height - ySize) / 2;
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		chooser.click(mouseX, mouseY);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.renderHoveredToolTip(mouseX, mouseY);
		GlStateManager.color(1, 1, 1, 1);
		RenderHelper.disableStandardItemLighting();
		chooser.drawChooser();
		RenderHelper.enableStandardItemLighting();
	}

	private ResourceLocation textureLoc = new ResourceLocation(IndustrialWires.MODID, "textures/gui/rs_wire_controller.png");

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1, 1, 1, 1);
		mc.getTextureManager().bindTexture(textureLoc);
		Gui.drawModalRectWithCustomSizedTexture(guiLeft, guiTop, 0, 0, xSize, ySize, 64, 64);
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		onChange();
	}

	private void onChange() {
		if (chooser.getValue() != te.getRsId()) {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setInteger("rsId", chooser.getValue());
			IndustrialWires.packetHandler.sendToServer(new MessageGUIInteract(te, nbt));
		}
	}
}

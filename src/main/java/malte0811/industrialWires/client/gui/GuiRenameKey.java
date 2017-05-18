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

import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.containers.ContainerRenameKey;
import malte0811.industrialWires.network.MessageItemSync;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class GuiRenameKey extends GuiContainer {
	private EnumHand hand;
	private GuiTextField field;

	public GuiRenameKey(EnumHand h) {
		super(new ContainerRenameKey(h));
		hand = h;
	}

	@Override
	public void initGui() {
		super.initGui();
		field = new GuiTextField(0, mc.fontRenderer, (width-58)/2, (height-12)/2, 58, 12);
		ItemStack held = mc.player.getHeldItem(hand);
		NBTTagCompound nbt = held.getTagCompound();
		if (nbt!=null&&nbt.hasKey("name")) {
			field.setText(nbt.getString("name"));
		}
		xSize = 64;
		ySize = 64;
		guiLeft = (width - xSize) / 2;
		guiTop = (height - ySize) / 2;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		GlStateManager.color(1, 1, 1, 1);
		RenderHelper.disableStandardItemLighting();
		field.drawTextBox();
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
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		field.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (!field.textboxKeyTyped(typedChar, keyCode)) {
			super.keyTyped(typedChar, keyCode);
		}
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		if (!field.getText().isEmpty()) {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setString("name", field.getText());
			IndustrialWires.packetHandler.sendToServer(new MessageItemSync(hand, nbt));
		}
	}

}

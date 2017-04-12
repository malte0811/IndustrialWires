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
import malte0811.industrialWires.blocks.controlpanel.TileEntityRSPanelConn;
import malte0811.industrialWires.network.MessageGUIInteract;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;

import java.io.IOException;

public class GuiRSPanelConn extends GuiScreen {
	private TileEntityRSPanelConn te;
	private int curr = 0;
	public GuiRSPanelConn(TileEntityRSPanelConn tile) {
		te = tile;
	}
	@Override
	public void initGui() {
		super.initGui();
		buttonList.clear();
		buttonList.add(new GuiButton(0, width/2-8, height/2-32, 16, 16, "+1"));
		buttonList.add(new GuiButton(1, width/2-8, height/2+32, 16, 16, "-1"));
		buttonList.add(new GuiButton(2, width/2-8, height/2+48, 16, 16, "Ok"));
		curr = te.getRsId();
		onChange();
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		switch (button.id) {
		case 0:
			curr++;
			onChange();
			break;
		case 1:
			curr--;
			onChange();
			break;
		case 2:
			mc.displayGuiScreen(null);
			mc.setIngameFocus();
			break;
		}
	}

	@Override
	public void drawBackground(int tint) {
		super.drawBackground(tint);
		//TODO proper background
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	private void onChange() {
		curr = Math.max(0, curr);
		labelList.clear();
		labelList.add(new GuiLabel(mc.fontRendererObj, 0, width/2-8, height/2-8, 16, 16, 0xff0000));
		labelList.get(0).addLine(Integer.toString(curr));
		if (curr!=te.getRsId()) {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setInteger("rsId", curr);
			IndustrialWires.packetHandler.sendToServer(new MessageGUIInteract(te, nbt));
		}
	}
}

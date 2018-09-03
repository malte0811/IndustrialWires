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

package malte0811.industrialwires.client.gui.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.EnumDyeColor;

import javax.annotation.Nonnull;

public class GuiChannelPicker extends GuiButton {
	protected byte selected;
	protected byte currHovered;

	public GuiChannelPicker(int id, int x, int y, int size, byte selectedChannel) {
		super(id, x, y, size, size, "");
		selected = selectedChannel;
	}

	@Override
	public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float patrtialTicks) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		mouseX -= x;
		mouseY -= y;
		currHovered = -1;
		for (byte i = 0; i < 16; i++) {
			int xMin = width / 4 * (i % 4);
			int yMin = height / 4 * (i / 4);
			int xMax = xMin + width / 4;
			int yMax = yMin + height / 4;
			EnumDyeColor color = EnumDyeColor.byMetadata(i);
			int colorVal = color.getColorValue() | 0xff000000;
			if (mouseX >= xMin && mouseX < xMax && mouseY >= yMin && mouseY < yMax) {
				currHovered = i;
			}
			if (selected == i) {
				drawRect(xMin + x, yMin + y, xMax + x, yMax + y, 0xff000000 | ~colorVal);
			}
			if (currHovered == i) {
				drawRect(xMin + x, yMin + y, xMax + x, yMax + y, colorVal);
			} else {
				final int offset = width / 20;
				drawRect(xMin + offset + x, yMin + offset + y, xMax - offset + x, yMax - offset + y, colorVal);
			}
		}
	}

	public boolean click(int xMouse, int yMouse) {
		if (x <= xMouse && x + width >= xMouse && y <= yMouse && y + height >= yMouse) {
			select();
			return true;
		}
		return false;
	}

	protected void select() {
		if (currHovered >= 0) {
			selected = currHovered;
		}
	}

	public byte getSelected() {
		return selected;
	}

	public boolean isHovered(int xMouse, int yMouse) {
		return x <= xMouse && x + width >= xMouse && y <= yMouse && y + height >= yMouse;
	}
}

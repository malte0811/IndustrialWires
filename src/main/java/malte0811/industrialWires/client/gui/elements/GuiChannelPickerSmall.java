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

package malte0811.industrialWires.client.gui.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.EnumDyeColor;

import javax.annotation.Nonnull;

public class GuiChannelPickerSmall extends GuiChannelPicker {
	private boolean open = false;
	private int offSize, onSize;

	public GuiChannelPickerSmall(int id, int x, int y, int offSize, int onSize, byte selectedChannel) {
		super(id, x, y, offSize, selectedChannel);
		selected = selectedChannel;
		this.onSize = onSize;
		this.offSize = offSize;
	}

	@Override
	public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		if (open) {
			super.drawButton(mc, mouseX, mouseY, partialTicks);
		} else {
			EnumDyeColor color = EnumDyeColor.byMetadata(selected);
			int colorVal = color.getColorValue() | 0xff000000;
			drawRect(x, y, x + width, y + height, colorVal);
		}
	}

	@Override
	public boolean click(int xMouse, int yMouse) {
		if (!open) {
			if (x <= xMouse && x + width >= xMouse && y <= yMouse && y + height >= yMouse) {
				open = true;
				width = onSize;
				height = onSize;
				return true;
			}
			return false;
		} else {
			boolean ret = false;
			if (x <= xMouse && x + width >= xMouse && y <= yMouse && y + height >= yMouse) {
				select();
				ret = true;
			}
			open = false;
			width = offSize;
			height = offSize;
			return ret;
		}
	}
}

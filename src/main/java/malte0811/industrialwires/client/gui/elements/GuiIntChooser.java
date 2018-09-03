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
import net.minecraft.client.gui.Gui;

public class GuiIntChooser extends Gui {
	private boolean allowNegative;
	private int value;
	private int xPos, yPos;
	private int xPlus;
	private int max;
	private String format;
	private Minecraft mc = Minecraft.getMinecraft();

	public GuiIntChooser(int x, int y, boolean neg, int initialValue, int digits) {
		allowNegative = neg;
		value = initialValue;
		xPos = x;
		yPos = y;
		max = (int) Math.pow(10, digits) - 1;
		xPlus = x + mc.fontRenderer.getCharWidth('0') * (digits + (allowNegative ? 1 : 0)) + mc.fontRenderer.getCharWidth('-') + 2;
		format = "%" + digits + "s";
	}

	public void drawChooser() {
		int color = 0xE0E0E0;
		String val = String.format(format, Integer.toString(Math.abs(value))).replace(' ', '0');
		if (allowNegative) {
			if (value > 0) {
				val = "+" + val;
			} else if (value < 0) {
				val = "-" + val;
			} else {
				val = "0" + val;
			}
		}
		mc.fontRenderer.drawStringWithShadow("-", xPos, yPos, color);
		mc.fontRenderer.drawStringWithShadow("+", xPlus, yPos, color);
		color = 0x9999ff;
		if (allowNegative&&value!=0) {
			color = value<0?0xff9999:0x99ff99;
		}
		mc.fontRenderer.drawStringWithShadow(val, xPos + mc.fontRenderer.getCharWidth('-') + 1, yPos, color);
	}

	public void click(int x, int y) {
		int height = mc.fontRenderer.FONT_HEIGHT;
		if (y >= yPos && y < yPos + height) {
			if (x >= xPlus && x < xPlus + mc.fontRenderer.getCharWidth('+')) {
				if (value < max) {
					value++;
				}
			} else if (x >= xPos && x <= xPos + mc.fontRenderer.getCharWidth('-')) {
				if (value > (allowNegative ? -1 : 0)) {
					value--;
				}
			}
		}
	}

	public int getValue() {
		return value;
	}

	public boolean isMouseOver(int mX, int mY) {
		return mX >= xPos && mX < xPlus + mc.fontRenderer.getCharWidth('V') && mY >= yPos && mY < yPos + mc.fontRenderer.FONT_HEIGHT;
	}
}

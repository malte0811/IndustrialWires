package malte0811.industrialWires.client.gui.elements;

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
		String val = String.format(format, Integer.toString(value)).replace(' ', '0');
		if (value >= 0 && allowNegative) {
			val = "+" + val;
		}
		mc.fontRenderer.drawStringWithShadow(val, xPos + mc.fontRenderer.getCharWidth('-') + 1, yPos, color);
		mc.fontRenderer.drawStringWithShadow("-", xPos, yPos, color);
		mc.fontRenderer.drawStringWithShadow("+", xPlus, yPos, color);
	}

	public void click(int x, int y) {
		int height = mc.fontRenderer.FONT_HEIGHT;
		if (y >= yPos && y < yPos + height) {
			if (x >= xPlus && x < xPlus + mc.fontRenderer.getCharWidth('+')) {
				if (value < max) {
					value++;
				}
			} else if (x >= xPos && x <= xPos + mc.fontRenderer.getCharWidth('-')) {
				if (value > (allowNegative ? -value : 0)) {
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

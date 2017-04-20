package malte0811.industrialWires.client.gui.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

public class GuiIntChooser extends Gui {
	private boolean allowNegative;
	private int value;
	private int xPos, yPos;
	private int xBtn;
	private int max;
	private Minecraft mc = Minecraft.getMinecraft();
	public GuiIntChooser(int x, int y, boolean neg, int initialValue, int digits) {
		allowNegative = neg;
		value = initialValue;
		xPos = x;
		yPos = y;
		max = (int) Math.pow(10, digits)-1;
		xBtn = x+mc.fontRendererObj.getCharWidth('0')*(digits+1);
	}
	public void drawChooser() {
		int color = 0xE0E0E0;
		mc.fontRendererObj.drawStringWithShadow(Integer.toString(value), xPos, yPos, color);
		//TODO nicer buttons
		mc.fontRendererObj.drawStringWithShadow("^", xBtn, yPos, color);
		mc.fontRendererObj.drawStringWithShadow("V", xBtn, yPos+mc.fontRendererObj.FONT_HEIGHT/2, color);
	}

	public void click(int x, int y) {
		int height = mc.fontRendererObj.FONT_HEIGHT;
		if (x>=xBtn&&x<xBtn+mc.fontRendererObj.getCharWidth('V')) {
			if (y>=yPos&&y<yPos+height/2) {
				if (value<max) {
					value++;
				}
			} else if (y<yPos+height&&y>=yPos+height/2) {
				if (allowNegative||value>0) {
					value--;
				}
			}
		}
	}

	public int getValue() {
		return value;
	}

	public boolean isMouseOver(int mX, int mY) {
		return mX>=xPos&&mX<xBtn+mc.fontRendererObj.getCharWidth('V')&&mY>=yPos&&mY<yPos+mc.fontRendererObj.FONT_HEIGHT;
	}
}

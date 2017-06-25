package malte0811.industrialWires.client.gui.elements;

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
	public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY) {
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
			int colorVal = color.getMapColor().colorValue | 0xff000000;
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

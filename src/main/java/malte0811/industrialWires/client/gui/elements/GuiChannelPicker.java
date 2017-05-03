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
		mouseX-=xPosition;
		mouseY-=yPosition;
		currHovered = -1;
		for (byte i = 0;i<16;i++) {
			int xMin = width/4*(i%4);
			int yMin = height/4*(i/4);
			int xMax = xMin+width/4;
			int yMax = yMin+height/4;
			EnumDyeColor color = EnumDyeColor.byMetadata(i);
			int colorVal = color.getMapColor().colorValue|0xff000000;
			if (mouseX>=xMin&&mouseX<xMax&&mouseY>=yMin&&mouseY<yMax) {
				currHovered = i;
			}
			if (selected==i) {
				drawRect(xMin+xPosition, yMin+yPosition, xMax+xPosition, yMax+yPosition, 0xff000000|~colorVal);
			}
			if (currHovered==i) {
				drawRect(xMin+xPosition, yMin+yPosition, xMax+xPosition, yMax+yPosition, colorVal);
			} else {
				final int offset = width/20;
				drawRect(xMin+offset+xPosition, yMin+offset+yPosition, xMax-offset+xPosition, yMax-offset+yPosition, colorVal);
			}
		}
	}

	public boolean click(int x, int y) {
		if (xPosition<=x&&xPosition+width>=x&&yPosition<=y&&yPosition+height>=y) {
			select();
			return true;
		}
		return false;
	}

	protected void select() {
		if (currHovered>=0) {
			selected = currHovered;
		}
	}
	public byte getSelected() {
		return selected;
	}
	public boolean isHovered(int x, int y) {
		return xPosition<=x&&xPosition+width>=x&&yPosition<=y&&yPosition+height>=y;
	}
}

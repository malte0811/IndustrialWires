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

package malte0811.industrialWires.controlpanel;

import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.controlpanel.TileEntityPanel;
import malte0811.industrialWires.client.RawQuad;
import malte0811.industrialWires.client.gui.GuiPanelCreator;
import malte0811.industrialWires.client.panelmodel.RawModelFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static malte0811.industrialWires.util.NBTKeys.COLOR;
import static malte0811.industrialWires.util.NBTKeys.TEXT;

public class Label extends PanelComponent implements IConfigurableComponent {
	public static final ResourceLocation FONT = new ResourceLocation("minecraft", "textures/font/ascii.png");
	private String text = "Test";
	private RawModelFontRenderer renderer;
	private int color = 0x808080;

	public Label(String text, int color) {
		this();
		this.text = text;
		this.color = color;
	}

	public Label() {
		super("label");
	}

	@Override
	protected void writeCustomNBT(NBTTagCompound nbt, boolean toItem) {
		nbt.setString(TEXT, text);
		nbt.setInteger(COLOR, color);
	}

	@Override
	protected void readCustomNBT(NBTTagCompound nbt) {
		text = nbt.getString(TEXT);
		color = nbt.getInteger(COLOR);
	}

	@Override
	public List<RawQuad> getQuads() {
		RawModelFontRenderer render = fontRenderer();
		render.drawString(text, 0, 0, 0xff000000 | color);
		return render.build();
	}

	@Nonnull
	@Override
	public Label copyOf() {
		Label ret = new Label(text, color);
		ret.setX(x);
		ret.setY(y);
		ret.panelHeight = panelHeight;
		return ret;
	}

	@Nonnull
	@Override
	public AxisAlignedBB getBlockRelativeAABB() {
		if (aabb == null) {
			if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
				RawModelFontRenderer fr = fontRenderer();
				float width = fr.getStringWidth(text) * fr.scale;
				float height = fr.FONT_HEIGHT * fr.scale;
				aabb = new AxisAlignedBB(getX(), 0, getY(), getX() + width, 0, getY() + height);
			} else {
				aabb = new AxisAlignedBB(getX(), 0, getY(), getX() + .001, 0, getY() + .001);
			}
		}
		return aabb;
	}

	@Override
	public void interactWith(Vec3d hitRelative, TileEntityPanel tile, EntityPlayerMP player) {
	}

	@Override
	public void update(TileEntityPanel tile) {

	}

	@Override
	public int getColor() {
		return color;
	}

	@Override
	public float getHeight() {
		return 0;
	}

	private RawModelFontRenderer fontRenderer() {
		if (renderer == null) {
			renderer = new RawModelFontRenderer(Minecraft.getMinecraft().gameSettings, FONT, Minecraft.getMinecraft().getTextureManager(),
					false, 1);
		}
		return renderer;
	}

	@Override
	public void renderInGUI(GuiPanelCreator gui) {
		int left = (int) (gui.getX0() + getX() * gui.panelSize);
		int top = (int) (gui.getY0() + getY() * gui.panelSize);
		GlStateManager.pushMatrix();
		float scale = gui.mc.fontRenderer.FONT_HEIGHT * gui.panelSize / (16F * 9F * 9F);
		GlStateManager.translate(left, top, 0);
		GlStateManager.scale(scale, scale, scale);
		gui.mc.fontRenderer.drawString(text, 0, 0, 0xff000000 | color);
		GlStateManager.popMatrix();
	}

	@Override
	public void applyConfigOption(ConfigType type, int id, NBTBase value) {
		switch (type) {
		case STRING:
			text = ((NBTTagString) value).getString();
			break;
		case FLOAT:
			color = PanelUtils.setColor(color, id, value);
			break;
		}
	}

	@Nullable
	@Override
	public String fomatConfigName(ConfigType type, int id) {
		switch (type) {
		case FLOAT:
			return I18n.format(IndustrialWires.MODID + ".desc." + (id == 0 ? "red" : (id == 1 ? "green" : "blue")));
		default:
			return null;
		}
	}

	@Nullable
	@Override
	public String fomatConfigDescription(ConfigType type, int id) {
		switch (type) {
		case STRING:
			return I18n.format(IndustrialWires.MODID + ".desc.label_text");
		default:
			return null;
		}
	}

	@Override
	public StringConfig[] getStringOptions() {
		return new StringConfig[]{
				new StringConfig("text", 0, 0, text)
		};
	}

	@Override
	public FloatConfig[] getFloatOptions() {
		float[] color = PanelUtils.getFloatColor(true, this.color);
		return new FloatConfig[]{
				new FloatConfig("red", 0, 30, color[0], 60),
				new FloatConfig("green", 0, 50, color[1], 60),
				new FloatConfig("blue", 0, 70, color[2], 60)
		};
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		Label label = (Label) o;

		if (color != label.color) return false;
		return text.equals(label.text);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + text.hashCode();
		result = 31 * result + color;
		return result;
	}
}
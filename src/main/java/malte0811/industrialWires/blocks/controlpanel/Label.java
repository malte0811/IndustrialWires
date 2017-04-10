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

package malte0811.industrialWires.blocks.controlpanel;

import malte0811.industrialWires.client.RawQuad;
import malte0811.industrialWires.client.panelmodel.RawModelFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;
import java.util.List;

public class Label extends PanelComponent {
	private static final ResourceLocation font = new ResourceLocation("minecraft", "textures/font/ascii.png");
	String text;
	RawModelFontRenderer renderer;
	int color;

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
		nbt.setString("text", text);
		nbt.setInteger("color", color);
	}

	@Override
	protected void readCustomNBT(NBTTagCompound nbt) {
		text = nbt.getString("text");
		color = nbt.getInteger("color");
	}

	@Override
	public List<RawQuad> getQuads() {
		RawModelFontRenderer render = fontRenderer();
		render.drawString(text, 0, 0, 0xff000000|color);
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

	@Override
	public AxisAlignedBB getBlockRelativeAABB() {
		return null;
	}

	@Override
	public boolean interactWith(Vec3d hitRelative, TileEntityPanel tile) {
		return false;
	}

	@Override
	public void update(TileEntityPanel tile) {

	}

	@Override
	public float getHeight() {
		return 0;
	}

	private RawModelFontRenderer fontRenderer() {
		if (renderer==null) {
			renderer = new RawModelFontRenderer(Minecraft.getMinecraft().gameSettings, font, Minecraft.getMinecraft().getTextureManager(),
					false,  1);
		}
		return renderer;
	}

}
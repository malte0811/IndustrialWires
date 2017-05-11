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

package malte0811.industrialWires.client.panelmodel;

import com.google.common.collect.ImmutableList;
import malte0811.industrialWires.client.RawQuad;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.util.vector.Vector3f;

public class RawModelFontRenderer extends FontRenderer {
	float[] colorA = new float[4];
	private ImmutableList.Builder<RawQuad> builder = ImmutableList.builder();
	private final Vector3f normal = new Vector3f(0, 1, 0);
	public final float scale;

	private TextureAtlasSprite sprite;

	public RawModelFontRenderer(GameSettings settings, ResourceLocation font, TextureManager manager, boolean isUnicode, float scale) {
		super(settings, font, manager, isUnicode);
		this.scale = scale / (9 * 16);
		onResourceManagerReload(null);
	}

	@Override
	protected float renderDefaultChar(int pos, boolean italic) {
		float x = (pos % 16);
		float y = (pos / 16);
		float w = charWidth[pos] - 1.01f;
		float h = FONT_HEIGHT - 1.01f;
		float wt = w / 128f * 16;
		float ht = h / 128f * 16;
		float h0 = .01F;
		Vector3f v0 = new Vector3f(posX, h0, posY);
		v0.scale(scale);
		Vector3f v1 = new Vector3f(posX, h0, posY + h);
		v1.scale(scale);
		Vector3f v2 = new Vector3f(posX + w, h0, posY + h);
		v2.scale(scale);
		Vector3f v3 = new Vector3f(posX + w, h0, posY);
		v3.scale(scale);
		builder.add(new RawQuad(v0, v1, v2, v3,
				EnumFacing.UP, sprite, colorA, new Vector3f(0, 1, 0),
				new float[]{x, y, x + wt, y + ht}));
		return charWidth[pos];
	}

	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) {
		super.onResourceManagerReload(resourceManager);
		String p = locationFontTexture.getResourcePath();
		if (p.startsWith("textures/")) p = p.substring("textures/".length(), p.length());
		if (p.endsWith(".png")) p = p.substring(0, p.length() - ".png".length());
		String f = locationFontTexture.getResourceDomain() + ":" + p;
		sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(f);
	}

	@Override
	protected void doDraw(float shift) {
		posX += (int) shift;
	}

	@Override
	protected void setColor(float r, float g, float b, float a) {
		colorA[0] = r;
		colorA[1] = g;
		colorA[2] = b;
		colorA[3] = 1;
	}

	@Override
	public void enableAlpha() {
	}

	public ImmutableList<RawQuad> build() {
		ImmutableList<RawQuad> ret = builder.build();
		builder = ImmutableList.builder();
		return ret;
	}

	@Override
	protected void bindTexture(ResourceLocation location) {
		//NO-OP
	}
}
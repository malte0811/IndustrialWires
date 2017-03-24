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

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.util.IELogger;
import com.google.common.collect.ImmutableList;
import malte0811.industrialWires.blocks.controlpanel.PanelComponent;
import malte0811.industrialWires.blocks.controlpanel.PropertyComponents;
import malte0811.industrialWires.client.RawQuad;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.List;

public final class PanelUtils {
	private PanelUtils() {}

	public static List<BakedQuad> generateQuads(PropertyComponents.ComponentList components) {
		//TODO different sizes of panels?
		List<BakedQuad> ret = new ArrayList<>();
		final float panelHeight = .5F;
		for (PanelComponent pc:components) {
			List<RawQuad> compQuads = pc.getQuads();
			for (RawQuad bq:compQuads) {
				ret.add(bakeQuad(bq, new Vector3f(pc.getX(), panelHeight, pc.getY())));
			}
		}
		return ret;
	}

	public static BakedQuad bakeQuad(RawQuad raw, Vector3f offset) {
		VertexFormat format = DefaultVertexFormats.ITEM;
		UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
		builder.setQuadOrientation(raw.facing);
		builder.setTexture(raw.tex);
		Vector3f[] vertices = raw.vertices;
		float[] uvs = raw.uvs;
		OBJModel.Normal faceNormal = new OBJModel.Normal(raw.normal.x, raw.normal.y, raw.normal.z);
		putVertexData(format, builder, vertices[0].translate(offset.x, offset.y, offset.z), faceNormal, uvs[0], uvs[1], raw.tex,
				raw.colorA);
		putVertexData(format, builder, vertices[1].translate(offset.x, offset.y, offset.z), faceNormal, uvs[0], uvs[3], raw.tex,
				raw.colorA);
		putVertexData(format, builder, vertices[2].translate(offset.x, offset.y, offset.z), faceNormal, uvs[2], uvs[3], raw.tex,
				raw.colorA);
		putVertexData(format, builder, vertices[3].translate(offset.x, offset.y, offset.z), faceNormal, uvs[2], uvs[1], raw.tex,
				raw.colorA);
		return builder.build();
	}
	//mostly copied from IE's ClientUtils, it has protected access there...
	public static void putVertexData(VertexFormat format, UnpackedBakedQuad.Builder builder, Vector3f pos, OBJModel.Normal faceNormal, double u, double v, TextureAtlasSprite sprite, float[] colorA)
	{
		for(int e = 0; e < format.getElementCount(); e++)
			switch(format.getElement(e).getUsage())
			{
			case POSITION:
				builder.put(e, pos.getX(), pos.getY(), pos.getZ(), 0);
				break;
			case COLOR:
				builder.put(e, colorA[0], colorA[1], colorA[2], colorA[3]);
				break;
			case UV:
				if(sprite == null)//Double Safety. I have no idea how it even happens, but it somehow did .-.
					sprite = Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
				builder.put(e,
						sprite.getInterpolatedU(u),
						sprite.getInterpolatedV((v)),
						0, 1);
				break;
			case NORMAL:
				builder.put(e, faceNormal.x, faceNormal.y, faceNormal.z, 0);
				break;
			default:
				builder.put(e);
			}
	}
	public static void addColoredBox(float[] colorTop, float[] colorSides, float[] colorBottom, Vector3f min, Vector3f size, List<RawQuad> out, boolean doBottom) {
		addQuad(out, new Vector3f(min.x, min.y+size.y, min.z), new Vector3f(min.x, min.y+size.y, min.z+size.z),
				new Vector3f(min.x+size.x, min.y+size.y, min.z+size.z), new Vector3f(min.x+size.x, min.y+size.y, min.z),
				EnumFacing.UP, colorTop);
		if (doBottom) {
			addQuad(out, new Vector3f(min.x, min.y, min.z), new Vector3f(min.x+size.x, min.y, min.z),
					new Vector3f(min.x+size.x, min.y, min.z+size.z), new Vector3f(min.x, min.y, min.z+size.z),
					EnumFacing.UP, colorBottom);
		}
		addQuad(out, new Vector3f(min.x, min.y, min.z), new Vector3f(min.x, min.y, min.z+size.z),
				new Vector3f(min.x, min.y+size.y, min.z+size.z), new Vector3f(min.x, min.y+size.y, min.z),
				EnumFacing.WEST, colorSides);
		addQuad(out, new Vector3f(min.x+size.x, min.y, min.z), new Vector3f(min.x+size.x, min.y+size.y, min.z),
				new Vector3f(min.x+size.x, min.y+size.y, min.z+size.z), new Vector3f(min.x+size.x, min.y, min.z+size.z),
				EnumFacing.EAST, colorSides);
		addQuad(out, new Vector3f(min.x, min.y, min.z), new Vector3f(min.x, min.y+size.y, min.z),
				new Vector3f(min.x+size.x, min.y+size.y, min.z), new Vector3f(min.x+size.x, min.y, min.z),
				EnumFacing.NORTH, colorSides);
		addQuad(out, new Vector3f(min.x, min.y, min.z+size.z), new Vector3f(min.x+size.x, min.y, min.z+size.z),
				new Vector3f(min.x+size.x, min.y+size.y, min.z+size.z), new Vector3f(min.x, min.y+size.y, min.z+size.z),
				EnumFacing.SOUTH, colorSides);
	}
	private static final float[] UV_FULL = {0, 0, 1, 1};
	public static void addQuad(List<RawQuad> out, Vector3f v0, Vector3f v1, Vector3f v2, Vector3f v3, EnumFacing dir, float[] color) {
		Vec3i dirV = dir.getDirectionVec();
		out.add(new RawQuad(v0, v1, v2, v3, dir, Minecraft.getMinecraft().getTextureMapBlocks().getTextureExtry(ModelLoader.White.LOCATION.toString()),
				color, new Vector3f(dirV.getX(), dirV.getY(), dirV.getZ()), UV_FULL));
	}
}

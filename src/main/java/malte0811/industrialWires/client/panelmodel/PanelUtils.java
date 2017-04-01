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

import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import malte0811.industrialWires.blocks.controlpanel.PanelComponent;
import malte0811.industrialWires.blocks.controlpanel.PropertyComponents;
import malte0811.industrialWires.client.RawQuad;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.List;

public final class PanelUtils {
	public static TextureAtlasSprite IRON_BLOCK_TEX;
	private PanelUtils() {}

	public static List<BakedQuad> generateQuads(PropertyComponents.PanelRenderProperties components) {
		if (IRON_BLOCK_TEX==null) {
			IRON_BLOCK_TEX = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/iron_block");
		}
		List<BakedQuad> ret = new ArrayList<>();
		Matrix4 m4 = components.getPanelTopTransform();
		Matrix4 m4RotOnly = m4.copy();
		m4RotOnly.invert();
		m4RotOnly.transpose();
		for (PanelComponent pc:components) {
			Matrix4 m4Here = m4.copy().translate(pc.getX(), 0, pc.getY());
			List<RawQuad> compQuads = pc.getQuads();
			for (RawQuad bq:compQuads) {
				ret.add(bakeQuad(bq, m4Here, m4RotOnly));
			}
		}
		Matrix4 baseTrans = components.getPanelBaseTransform();
		Matrix4 baseNorm = baseTrans.copy();
		baseNorm.invert();
		baseNorm.transpose();

		List<RawQuad> rawOut = new ArrayList<>();
		addTexturedBox(new Vector3f(0, 0, 0), new Vector3f(1, components.height, 1), rawOut, UV_FULL, IRON_BLOCK_TEX);
		for (RawQuad bq:rawOut) {
			ret.add(bakeQuad(bq, baseTrans, baseNorm));
		}

		return ret;
	}

	public static BakedQuad bakeQuad(RawQuad raw, Matrix4 transform, Matrix4 transfNormal) {
		VertexFormat format = DefaultVertexFormats.ITEM;
		UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
		builder.setQuadOrientation(raw.facing);
		builder.setTexture(raw.tex);
		Vector3f[] vertices = raw.vertices;
		float[] uvs = raw.uvs;
		Vector3f normal = transfNormal.apply(raw.normal);
		OBJModel.Normal faceNormal = new OBJModel.Normal(normal.x, normal.y, normal.z);
		putVertexData(format, builder, transform.apply(vertices[0]), faceNormal, uvs[0], uvs[1], raw.tex,
				raw.colorA);
		putVertexData(format, builder, transform.apply(vertices[1]), faceNormal, uvs[0], uvs[3], raw.tex,
				raw.colorA);
		putVertexData(format, builder, transform.apply(vertices[2]), faceNormal, uvs[2], uvs[3], raw.tex,
				raw.colorA);
		putVertexData(format, builder, transform.apply(vertices[3]), faceNormal, uvs[2], uvs[1], raw.tex,
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
	private static final float[] UV_FULL = {0, 0, 16, 16};
	private static final float[] WHITE = {1, 1, 1, 1};
	public static void addTexturedBox(Vector3f min, Vector3f size, List<RawQuad> out, float[] uvs, TextureAtlasSprite tex) {
		addBox(WHITE, WHITE, WHITE, min, size, out, true, uvs, tex);
	}
	public static void addColoredBox(float[] colorTop, float[] colorSides, float[] colorBottom, Vector3f min, Vector3f size, List<RawQuad> out, boolean doBottom) {
		addBox(colorTop, colorSides, colorBottom, min, size, out, doBottom, UV_FULL, ModelLoader.White.INSTANCE);
	}
	public static void addBox(float[] colorTop, float[] colorSides, float[] colorBottom, Vector3f min, Vector3f size, List<RawQuad> out, boolean doBottom, float[] uvs, TextureAtlasSprite tex) {
		addQuad(out, new Vector3f(min.x, min.y+size.y, min.z), new Vector3f(min.x, min.y+size.y, min.z+size.z),
				new Vector3f(min.x+size.x, min.y+size.y, min.z+size.z), new Vector3f(min.x+size.x, min.y+size.y, min.z),
				EnumFacing.UP, colorTop, tex, uvs);
		if (doBottom) {
			addQuad(out, new Vector3f(min.x, min.y, min.z), new Vector3f(min.x+size.x, min.y, min.z),
					new Vector3f(min.x+size.x, min.y, min.z+size.z), new Vector3f(min.x, min.y, min.z+size.z),
					EnumFacing.UP, colorBottom, tex, uvs);
		}
		addQuad(out, new Vector3f(min.x, min.y, min.z), new Vector3f(min.x, min.y, min.z+size.z),
				new Vector3f(min.x, min.y+size.y, min.z+size.z), new Vector3f(min.x, min.y+size.y, min.z),
				EnumFacing.WEST, colorSides, tex, uvs);
		addQuad(out, new Vector3f(min.x+size.x, min.y, min.z), new Vector3f(min.x+size.x, min.y+size.y, min.z),
				new Vector3f(min.x+size.x, min.y+size.y, min.z+size.z), new Vector3f(min.x+size.x, min.y, min.z+size.z),
				EnumFacing.EAST, colorSides, tex, uvs);
		addQuad(out, new Vector3f(min.x, min.y, min.z), new Vector3f(min.x, min.y+size.y, min.z),
				new Vector3f(min.x+size.x, min.y+size.y, min.z), new Vector3f(min.x+size.x, min.y, min.z),
				EnumFacing.NORTH, colorSides, tex, uvs);
		addQuad(out, new Vector3f(min.x, min.y, min.z+size.z), new Vector3f(min.x+size.x, min.y, min.z+size.z),
				new Vector3f(min.x+size.x, min.y+size.y, min.z+size.z), new Vector3f(min.x, min.y+size.y, min.z+size.z),
				EnumFacing.SOUTH, colorSides, tex, uvs);
	}
	public static void addColoredQuad(List<RawQuad> out, Vector3f v0, Vector3f v1, Vector3f v2, Vector3f v3, EnumFacing dir, float[] color) {
		addQuad(out, v0, v1, v2, v3, dir, color, Minecraft.getMinecraft().getTextureMapBlocks().getTextureExtry(ModelLoader.White.LOCATION.toString()), UV_FULL);
	}

	public static void addQuad(List<RawQuad> out, Vector3f v0, Vector3f v1, Vector3f v2, Vector3f v3, EnumFacing dir, float[] color, TextureAtlasSprite tex, float[] uvs) {
		Vec3i dirV = dir.getDirectionVec();
		out.add(new RawQuad(v0, v1, v2, v3, dir, tex,
				color, new Vector3f(dirV.getX(), dirV.getY(), dirV.getZ()), uvs));
	}
}

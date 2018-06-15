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

package malte0811.industrialWires.client.render;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import malte0811.industrialWires.blocks.converter.TileEntityMechMB;
import malte0811.industrialWires.client.ClientUtilsIW;
import malte0811.industrialWires.client.RawQuad;
import malte0811.industrialWires.mech_mb.MechMBPart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;
import java.util.*;

import static malte0811.industrialWires.blocks.converter.TileEntityMechMB.TICK_ANGLE_PER_SPEED;
import static malte0811.industrialWires.mech_mb.MechMBPart.SHAFT_KEY;

public class TileRenderMechMB extends TileEntitySpecialRenderer<TileEntityMechMB> implements IResourceManagerReloadListener {
	public static final Map<ResourceLocation, IBakedModel> BASE_MODELS = new HashMap<>();
	private static final Set<TileEntityMechMB> TES_WITH_MODELS = Collections.newSetFromMap(new WeakHashMap<>());
	static {
		IEApi.renderCacheClearers.add(TileRenderMechMB::clearCache);
	}
	@Override
	public void render(TileEntityMechMB te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		if (te.mechanical != null) {
			if (te.rotatingModel == null) {
				generateModel(te);
			}
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(770, 771);
			RenderHelper.disableStandardItemLighting();
			Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			GlStateManager.pushMatrix();
			GlStateManager.translate(.5F + x, .5F + y, .5F + z);
			GlStateManager.rotate(180 - te.facing.getHorizontalAngle(), 0, 1, 0);
			GlStateManager.rotate((float) (te.angle + te.energyState.getSpeed() * TICK_ANGLE_PER_SPEED * partialTicks),
					0, 0, 1);
			GlStateManager.translate(-.5, -.5, -.5);
			Tessellator tes = Tessellator.getInstance();
			BufferBuilder bb = tes.getBuffer();
			bb.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
			ClientUtils.renderModelTESRFast(te.rotatingModel, bb, te.getWorld(), te.getPos());
			tes.draw();
			GlStateManager.popMatrix();
			RenderHelper.enableStandardItemLighting();
		}
	}

	@Override
	public void onResourceManagerReload(@Nonnull IResourceManager resourceManager) {
		clearCache();
	}

	private static void clearCache() {
		for (TileEntityMechMB te : TES_WITH_MODELS)
			te.rotatingModel = null;
		TES_WITH_MODELS.clear();
	}

	private void generateModel(TileEntityMechMB te) {
		te.rotatingModel = new ArrayList<>();
		int offset = 1;
		for (MechMBPart part : te.mechanical) {
			addQuadsForPart(part, offset, te.rotatingModel);
			offset += part.getLength();
		}
		//Add shaft model in the end blocks
		List<BakedQuad> shaftQuads = MechMBPart.INSTANCES.get(SHAFT_KEY).getRotatingQuads();
		Vector3f tmp = new Vector3f();
		Matrix4 id = new Matrix4();
		Matrix4 translate = new Matrix4();
		translate.translate(0, 0, offset);
		for (BakedQuad q : shaftQuads) {
			RawQuad raw = RawQuad.unbake(q);
			Vector3f.add(raw.vertices[0], raw.vertices[1], tmp);
			tmp.scale(.5F);
			Vector3f middle01 = new Vector3f(tmp);
			Vector3f.add(raw.vertices[2], raw.vertices[3], tmp);
			tmp.scale(.5F);
			Vector3f middle23 = new Vector3f(tmp);
			RawQuad start = new RawQuad(raw.vertices[0], middle01, middle23, raw.vertices[3],
					raw.facing, raw.tex, raw.colorA, raw.normal, new float[][]{
					raw.uvs[0], {raw.uvs[1][0], 8}, {raw.uvs[2][0], 8}, raw.uvs[3]
			}, -1);
			te.rotatingModel.add(ClientUtilsIW.bakeQuad(start, id, id));
			RawQuad end = new RawQuad(middle01, raw.vertices[1], raw.vertices[2], middle23,
					raw.facing, raw.tex, raw.colorA, raw.normal, new float[][]{
					{raw.uvs[0][0], 8}, raw.uvs[1], raw.uvs[2], {raw.uvs[3][0], 8}
			}, -1);
			te.rotatingModel.add(ClientUtilsIW.bakeQuad(end, translate, id));
		}
		TES_WITH_MODELS.add(te);
	}

	private void addQuadsForPart(MechMBPart part, int offset, List<BakedQuad> out) {
		List<BakedQuad> quadsForPart = part.getRotatingQuads();
		if (offset != 0) {
			for (BakedQuad b : quadsForPart) {
				out.add(translateQuadZ(b, offset));
			}
		} else {
			out.addAll(quadsForPart);
		}
	}

	private BakedQuad translateQuadZ(BakedQuad b, float offset) {
		int[] data = Arrays.copyOf(b.getVertexData(), b.getVertexData().length);
		int pos = 0;
		for (VertexFormatElement ele : b.getFormat().getElements()) {
			if (ele.getUsage() == VertexFormatElement.EnumUsage.POSITION) {
				for (int i = 0; i < 4; i++) {
					data[i * b.getFormat().getIntegerSize() + pos + 2] = Float.floatToRawIntBits(
							Float.intBitsToFloat(data[i * b.getFormat().getIntegerSize() + pos + 2]) + offset);
				}
				break;
			}
			pos += ele.getSize() / 4;
		}
		return new BakedQuad(data, b.getTintIndex(), b.getFace(),
				b.getSprite(), b.shouldApplyDiffuseLighting(), b.getFormat());
	}
}

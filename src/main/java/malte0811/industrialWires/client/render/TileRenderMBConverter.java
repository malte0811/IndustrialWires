/*
 * This file is part of Industrial Wires.
 * Copyright (C) 2016-2017 malte0811
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

import blusunrize.immersiveengineering.client.ClientUtils;
import malte0811.industrialWires.blocks.converter.TileEntityMultiblockConverter;
import malte0811.industrialWires.converter.MechMBPart;
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

import java.util.*;

import static malte0811.industrialWires.blocks.converter.TileEntityMultiblockConverter.TICK_ANGLE_PER_SPEED;

public class TileRenderMBConverter extends TileEntitySpecialRenderer<TileEntityMultiblockConverter> implements IResourceManagerReloadListener {
	public static final Map<ResourceLocation, IBakedModel> BASE_MODELS = new HashMap<>();
	public static final Set<TileEntityMultiblockConverter> TES_WITH_MODELS = Collections.newSetFromMap(new WeakHashMap<>());
	@Override
	public void render(TileEntityMultiblockConverter te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		if (te.mechanical!=null) {
			if (te.rotatingModel == null)
			{
				te.rotatingModel = new ArrayList<>();
				int offset = 0;
				for (MechMBPart part : te.mechanical) {
					List<BakedQuad> quadsForPart = part.getRotatingQuads();
					if (offset != 0) {
						for (BakedQuad b : quadsForPart) {
							int[] data = Arrays.copyOf(b.getVertexData(), b.getVertexData().length);
							int pos = 0;
							for (VertexFormatElement ele : b.getFormat().getElements()) {
								if (ele.getUsage() == VertexFormatElement.EnumUsage.POSITION) {
									for (int i = 0;i<4;i++) {
										data[i*b.getFormat().getIntegerSize()+pos + 2] = Float.floatToRawIntBits(
												Float.intBitsToFloat(data[i*b.getFormat().getIntegerSize()+pos + 2]) + offset);
									}
									break;
								}
								pos += ele.getSize()/4;
							}
							BakedQuad translated = new BakedQuad(data, b.getTintIndex(), b.getFace(),
									b.getSprite(), b.shouldApplyDiffuseLighting(), b.getFormat());
							te.rotatingModel.add(translated);
						}
					} else {
						te.rotatingModel.addAll(quadsForPart);
					}
					offset += part.getLength();
				}
				TES_WITH_MODELS.add(te);
			}
			GlStateManager.enableBlend();
			GlStateManager.disableCull();
			GlStateManager.blendFunc(770, 771);
			RenderHelper.disableStandardItemLighting();
			Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			GlStateManager.pushMatrix();
			GlStateManager.translate(.5F + x, .5F + y, .5F + z);
			GlStateManager.rotate(180-te.facing.getHorizontalAngle(), 0, 1, 0);
			GlStateManager.rotate((float) (te.angle + te.energyState.getSpeed() * TICK_ANGLE_PER_SPEED * partialTicks),
					0, 0, 1);
			GlStateManager.translate(-.5, -.5, .5);
			Tessellator tes = Tessellator.getInstance();
			BufferBuilder bb = tes.getBuffer();
			bb.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
			//TODO fix that and probably remove the AT entry
			ClientUtils.renderModelTESRFast(te.rotatingModel, bb, te.getWorld(), te.getPos());
			tes.draw();
			GlStateManager.popMatrix();
			RenderHelper.enableStandardItemLighting();
		}
	}

	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) {
		for (TileEntityMultiblockConverter te:TES_WITH_MODELS)
			te.rotatingModel = null;
		TES_WITH_MODELS.clear();
	}
}

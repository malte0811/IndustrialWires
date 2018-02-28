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

import blusunrize.immersiveengineering.client.ClientUtils;
import malte0811.industrialWires.entities.EntityBrokenPart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityRenderBrokenPart extends Render<EntityBrokenPart> {
	public EntityRenderBrokenPart(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(@Nonnull EntityBrokenPart entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
		AxisAlignedBB aabb = entity.getEntityBoundingBox();
		ClientUtils.bindAtlas();
		Tessellator tes = Tessellator.getInstance();
		BufferBuilder bb = tes.getBuffer();
		bb.setTranslation(x - entity.lastTickPosX, y - entity.lastTickPosY, z - entity.lastTickPosZ);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		bb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		TextureAtlasSprite tex = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(entity.texture.toString());
		ClientUtils.renderTexturedBox(bb, aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ,
				tex.getMinU(), tex.getMinV(), tex.getInterpolatedU(8), tex.getInterpolatedV(8));
		tes.draw();
		bb.setTranslation(0, 0, 0);
		GlStateManager.popMatrix();
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}

	@Nullable
	@Override
	protected ResourceLocation getEntityTexture(@Nonnull EntityBrokenPart entity) {
		return entity.texture;
	}
}

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

package malte0811.industrialWires.client.render;

import malte0811.industrialWires.blocks.TileEntityJacobsLadder;
import malte0811.industrialWires.util.Beziers;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.ModelLoader;
import org.lwjgl.opengl.GL11;

public class TileRenderJacobsLadder extends TileEntitySpecialRenderer<TileEntityJacobsLadder> {
	@Override
	public void renderTileEntityAt(TileEntityJacobsLadder tile, double x, double y, double z, float partialTicks, int destroyStage) {
		super.renderTileEntityAt(tile, x, y, z, partialTicks, destroyStage);
		if (!tile.isDummy()&&tile.timeTillActive==0&&tile.controls[0] != null) {
			//TODO move to size
			final int steps = 10;

			GlStateManager.pushMatrix();
			GlStateManager.translate(x + .5 - tile.size.bottomDistance / 2, y + tile.size.heightOffset, z + .5);

			GlStateManager.disableTexture2D();
			GlStateManager.disableLighting();

			float oldBX = OpenGlHelper.lastBrightnessX;
			float oldBY = OpenGlHelper.lastBrightnessY;
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 238, 238);
			GlStateManager.color(1, .85F, 1, 1);
			Vec3d[] controls = new Vec3d[tile.size.arcPoints];
			for (int i = 0; i < tile.size.arcPoints; i++) {
				Vec3d speed = tile.controlMovement[i];
				controls[i] = tile.controls[i].addVector(speed.xCoord * partialTicks, speed.yCoord * partialTicks, speed.zCoord * partialTicks);
			}
			drawBezier(controls, tile.size.renderDiameter, steps);
			/*for (Vec3d[] c:tile.controlControls) {
				drawBezier(c, .05, steps);
			}*/

			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, oldBX, oldBY);

			GlStateManager.enableTexture2D();
			GlStateManager.enableLighting();

			GlStateManager.popMatrix();
		}
	}

	private void drawBezier(Vec3d[] controls, double diameter, int steps) {
		Vec3d radY = new Vec3d(0, diameter / 2, 0);
		Vec3d radZ = new Vec3d(0, 0, diameter / 2);
		Tessellator tes = Tessellator.getInstance();
		VertexBuffer vertBuffer = tes.getBuffer();

		vertBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
		Vec3d last = Beziers.getPoint(0, controls);
		for (double d = 1D / steps; d <= 1; d += 1D / steps) {
			Vec3d pos = Beziers.getPoint(d, controls);
			drawQuad(last, pos, radY, vertBuffer);
			drawQuad(last, pos, radZ, vertBuffer);
			last = pos;
		}

		tes.draw();
	}

	private void drawQuad(Vec3d v0, Vec3d v1, Vec3d rad, VertexBuffer vertexBuffer) {
		float r = 1, g = 1F, b = 1, a = 1;
		TextureAtlasSprite tex = ModelLoader.White.INSTANCE;
		vertexBuffer.pos(v1.xCoord - rad.xCoord, v1.yCoord - rad.yCoord, v1.zCoord - rad.zCoord).endVertex();
		vertexBuffer.pos(v0.xCoord - rad.xCoord, v0.yCoord - rad.yCoord, v0.zCoord - rad.zCoord).endVertex();
		vertexBuffer.pos(v0.xCoord + rad.xCoord, v0.yCoord + rad.yCoord, v0.zCoord + rad.zCoord).endVertex();
		vertexBuffer.pos(v1.xCoord + rad.xCoord, v1.yCoord + rad.yCoord, v1.zCoord + rad.zCoord).endVertex();

		vertexBuffer.pos(v1.xCoord + rad.xCoord, v1.yCoord + rad.yCoord, v1.zCoord + rad.zCoord).endVertex();
		vertexBuffer.pos(v0.xCoord + rad.xCoord, v0.yCoord + rad.yCoord, v0.zCoord + rad.zCoord).endVertex();
		vertexBuffer.pos(v0.xCoord - rad.xCoord, v0.yCoord - rad.yCoord, v0.zCoord - rad.zCoord).endVertex();
		vertexBuffer.pos(v1.xCoord - rad.xCoord, v1.yCoord - rad.yCoord, v1.zCoord - rad.zCoord).endVertex();
	}
}

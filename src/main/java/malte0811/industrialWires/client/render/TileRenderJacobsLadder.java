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

import malte0811.industrialWires.blocks.hv.TileEntityJacobsLadder;
import malte0811.industrialWires.blocks.hv.TileEntityJacobsLadder.LadderSize;
import malte0811.industrialWires.util.Beziers;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import static malte0811.industrialWires.blocks.hv.TileEntityJacobsLadder.getColor;

public class TileRenderJacobsLadder extends TileEntitySpecialRenderer<TileEntityJacobsLadder> {
	@Override
	public void render(TileEntityJacobsLadder tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		super.render(tile, x, y, z, partialTicks, destroyStage, alpha);
		if (!tile.isDummy() && tile.timeTillActive == 0 && tile.controls[0] != null) {

			GlStateManager.pushMatrix();
			GlStateManager.translate(x + .5, y + tile.size.heightOffset, z + .5);
			GlStateManager.rotate(tile.facing.getHorizontalAngle(), 0, 1, 0);
			GlStateManager.translate(-tile.size.bottomDistance / 2, 0, 0);
			GlStateManager.disableTexture2D();
			GlStateManager.disableLighting();
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
			if (Shaders.areShadersEnabled()) {
				GlStateManager.enableBlend();
				GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			}

			setLightmapDisabled(true);
			GlStateManager.color(1, .85F, 1, 1);
			Vec3d[] controls = new Vec3d[tile.size.arcPoints];
			for (int i = 0; i < tile.size.arcPoints; i++) {
				Vec3d speed = tile.controlMovement[i].scale(partialTicks);
				controls[i] = tile.controls[i].add(speed);
			}
			drawBezier(controls, tile.salt, tile.size);
			//DEBUG CODE
			/*for (Vec3d[] c:tile.controlControls) {
				drawBezier(c, .05, steps);
			}
			Vec3d topA = tile.controlMovement[0].scale(tile.size.tickToTop);
			Vec3d topB = tile.controlMovement[tile.controlMovement.length-1].scale(tile.size.tickToTop).add(new Vec3d(tile.size.bottomDistance, 0, 0));

			Tessellator tes = Tessellator.getInstance();
			VertexBuffer vertBuffer = tes.getBuffer();
			vertBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
			drawQuad(Vec3d.ZERO, topA, new Vec3d(.005, 0, 0), vertBuffer);
			drawQuad(new Vec3d(tile.size.bottomDistance, 0, 0), topB, new Vec3d(.005, 0, 0), vertBuffer);
			tes.draw();*/
			//END OF DEBUG CODE

			setLightmapDisabled(false);

			GlStateManager.enableTexture2D();
			GlStateManager.enableLighting();
			GlStateManager.shadeModel(GL11.GL_FLAT);
			GlStateManager.disableBlend();

			GlStateManager.popMatrix();
		}
	}

	private void drawBezier(Vec3d[] controls, double salt, LadderSize size) {
		Shaders.useShader(Shaders.JACOBS_ARC);
		int steps = size.renderPoints;
		double diameter = size.renderDiameter;
		Vec3d radY = new Vec3d(0, diameter / 2, 0);
		Vec3d radZ = new Vec3d(0, 0, diameter / 2);
		Tessellator tes = Tessellator.getInstance();
		BufferBuilder vertBuffer = tes.getBuffer();
		float[][] colors = new float[steps + 1][];
		vertBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		Vec3d last = Beziers.getPoint(0, controls);
		colors[0] = getColor(0, salt, size);
		if (Shaders.areShadersEnabled()) {
			colors[0][0] = 0;
		}
		for (int i = 1; i <= steps; i++) {
			double d = i / (double) steps;
			colors[i] = getColor(d, salt, size);
			Vec3d pos = Beziers.getPoint(d, controls);
			if (Shaders.areShadersEnabled()) {
				colors[i][0] = (float) d;
			}
			drawQuad(last, pos, radY, colors[i - 1], colors[i], vertBuffer, false);
			drawQuad(last, pos, radZ, colors[i - 1], colors[i], vertBuffer, false);
			last = pos;
		}
		tes.draw();
		Shaders.stopUsingShaders();
	}

	private void drawQuad(Vec3d v0, Vec3d v1, Vec3d rad, float[] color0, float[] color1, BufferBuilder vertexBuffer,
						  boolean horizontal) {
		float alpha0 = horizontal?.5F:0;
		float alpha1 = horizontal?.5F:1;
		color(color1, alpha0, vertexBuffer.pos(v1.x - rad.x, v1.y - rad.y, v1.z - rad.z)).endVertex();
		color(color0, alpha0, vertexBuffer.pos(v0.x - rad.x, v0.y - rad.y, v0.z - rad.z)).endVertex();
		color(color0, alpha1, vertexBuffer.pos(v0.x + rad.x, v0.y + rad.y, v0.z + rad.z)).endVertex();
		color(color1, alpha1, vertexBuffer.pos(v1.x + rad.x, v1.y + rad.y, v1.z + rad.z)).endVertex();

		color(color1, alpha1, vertexBuffer.pos(v1.x + rad.x, v1.y + rad.y, v1.z + rad.z)).endVertex();
		color(color0, alpha1, vertexBuffer.pos(v0.x + rad.x, v0.y + rad.y, v0.z + rad.z)).endVertex();
		color(color0, alpha0, vertexBuffer.pos(v0.x - rad.x, v0.y - rad.y, v0.z - rad.z)).endVertex();
		color(color1, alpha0, vertexBuffer.pos(v1.x - rad.x, v1.y - rad.y, v1.z - rad.z)).endVertex();
	}

	private BufferBuilder color(float[] color, float alpha, BufferBuilder vb) {
		vb.color(color[0], color[1], color[2], alpha);
		return vb;
	}
}

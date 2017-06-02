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

import malte0811.industrialWires.blocks.IWProperties;
import malte0811.industrialWires.blocks.hv.TileEntityMarx;
import malte0811.industrialWires.util.MiscUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

public class TileRenderMarx extends TileEntitySpecialRenderer<TileEntityMarx> {
	@Override
	public void renderTileEntityAt(TileEntityMarx te, double x, double y, double z, float partialTicks, int destroyStage) {
		if (te.type== IWProperties.MarxType.BOTTOM&&te.state== TileEntityMarx.FiringState.FIRE) {
			prepare(x, y, z, te);
			Tessellator tes = Tessellator.getInstance();
			VertexBuffer vb = tes.getBuffer();

			drawDischarge(te.dischargeData, vb, tes);
			GlStateManager.popMatrix();
			GlStateManager.pushMatrix();
			Vec3i offset = MiscUtils.offset(BlockPos.ORIGIN, te.facing, te.mirrored, 1, 1, 0);
			GlStateManager.translate(x+offset.getX(), y+offset.getY()+.75, z+offset.getZ());
			Vec3i facing = te.facing.getDirectionVec();
			final float pos = .6875F;
			GlStateManager.translate(-facing.getX()*pos, 0, -facing.getZ()*pos);
			//draw firing spark gaps
			for (int i = 0;i<te.stageCount-1;i++) {
				GlStateManager.pushMatrix();
				GlStateManager.translate(0, i, 0);
				GlStateManager.rotate(-45, facing.getX(), facing.getY(), facing.getZ());
				GlStateManager.rotate(-Minecraft.getMinecraft().player.rotationYaw+180, 0, 1, 0);
				vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
				drawDischargeSection(new Vector3f(0, -.2F, 0), new Vector3f(0, .2F, 0), .25F, vb);
				tes.draw();
				GlStateManager.popMatrix();
			}

			cleanUp();
		}
	}
	private void prepare(double x, double y, double z, TileEntityMarx te) {
		setLightmapDisabled(true);
		GlStateManager.pushMatrix();
		Vec3i offset = MiscUtils.offset(BlockPos.ORIGIN, te.facing, te.mirrored, 1, 4, 1);
		GlStateManager.translate(x+offset.getX()+.5, y+offset.getY(), z+offset.getZ()+.5);
		GlStateManager.disableTexture2D();
		GlStateManager.disableLighting();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.rotate(-Minecraft.getMinecraft().player.rotationYaw+180, 0, 1, 0);
	}
	private void cleanUp() {
		setLightmapDisabled(false);
		GlStateManager.popMatrix();
		GlStateManager.enableTexture2D();
		GlStateManager.enableLighting();
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.disableBlend();
	}
	private static final float[] WHITE = {1, 1, 1, 1};
	private static final float[] WHITE_TRANSPARENT = {1, 1, 1, 0};
	private void drawDischarge(Discharge d, VertexBuffer vb, Tessellator tes) {
		if (d!=null&&d.vertices!=null) {
			vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
			for (int i = 0;i<d.vertices.length-1;i++) {
				drawDischargeSection(d.vertices[i], d.vertices[i+1], d.diameter, vb);
			}
			tes.draw();
		}
	}
	private void drawDischargeSection(Vector3f start, Vector3f end, float diameter, VertexBuffer vb) {
		drawPart(start, end, diameter/3, diameter/3, WHITE_TRANSPARENT, WHITE, vb);
		drawPart(start, end, 0, diameter/3, WHITE, WHITE, vb);
		drawPart(start, end, -diameter/3, diameter/3, WHITE, WHITE_TRANSPARENT, vb);
	}
	private void drawPart(Vector3f start, Vector3f end, float offset, float width, float[] color1, float[] color2, VertexBuffer vb) {
		vb.setTranslation(-offset-width/2, 0, 0);
		vb.pos(start.x, start.y, start.z).color(color1[0], color1[1], color1[2], color1[3]).endVertex();
		vb.pos(start.x+width, start.y, start.z).color(color2[0], color2[1], color2[2], color2[3]).endVertex();
		vb.pos(end.x+width, end.y, end.z).color(color2[0], color2[1], color2[2], color2[3]).endVertex();
		vb.pos(end.x, end.y, end.z).color(color1[0], color1[1], color1[2], color1[3]).endVertex();
		vb.setTranslation(0, 0, 0);
	}
	public static final class Discharge {
		public float energy;
		public Vector3f[] vertices;
		public float diameter = .25F;
	}
}

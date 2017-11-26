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

import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import malte0811.industrialWires.blocks.IWProperties;
import malte0811.industrialWires.blocks.hv.TileEntityMarx;
import malte0811.industrialWires.blocks.hv.TileEntityMarx.Discharge;
import malte0811.industrialWires.client.ClientEventHandler;
import malte0811.industrialWires.util.MiscUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.lwjgl.opengl.GL11;

import static malte0811.industrialWires.blocks.hv.TileEntityMarx.FiringState.FIRE;

public class TileRenderMarx extends TileEntitySpecialRenderer<TileEntityMarx> {
	public static boolean screenShot = false;
	@Override
	public void render(TileEntityMarx te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		final boolean debug = false;
		//noinspection ConstantConditions,PointlessBooleanExpression
		if (te.type == IWProperties.MarxType.BOTTOM && (debug || te.state == FIRE) && te.dischargeData!=null) {
			Vec3d player = Minecraft.getMinecraft().player.getPositionEyes(partialTicks);
			Tessellator tes = Tessellator.getInstance();
			BufferBuilder vb = tes.getBuffer();
			Matrix4 mat = prepare(x, y, z, te, player);
			if (te.dischargeData.energy>0) {
				drawDischarge(te.dischargeData, vb, tes, mat);
			}
			GlStateManager.popMatrix();
			//draw firing spark gaps
			Vec3i facing = te.facing.getDirectionVec();
			Vec3d offset = new Vec3d(MiscUtils.offset(BlockPos.ORIGIN, te.facing, te.mirrored, 1, 1, 0));
			offset = offset.addVector(-.5*oneSgn(offset.x), 0, -.5*oneSgn(offset.z));
			final float pos = .3125F;
			Vec3d gapDir = new Vec3d(facing.getZ()*(te.mirrored?-1:1), 1, facing.getX()*(te.mirrored?1:-1));
			Vec3d up = new Vec3d(gapDir.x, -1, gapDir.z);
			Vec3d bottomGap = new Vec3d(offset.x+facing.getX()*pos+.5, offset.y+.75, offset.z+facing.getZ() * pos+.5);
			GlStateManager.pushMatrix();
			GlStateManager.translate(x + bottomGap.x, y + bottomGap.y, z + bottomGap.z);
			bottomGap = bottomGap.addVector(te.getPos().getX(), te.getPos().getY(), te.getPos().getZ());
			for (int i = 0; i < te.getStageCount() - 1; i++) {
				renderGap(i, facing, vb, tes, player, gapDir, up, bottomGap, te.mirrored);
			}
			cleanUp();
			te.state = TileEntityMarx.FiringState.CHARGING;
			if (screenShot) {
				ClientEventHandler.shouldScreenshot = true;
				screenShot = false;
			}
		}
	}

	private double oneSgn(double in) {
		double ret = Math.signum(in);
		return ret==0?1:ret;
	}

	private void renderGap(int i, Vec3i facing, BufferBuilder vb, Tessellator tes, Vec3d player, Vec3d gapDir, Vec3d up,
						   Vec3d bottomGap, boolean mirrored) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, i, 0);
		GlStateManager.rotate((mirrored?45:135), facing.getX(), facing.getY(), facing.getZ());
		player = player.subtract(bottomGap.x, bottomGap.y+i, bottomGap.z);
		double t = player.dotProduct(gapDir)/2;
		Vec3d lineToPlayer = gapDir.scale(t).subtract(player);
		double angleRad = Math.acos(up.dotProduct(lineToPlayer)/(up.lengthVector()*lineToPlayer.lengthVector()));
		angleRad *= Math.signum(lineToPlayer.dotProduct(new Vec3d(facing)));
		float angle = (float) (Math.toDegrees(angleRad));
		if (facing.getZ()<0) {
			angle = 270+angle;
		} else if (facing.getZ()>0) {
			angle = 90+angle;
		} else if (facing.getX()>0) {
			angle = 180+angle;
		}
		GlStateManager.rotate(angle, 0, 1, 0);
		vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		drawDischargeSection(new Vec3d(0, -.2F, 0), new Vec3d(0, .2F, 0), .25F,
				.4F, .45F, vb);
		tes.draw();
		GlStateManager.popMatrix();
	}

	private Matrix4 prepare(double x, double y, double z, TileEntityMarx te, Vec3d player) {
		setLightmapDisabled(true);
		GlStateManager.pushMatrix();
		Vec3i offset = MiscUtils.offset(BlockPos.ORIGIN, te.facing, te.mirrored, 1, 4, 1);
		Vec3d bottom = new Vec3d(offset.getX()+.5, offset.getY(), offset.getZ()+.5);
		GlStateManager.translate(x+bottom.x, y+bottom.y, z+bottom.z);
		GlStateManager.disableTexture2D();
		GlStateManager.disableLighting();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		player = player.subtract(bottom.add(new Vec3d(te.getPos())));
		double angle = Math.atan2(player.x, player.z);
		Matrix4 ret = new Matrix4();
		ret.rotate(-angle, 0, 1, 0);
		GlStateManager.rotate((float) Math.toDegrees(angle), 0, 1, 0);
		Shaders.useShader(Shaders.MARX);
		return ret;
	}
	private void cleanUp() {
		setLightmapDisabled(false);
		GlStateManager.popMatrix();
		GlStateManager.enableTexture2D();
		GlStateManager.enableLighting();
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.disableBlend();
		Shaders.stopUsingShaders();
	}
	private void drawDischarge(Discharge d, BufferBuilder vb, Tessellator tes, Matrix4 mat) {
		if (d!=null&&d.vertices!=null) {
			vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
			for (int i = 0;i<d.vertices.length-1;i++) {
				drawDischargeSection(mat.apply(d.vertices[i]), mat.apply(d.vertices[i+1]), d.diameter,
						i/(float)(d.vertices.length-1), (i+1)/(float)(d.vertices.length-1), vb);
			}
			tes.draw();
		}
	}
	private void drawDischargeSection(Vec3d start, Vec3d end, float diameter, float aMin, float aMax, BufferBuilder vb) {
		if (Shaders.areShadersEnabled()) {
			drawPart(0, diameter, start, end, aMin, aMax, vb);
		} else {
			drawPart(-diameter/3, diameter/3, start, end, 0, 1, vb);
			drawPart(0, diameter/3, start, end, 1, 1, vb);
			drawPart(diameter/3, diameter/3, start, end, 1, 0, vb);
		}
	}
	private void drawPart(double offset, double diameter, Vec3d start, Vec3d end, float aMin, float aMax, BufferBuilder vb) {
		boolean shaders = Shaders.areShadersEnabled();
		float red1 = shaders?0:1;
		vb.setTranslation(offset-diameter/2, 0, 0);
		vb.pos(start.x, start.y, start.z).color(red1, 1, 1, aMin).endVertex();
		vb.pos(start.x+diameter, start.y, start.z).color(1, 1, 1, shaders?aMin:aMax).endVertex();
		vb.pos(end.x+diameter, end.y, end.z).color(1, 1, 1, aMax).endVertex();
		vb.pos(end.x, end.y, end.z).color(red1, 1, 1, shaders?aMax:aMin).endVertex();
		vb.setTranslation(0, 0, 0);
	}
}

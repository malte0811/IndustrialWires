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

package malte0811.industrialWires.blocks.controlpanel;

import blusunrize.immersiveengineering.common.util.IELogger;
import malte0811.industrialWires.client.RawQuad;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public abstract class PanelComponent {
	protected float panelHeight;
	protected float x, y;
	private final String type;
	protected PanelComponent(String type) {
		this.type = type;
	}
	private static final Map<String, Supplier<PanelComponent>> baseCreaters = new HashMap<>();
	static {
		baseCreaters.put("lightedButton", LightedButton::new);
		baseCreaters.put("label", Label::new);
	}
	protected abstract void writeCustomNBT(NBTTagCompound nbt);
	protected abstract void readCustomNBT(NBTTagCompound nbt);
	// DON'T OFFSET BY x, y IN THIS METHOD!
	public abstract List<RawQuad> getQuads();
	@Nonnull
	public abstract PanelComponent copyOf();

	//well, only relative in the x/z directions
	public abstract AxisAlignedBB getBlockRelativeAABB();

	public abstract boolean interactWith(Vec3d hitRelative, TileEntityPanel tile);

	public abstract void update(TileEntityPanel tile);

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public void setX(float x) {
		this.x = x;
	}

	public void setY(float y) {
		this.y = y;
	}

	public void setPanelHeight(float panelHeight) {
		this.panelHeight = panelHeight;
	}

	public void writeToNBT(NBTTagCompound nbt) {
		writeCustomNBT(nbt);
		nbt.setFloat("x", getX());
		nbt.setFloat("y", getY());
		nbt.setFloat("panelHeight", panelHeight);
		nbt.setString("type", type);
	}
	public static PanelComponent read(NBTTagCompound nbt) {
		String type = nbt.getString("type");
		if (baseCreaters.containsKey(type)) {
			PanelComponent ret = baseCreaters.get(type).get();
			ret.readCustomNBT(nbt);
			ret.setX(nbt.getFloat("x"));
			ret.setY(nbt.getFloat("y"));
			ret.setPanelHeight(nbt.getFloat("panelHeight"));
			return ret;
		} else {
			IELogger.info("(IndustrialWires) Unknown panel component: "+type);
			return null;
		}
	}
	public void renderBox(TileEntityPanel te) {
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.glLineWidth(2.0F);
		GlStateManager.disableTexture2D();
		GlStateManager.depthMask(false);
		double px = te.getPos().getX()-TileEntityRendererDispatcher.staticPlayerX;
		double py = te.getPos().getY()-TileEntityRendererDispatcher.staticPlayerY;
		double pz = te.getPos().getZ()-TileEntityRendererDispatcher.staticPlayerZ;
		RenderGlobal.func_189697_a(te.apply(te.components.getPanelTopTransform(), getBlockRelativeAABB()).expandXyz(0.002).offset(px, py, pz), 0.0F, 0.0F, 0.0F, 0.4F);
		GlStateManager.depthMask(true);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		PanelComponent that = (PanelComponent) o;

		if (Float.compare(that.panelHeight, panelHeight) != 0) return false;
		if (Float.compare(that.x, x) != 0) return false;
		if (Float.compare(that.y, y) != 0) return false;
		return type.equals(that.type);
	}

	@Override
	public int hashCode() {
		int result = (panelHeight != +0.0f ? Float.floatToIntBits(panelHeight) : 0);
		result = 31 * result + (x != +0.0f ? Float.floatToIntBits(x) : 0);
		result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
		result = 31 * result + type.hashCode();
		return result;
	}
}

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

package malte0811.industrialWires.controlpanel;

import blusunrize.immersiveengineering.common.util.IELogger;
import malte0811.industrialWires.blocks.controlpanel.TileEntityPanel;
import malte0811.industrialWires.client.RawQuad;
import malte0811.industrialWires.client.gui.GuiPanelCreator;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class PanelComponent {
	protected float panelHeight;
	protected AxisAlignedBB aabb = null;
	protected float x, y;
	private final String type;
	protected final static float[] GRAY = {.8F, .8F, .8F};
	protected final static int GRAY_INT = 0xFFD0D0D0;
	protected PanelComponent(String type) {
		this.type = type;
	}
	public static final Map<String, Supplier<PanelComponent>> baseCreaters = new HashMap<>();
	public final static String COLOR = "color";
	public final static String RS_CHANNEL = "rsChannel";
	public final static String RS_ID = "rsId";
	public final static String TEXT = "text";
	public static final String HORIZONTAL = "horizontal";
	public static final String LENGTH = "length";
	public static final String LATCHING = "latching";
	static {
		baseCreaters.put("lighted_button", LightedButton::new);
		baseCreaters.put("label", Label::new);
		baseCreaters.put("indicator_light", IndicatorLight::new);
		baseCreaters.put("slider", Slider::new);
	}
	protected abstract void writeCustomNBT(NBTTagCompound nbt, boolean toItem);
	protected abstract void readCustomNBT(NBTTagCompound nbt);
	// DON'T OFFSET BY x, y IN THIS METHOD!
	public abstract List<RawQuad> getQuads();
	@Nonnull
	public abstract PanelComponent copyOf();

	//well, only relative in the x/z directions
	@Nonnull
	public abstract AxisAlignedBB getBlockRelativeAABB();

	public abstract boolean interactWith(Vec3d hitRelative, TileEntityPanel tile);

	public abstract void update(TileEntityPanel tile);

	@Nullable
	public Consumer<byte[]> getRSInputHandler(int id, TileEntityPanel panel) {
		return null;
	}

	public void registerRSOutput(int id, @Nonnull BiConsumer<Integer, Byte> out) {}

	public void unregisterRSOutput(int id, @Nonnull BiConsumer<Integer, Byte> out) {}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public abstract float getHeight();

	public void setX(float x) {
		this.x = x;
		aabb = null;
	}

	public void setY(float y) {
		this.y = y;
		aabb = null;
	}

	public void setPanelHeight(float panelHeight) {
		this.panelHeight = panelHeight;
	}

	public void writeToNBT(NBTTagCompound nbt, boolean toItem) {
		writeCustomNBT(nbt, toItem);
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
			IELogger.info("(IndustrialWires) Unknown panel component: "+type);//TODO own logger?
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
		RenderGlobal.func_189697_a(te.apply(te.getComponents().getPanelTopTransform(), getBlockRelativeAABB()).expandXyz(0.002).offset(px, py, pz), 0.0F, 0.0F, 0.0F, 0.4F);
		GlStateManager.depthMask(true);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
	}

	public abstract void renderInGUI(GuiPanelCreator gui);

	public void renderInGUIDefault(GuiPanelCreator gui, int color) {
		color |= 0xff000000;
		AxisAlignedBB aabb = getBlockRelativeAABB();
		int left = (int) (gui.getX0()+aabb.minX*gui.panelSize);
		int top = (int) (gui.getY0()+aabb.minZ*gui.panelSize);
		int right = (int) (gui.getX0()+aabb.maxX*gui.panelSize);
		int bottom = (int) (gui.getY0()+aabb.maxZ*gui.panelSize);
		Gui.drawRect(left, top, right, bottom, color);
	}


	public boolean isValidPos() {
		AxisAlignedBB aabb = getBlockRelativeAABB().offset(0, panelHeight, 0);
		if (aabb.minX<0||aabb.maxX>1) {
			return false;
		}
		if (aabb.minY<0||aabb.maxY>1) {
			return false;
		}
		if (aabb.minZ<0||aabb.maxZ>1) {
			return false;
		}
		return true;
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
	//TODO make abstract & implement
	public ItemStack getIngredientStack() {
		return new ItemStack(Items.BEETROOT_SOUP);
	}
}

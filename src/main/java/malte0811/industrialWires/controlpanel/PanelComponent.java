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

package malte0811.industrialWires.controlpanel;

import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.controlpanel.TileEntityPanel;
import malte0811.industrialWires.client.RawQuad;
import malte0811.industrialWires.client.gui.GuiPanelCreator;
import malte0811.industrialWires.controlpanel.ControlPanelNetwork.IOwner;
import malte0811.industrialWires.util.TriConsumer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Supplier;

public abstract class PanelComponent implements IOwner {
	public static final float Y_DELTA = .001F;
	protected static final float[] GRAY = {.8F, .8F, .8F};
	protected static final int GRAY_INT = 0xFFD0D0D0;
	protected static final float[] BLACK = {0, 0, 0, 1};
	protected float panelHeight;
	protected AxisAlignedBB aabb = null;
	protected float x, y;
	private final String type;
	protected TileEntityPanel panel;
	protected ControlPanelNetwork network;

	private Set<TriConsumer<Integer, Byte, PanelComponent>> outputs = new HashSet<>();

	protected PanelComponent(String type) {
		this.type = type;
	}

	public static final Map<String, Supplier<PanelComponent>> baseCreaters = new HashMap<>();

	public static void init() {
		baseCreaters.put("lighted_button", LightedButton::new);
		baseCreaters.put("label", Label::new);
		baseCreaters.put("indicator_light", IndicatorLight::new);
		baseCreaters.put("slider", Slider::new);
		baseCreaters.put("variac", Variac::new);
		baseCreaters.put("toggle_switch", ToggleSwitch::new);
		baseCreaters.put("toggle_switch_covered", CoveredToggleSwitch::new);
		baseCreaters.put("lock", Lock::new);
		baseCreaters.put("panel_meter", PanelMeter::new);
		baseCreaters.put(SevenSegDisplay.NAME, SevenSegDisplay::new);
		//Check that all components implement equals+hashCode if in a dev env
		boolean isDevEnv = "NBTTagCompound".equals(NBTTagCompound.class.getSimpleName());
		if (isDevEnv) {
			for (Supplier<PanelComponent> sup:baseCreaters.values()) {
				PanelComponent comp = sup.get();
				try {
					comp.getClass().getDeclaredMethod("equals", Object.class);
					comp.getClass().getDeclaredMethod("hashCode");
				} catch (NoSuchMethodException e) {
					throw new RuntimeException(comp.getClass()+" lacks equals or hasCode! This will break the cache!", e);
				}
			}
		}
	}

	protected abstract void writeCustomNBT(NBTTagCompound nbt, boolean toItem);

	protected abstract void readCustomNBT(NBTTagCompound nbt);

	// DON'T OFFSET BY x, y IN THIS METHOD!
	@SideOnly(Side.CLIENT)
	public abstract List<RawQuad> getQuads();

	@Nonnull
	public abstract PanelComponent copyOf();

	//well, only relative in the x/z directions
	@Nonnull
	public abstract AxisAlignedBB getBlockRelativeAABB();

	public abstract void interactWith(Vec3d hitRelative, EntityPlayerMP player);

	public abstract void update();

	public abstract int getColor();

	public abstract float getHeight();

	public void setNetwork(ControlPanelNetwork net, TileEntityPanel panel) {
		this.panel = panel;
		this.network = net;
	}

	public void dropItems() {
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

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

	@Override
	public BlockPos getBlockPos() {
		return panel.getBlockPos();
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
			ret.readFromNBT(nbt);
			return ret;
		} else {
			IndustrialWires.logger.warn("Unknown panel component: " + type);
			return null;
		}
	}

	public final void readFromNBT(NBTTagCompound nbt) {
		readCustomNBT(nbt);
		setX(nbt.getFloat("x"));
		setY(nbt.getFloat("y"));
		setPanelHeight(nbt.getFloat("panelHeight"));
	}

	public boolean isValidPos(List<PanelComponent> components, float height, float angle) {
		float h = PanelUtils.getHeightWithComponent(this, angle, height);
		if (h < 0 || h > 1) {
			return false;
		}

		AxisAlignedBB aabb = getBlockRelativeAABB();
		if (aabb.minX < 0 || aabb.maxX > 1) {
			return false;
		}
		if (aabb.minZ < 0 || aabb.maxZ > 1) {
			return false;
		}

		for (PanelComponent pc : components) {
			if (pc == this) {
				continue;
			}
			AxisAlignedBB otherBB = pc.getBlockRelativeAABB();
			if (PanelUtils.intersectXZ(aabb, otherBB)) {
				return false;
			}
		}
		return true;
	}

	@SideOnly(Side.CLIENT)
	public void renderBox() {
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.glLineWidth(2.0F);
		GlStateManager.disableTexture2D();
		GlStateManager.depthMask(false);
		panel.getComponents().transformGLForTop(panel.getBlockPos());
		RenderGlobal.drawSelectionBoundingBox(getBlockRelativeAABB().grow(0.002),
				0.0F, 0.0F, 0.0F, 0.4F);
		GlStateManager.depthMask(true);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}

	@SideOnly(Side.CLIENT)
	public abstract void renderInGUI(GuiPanelCreator gui);

	@SideOnly(Side.CLIENT)
	public void renderInGUIDefault(GuiPanelCreator gui, int color) {
		color |= 0xff000000;
		AxisAlignedBB aabb = getBlockRelativeAABB();
		int left = (int) (gui.getX0() + aabb.minX * gui.panelSize);
		int top = (int) (gui.getY0() + aabb.minZ * gui.panelSize);
		int right = (int) (gui.getX0() + aabb.maxX * gui.panelSize);
		int bottom = (int) (gui.getY0() + aabb.maxZ * gui.panelSize);
		Gui.drawRect(left, top, right, bottom, color);
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

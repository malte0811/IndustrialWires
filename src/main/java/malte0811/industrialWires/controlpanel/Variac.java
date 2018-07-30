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

import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.client.RawQuad;
import malte0811.industrialWires.client.gui.GuiPanelCreator;
import malte0811.industrialWires.controlpanel.ControlPanelNetwork.RSChannel;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static malte0811.industrialWires.util.NBTKeys.*;

public class Variac extends PanelComponent implements IConfigurableComponent {
	private static final float SIZE = 3 / 16F;
	private static final float innerSize = (float) (Math.sqrt(2) / 2 * SIZE);
	private static final float offset = (SIZE - innerSize) / 2;
	private static final float[] darkGray = {.2F, .2F, .2F, 1};
	private static final float[] white = {1, 1, 1, 1};
	private static final float rodDia = .0625F / 2;
	private static final float rodOffset = (SIZE - rodDia) / 2;
	private static final float arrowSize = .0625F / 2;

	private int out;
	@Nonnull
	private RSChannel primary = RSChannel.INVALID_CHANNEL;
	@Nonnull
	private RSChannel secondary = RSChannel.INVALID_CHANNEL;

	public Variac(@Nonnull RSChannel primary, @Nonnull RSChannel secondary) {
		this();
		this.primary = primary;
		this.secondary = secondary;
	}

	public Variac() {
		super("variac");
	}

	@Override
	protected void writeCustomNBT(NBTTagCompound nbt, boolean toItem) {
		if (!toItem) {
			nbt.setInteger("output", out);
		}
		nbt.setInteger(RS_ID, primary.getController());
		nbt.setByte(RS_CHANNEL, primary.getColor());
		nbt.setInteger(RS_ID2, secondary.getController());
		nbt.setByte(RS_CHANNEL2, secondary.getColor());
	}

	@Override
	protected void readCustomNBT(NBTTagCompound nbt) {
		out = nbt.getInteger("output");
		int rsController = nbt.getInteger(RS_ID);
		byte rsColor = nbt.getByte(RS_CHANNEL);
		primary = new RSChannel(rsController, rsColor);
		if (nbt.hasKey(RS_ID2)) {
			rsController = nbt.getInteger(RS_ID2);
			rsColor = nbt.getByte(RS_CHANNEL2);
			secondary = new RSChannel(rsController, rsColor);
		} else {
			secondary = RSChannel.INVALID_CHANNEL;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public List<RawQuad> getQuads() {
		List<RawQuad> ret = new ArrayList<>();
		float angle = -(float) (2 * Math.PI * (8.5 + out) / (17F*16F));
		Matrix4 mat = new Matrix4();
		mat.translate(SIZE / 2, 0, SIZE / 2);
		mat.rotate(angle, 0, 1, 0);
		mat.translate(-SIZE / 2, 0, -SIZE / 2);
		PanelUtils.addColoredBox(darkGray, darkGray, null, new Vector3f(offset, getHeight() / 2, offset),
				new Vector3f(innerSize, getHeight() / 2, innerSize), ret, false, mat);
		PanelUtils.addColoredBox(GRAY, GRAY, null, new Vector3f(rodOffset, 0, rodOffset),
				new Vector3f(rodDia, getHeight() / 2, rodDia), ret, false, mat);
		mat.translate(SIZE / 2, 0, SIZE / 2);
		mat.rotate(Math.PI / 4, 0, 1, 0);
		mat.translate(-SIZE / 2, 0, -SIZE / 2);
		PanelUtils.addColoredBox(darkGray, darkGray, null, new Vector3f(offset, getHeight() / 2, offset),
				new Vector3f(innerSize, getHeight() / 2, innerSize), ret, false, mat);
		mat.translate(SIZE / 2, 0, SIZE / 2);
		mat.rotate(Math.PI / 2, 0, 1, 0);
		mat.translate(-SIZE / 2, Y_DELTA, -SIZE / 2);
		PanelUtils.addColoredQuad(ret, new Vector3f(offset, getHeight(), offset), new Vector3f(offset, getHeight(), offset),
				new Vector3f(offset + arrowSize / 2, getHeight(), offset + arrowSize),
				new Vector3f(offset + arrowSize, getHeight(), offset + arrowSize / 2), EnumFacing.UP, white, mat);
		return ret;
	}

	@Nonnull
	@Override
	public PanelComponent copyOf() {
		Variac ret = new Variac(primary, secondary);
		ret.out = out;
		ret.setX(x);
		ret.setY(y);
		ret.panelHeight = panelHeight;
		return ret;
	}

	@Nonnull
	@Override
	public AxisAlignedBB getBlockRelativeAABB() {
		if (aabb == null) {
			aabb = new AxisAlignedBB(x, 0, y, x + SIZE, getHeight(), y + SIZE);
		}
		return aabb;
	}

	@Override
	public void interactWith(Vec3d hitRelative, EntityPlayerMP player) {
		double xRel = hitRelative.x - SIZE / 2;
		double yRel = -(hitRelative.z - SIZE / 2);
		double angle = 1.5 * Math.PI - Math.atan2(yRel, xRel);
		if (angle < 0) {
			angle += 2 * Math.PI;
		} else if (angle > 2 * Math.PI) {
			angle -= 2 * Math.PI;
		}
		angle /= 2 * Math.PI;
		int step = (secondary.isValid()&&player.isSneaking())?1:16;
		int newLevel = (int) ((angle-1/34F) * 17 * 16);
		int diff = Math.abs(newLevel-out);
		if (diff>step) {
			if (newLevel > out) {
				newLevel = out + step;
			} else if (newLevel < out) {
				newLevel = out - step;
			}
		}
		newLevel = Math.max(0, Math.min(newLevel, 255));
		if (newLevel != out) {
			setOut(newLevel);
			out = newLevel;
			panel.markDirty();
			panel.triggerRenderUpdate();
		}
	}

	@Override
	public void update() {

	}

	@Override
	public float getHeight() {
		return .0625F;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderInGUI(GuiPanelCreator gui) {
		AxisAlignedBB aabb = getBlockRelativeAABB();
		int left = (int) Math.ceil(gui.getX0() + (offset + aabb.minX) * gui.panelSize);
		int top = (int) Math.ceil(gui.getY0() + (offset + aabb.minZ) * gui.panelSize);
		int right = (int) Math.floor(gui.getX0() + (aabb.maxX - offset) * gui.panelSize);
		int bottom = (int) Math.floor(gui.getY0() + (aabb.maxZ - offset) * gui.panelSize);

		GlStateManager.pushMatrix();
		GlStateManager.translate((left + right) / 2, (top + bottom) / 2, 0);
		GlStateManager.rotate(360 / 17F, 0, 0, 1);
		GlStateManager.translate(-(left + right) / 2, -(top + bottom) / 2, 0);
		Gui.drawRect(left, top, right, bottom, 0xff333333);
		GlStateManager.translate((left + right) / 2, (top + bottom) / 2, 0);
		GlStateManager.rotate(45, 0, 0, 1);
		GlStateManager.translate(-(left + right) / 2, -(top + bottom) / 2, 0);
		Gui.drawRect(left, top, right, bottom, 0xff333333);
		GlStateManager.popMatrix();
	}

	public void setOut(int value) {
		network.setOutputs(this, new ControlPanelNetwork.RSChannelState(primary, (byte) (value>>4)));
		network.setOutputs(this, new ControlPanelNetwork.RSChannelState(secondary, (byte) (value&0xf)));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		Variac variac = (Variac) o;

		if (out != variac.out) return false;
		if (!primary.equals(variac.primary)) return false;
		return secondary.equals(variac.secondary);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + out;
		result = 31 * result + primary.hashCode();
		result = 31 * result + secondary.hashCode();
		return result;
	}

	@Override
	public void applyConfigOption(IConfigurableComponent.ConfigType type, int id, NBTBase value) {
		switch (type) {
			case RS_CHANNEL:
				if (id == 0) {
					primary = primary.withColor(value);
				} else {
					secondary = secondary.withColor(value);
				}
				break;
			case INT:
				if (id == 0) {
					primary = primary.withController(value);
				} else {
					secondary = secondary.withController(value);
				}
				break;
		}
	}

	@Override
	public String fomatConfigName(IConfigurableComponent.ConfigType type, int id) {
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String fomatConfigDescription(IConfigurableComponent.ConfigType type, int id) {
		switch (type) {
			case RS_CHANNEL:
				return I18n.format(IndustrialWires.MODID + ".desc.rschannel_info" + (id == 1 ? "2" : ""));
			case INT:
				return I18n.format(IndustrialWires.MODID + ".desc.rsid_info" + (id == 1 ? "2" : ""));
			default:
				return "INVALID?";
		}
	}

	@Override
	public RSColorConfig[] getRSChannelOptions() {
		return new RSColorConfig[]{
				new RSColorConfig("channel", 0, 0, primary.getColor(), false),
				new RSColorConfig("channel2", 60, 0, secondary.getColor(), false)
		};
	}

	@Override
	public IntConfig[] getIntegerOptions() {
		return new IntConfig[]{
				new IntConfig("rsId", 0, 60, primary.getController(), 2, false),
				new IntConfig("rsId2", 60, 60, secondary.getController(), 2, true)
		};
	}

	@Override
	public int getColor() {
		return 0xffffff;
	}
}
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

import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.controlpanel.TileEntityPanel;
import malte0811.industrialWires.client.RawQuad;
import malte0811.industrialWires.client.gui.GuiPanelCreator;
import malte0811.industrialWires.util.TriConsumer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class Variac extends PanelComponent implements IConfigurableComponent {
	private static final float SIZE = 3 / 16F;
	private static final float innerSize = (float) (Math.sqrt(2) / 2 * SIZE);
	private static final float offset = (SIZE - innerSize) / 2;
	private static final float[] darkGray = {.2F, .2F, .2F, 1};
	private static final float[] white = {1, 1, 1, 1};
	private static final float rodDia = .0625F / 2;
	private static final float rodOffset = (SIZE - rodDia) / 2;
	private static final float arrowSize = .0625F / 2;

	private byte out;
	private byte rsChannel;
	private int rsId;

	public Variac(int rsId, byte rsChannel) {
		this();
		this.rsChannel = rsChannel;
		this.rsId = rsId;
	}

	public Variac() {
		super("variac");
	}

	@Override
	protected void writeCustomNBT(NBTTagCompound nbt, boolean toItem) {
		if (!toItem) {
			nbt.setByte("output", out);
		}
		nbt.setByte(RS_CHANNEL, rsChannel);
		nbt.setInteger(RS_ID, rsId);
	}

	@Override
	protected void readCustomNBT(NBTTagCompound nbt) {
		out = nbt.getByte("output");
		rsChannel = nbt.getByte(RS_CHANNEL);
		rsId = nbt.getInteger(RS_ID);
	}

	@Override
	public List<RawQuad> getQuads() {
		List<RawQuad> ret = new ArrayList<>();
		float angle = -(float) (2 * Math.PI * (.5 + out) / 17F);
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
		mat.translate(-SIZE / 2, 0, -SIZE / 2);
		PanelUtils.addColoredQuad(ret, new Vector3f(offset, getHeight() + .00001F, offset), new Vector3f(offset, getHeight() + .00001F, offset),
				new Vector3f(offset + arrowSize / 2, getHeight() + .00001F, offset + arrowSize),
				new Vector3f(offset + arrowSize, getHeight() + .00001F, offset + arrowSize / 2), EnumFacing.UP, white, mat);
		return ret;
	}

	@Nonnull
	@Override
	public PanelComponent copyOf() {
		Variac ret = new Variac(rsId, rsChannel);
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
	public void interactWith(Vec3d hitRelative, TileEntityPanel tile, EntityPlayerMP player) {
		double xRel = hitRelative.xCoord - SIZE / 2;
		double yRel = -(hitRelative.zCoord - SIZE / 2);
		double angle = 1.5 * Math.PI - Math.atan2(yRel, xRel);
		if (angle < 0) {
			angle += 2 * Math.PI;
		} else if (angle > 2 * Math.PI) {
			angle -= 2 * Math.PI;
		}
		angle -= .5 * Math.PI / 17;
		angle /= 2 * Math.PI;
		if (angle < 0 || angle >= 16 / 17D) {
			return;
		}
		byte newLevel = (byte) (angle * 17);
		if (newLevel > out) {
			newLevel = (byte) (out + 1);
		} else if (newLevel < out) {
			newLevel = (byte) (out - 1);
		}
		newLevel = (byte) Math.max(0, Math.min(newLevel, 15));
		if (newLevel != out) {
			setOut(rsChannel, newLevel);
			out = newLevel;
			tile.triggerRenderUpdate();
		}
	}

	@Override
	public void registerRSOutput(int id, @Nonnull TriConsumer<Integer, Byte, PanelComponent> out) {
		if (id == rsId) {
			super.registerRSOutput(id, out);
			out.accept((int) rsChannel, this.out, this);
		}
	}

	@Override
	public void update(TileEntityPanel tile) {

	}

	@Override
	public float getHeight() {
		return .0625F;
	}

	@Override
	public void renderInGUI(GuiPanelCreator gui) {
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

	@Override
	public void invalidate(TileEntityPanel te) {
		setOut(rsChannel, 0);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		Variac variac = (Variac) o;

		if (out != variac.out) return false;
		return rsChannel == variac.rsChannel;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (int) out;
		result = 31 * result + (int) rsChannel;
		return result;
	}

	@Override
	public void applyConfigOption(IConfigurableComponent.ConfigType type, int id, NBTBase value) {
		switch (type) {
		case RS_CHANNEL:
			rsChannel = ((NBTTagByte) value).getByte();
			break;
		case INT:
			rsId = ((NBTTagInt) value).getInt();
			break;
		}
	}

	@Override
	public String fomatConfigName(IConfigurableComponent.ConfigType type, int id) {
		return null;
	}

	@Override
	public String fomatConfigDescription(IConfigurableComponent.ConfigType type, int id) {
		switch (type) {
		case RS_CHANNEL:
			return I18n.format(IndustrialWires.MODID + ".desc.rschannel_info");
		case INT:
			return I18n.format(IndustrialWires.MODID + ".desc.rsid_info");
		default:
			return "INVALID?";
		}
	}

	@Override
	public IConfigurableComponent.RSChannelConfig[] getRSChannelOptions() {
		return new IConfigurableComponent.RSChannelConfig[]{
				new IConfigurableComponent.RSChannelConfig("channel", 0, 0, rsChannel)
		};
	}

	@Override
	public IConfigurableComponent.IntConfig[] getIntegerOptions() {
		return new IConfigurableComponent.IntConfig[]{
				new IConfigurableComponent.IntConfig("rsId", 0, 50, rsId, 2, false)
		};
	}

	@Override
	public int getColor() {
		return 0xffffff;
	}
}
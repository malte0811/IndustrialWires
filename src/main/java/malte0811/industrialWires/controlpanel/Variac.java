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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	private byte rsChannel;
	private int rsId;
	private boolean hasSecond;
	private byte rsChannel2;
	private int rsId2 = -1;
	private Set<TriConsumer<Integer, Byte, PanelComponent>> secOutputs = new HashSet<>();

	public Variac(int rsId, byte rsChannel, int rsId2, byte rsChannel2, boolean hasSecond) {
		this();
		this.rsChannel = rsChannel;
		this.rsId = rsId;
		this.hasSecond = hasSecond;
		this.rsChannel2 = rsChannel2;
		this.rsId2 = rsId2;
	}

	public Variac() {
		super("variac");
	}

	@Override
	protected void writeCustomNBT(NBTTagCompound nbt, boolean toItem) {
		if (!toItem) {
			nbt.setInteger("output", out);
		}
		nbt.setByte(RS_CHANNEL, rsChannel);
		nbt.setInteger(RS_ID, rsId);
		nbt.setByte(RS_CHANNEL2, rsChannel2);
		nbt.setInteger(RS_ID2, rsId2);
	}

	@Override
	protected void readCustomNBT(NBTTagCompound nbt) {
		out = nbt.getInteger("output");
		rsChannel = nbt.getByte(RS_CHANNEL);
		rsId = nbt.getInteger(RS_ID);
		rsChannel2 = nbt.getByte(RS_CHANNEL2);
		rsId2 = nbt.getInteger(RS_ID2);
		hasSecond = rsChannel2>=0&&rsId2>=0;
		if (!hasSecond) {
			rsChannel2 = -1;
			rsId2 = -1;
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
		Variac ret = new Variac(rsId, rsChannel, rsId2, rsChannel2, hasSecond);
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
		double xRel = hitRelative.x - SIZE / 2;
		double yRel = -(hitRelative.z - SIZE / 2);
		double angle = 1.5 * Math.PI - Math.atan2(yRel, xRel);
		if (angle < 0) {
			angle += 2 * Math.PI;
		} else if (angle > 2 * Math.PI) {
			angle -= 2 * Math.PI;
		}
		angle /= 2 * Math.PI;
		int step = (hasSecond&&player.isSneaking())?1:16;
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
			tile.markDirty();
			tile.triggerRenderUpdate();
		}
	}

	@Override
	public void registerRSOutput(int id, @Nonnull TriConsumer<Integer, Byte, PanelComponent> out) {
		if (matchesId(rsId, id)) {
			super.registerRSOutput(id, out);
			out.accept((int) rsChannel, (byte) (this.out>>4), this);
		}
		if (matchesId(rsId2, id)&&hasSecond) {
			secOutputs.add(out);
			out.accept((int)rsChannel2, (byte) (this.out&0xf), this);
		}
	}

	@Override
	public void unregisterRSOutput(int id, @Nonnull TriConsumer<Integer, Byte, PanelComponent> out) {
		super.unregisterRSOutput(id, out);
		secOutputs.remove(out);
	}

	@Override
	public void update(TileEntityPanel tile) {

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

	@Override
	public void invalidate(TileEntityPanel te) {
		setOut(0);
	}

	public void setOut(int level) {
		if (hasSecond) {
			for (TriConsumer<Integer, Byte, PanelComponent> cons:secOutputs) {
				cons.accept((int)rsChannel2, (byte) (level&0xf), this);
			}
		}
		super.setOut(rsChannel, (byte)(level>>4));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		Variac variac = (Variac) o;

		if (out != variac.out) return false;
		if (rsChannel != variac.rsChannel) return false;
		if (rsId != variac.rsId) return false;
		if (hasSecond != variac.hasSecond) return false;
		if (rsChannel2 != variac.rsChannel2) return false;
		return rsId2 == variac.rsId2;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + out;
		result = 31 * result + (int) rsChannel;
		result = 31 * result + rsId;
		result = 31 * result + (hasSecond ? 1 : 0);
		result = 31 * result + (int) rsChannel2;
		result = 31 * result + rsId2;
		return result;
	}

	@Override
	public void applyConfigOption(IConfigurableComponent.ConfigType type, int id, NBTBase value) {
		switch (type) {
		case RS_CHANNEL:
			if (id==0) {
				rsChannel = ((NBTTagByte) value).getByte();
			} else {
				rsChannel2 = ((NBTTagByte) value).getByte();
			}
			break;
		case INT:
			if (id==0) {
				rsId = ((NBTTagInt) value).getInt();
			} else {
				rsId2 = ((NBTTagInt) value).getInt();
				hasSecond = rsId2>=0;
			}
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
				return I18n.format(IndustrialWires.MODID + ".desc.rschannel_info" + (id == 1 ? "2" : ""));
			case INT:
				return I18n.format(IndustrialWires.MODID + ".desc.rsid_info" + (id == 1 ? "2" : ""));
			default:
				return "INVALID?";
		}
	}

	@Override
	public IConfigurableComponent.RSChannelConfig[] getRSChannelOptions() {
		return new IConfigurableComponent.RSChannelConfig[]{
				new IConfigurableComponent.RSChannelConfig("channel", 0, 0, rsChannel),
				new IConfigurableComponent.RSChannelConfig("channel", 90, 0, rsChannel2)
		};
	}

	@Override
	public IConfigurableComponent.IntConfig[] getIntegerOptions() {
		return new IConfigurableComponent.IntConfig[]{
				new IConfigurableComponent.IntConfig("rsId", 0, 50, rsId, 2, false),
				new IConfigurableComponent.IntConfig("rsId", 90, 50, rsId2, 2, true)
		};
	}

	@Override
	public int getColor() {
		return 0xffffff;
	}
}
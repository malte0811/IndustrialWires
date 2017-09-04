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

import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.controlpanel.TileEntityPanel;
import malte0811.industrialWires.client.RawQuad;
import malte0811.industrialWires.client.gui.GuiPanelCreator;
import malte0811.industrialWires.util.TriConsumer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Slider extends PanelComponent implements IConfigurableComponent {
	private static final float WIDTH = .0625F;
	private float length = .5F;
	private int color = 0xffff00;
	private boolean horizontal;
	private int out;
	private byte rsChannel;
	private int rsId;
	private boolean hasSecond;
	private byte rsChannel2;
	private int rsId2 = -1;
	private Set<TriConsumer<Integer, Byte, PanelComponent>> secOutputs = new HashSet<>();

	public Slider(float length, int color, boolean horizontal, int rsId, byte rsChannel, boolean hasSecond, int rsId2, byte rsChannel2) {
		this();
		this.color = color;
		this.length = length;
		this.horizontal = horizontal;
		this.rsChannel = rsChannel;
		this.rsId = rsId;
		this.hasSecond = hasSecond;
		this.rsChannel2 = rsChannel2;
		this.rsId2 = rsId2;
	}

	public Slider() {
		super("slider");
	}

	@Override
	protected void writeCustomNBT(NBTTagCompound nbt, boolean toItem) {
		nbt.setInteger(COLOR, color);
		nbt.setFloat(LENGTH, length);
		if (!toItem) {
			nbt.setInteger("output", out);
		}
		nbt.setByte(RS_CHANNEL, rsChannel);
		nbt.setInteger(RS_ID, rsId);
		nbt.setByte(RS_CHANNEL2, rsChannel2);
		nbt.setInteger(RS_ID2, rsId2);
		nbt.setBoolean(HAS_SECOND_CHANNEL, hasSecond);
		nbt.setBoolean(HORIZONTAL, horizontal);
	}

	@Override
	protected void readCustomNBT(NBTTagCompound nbt) {
		color = nbt.getInteger(COLOR);
		length = nbt.getFloat(LENGTH);
		out = nbt.getInteger("output");
		rsChannel = nbt.getByte(RS_CHANNEL);
		rsId = nbt.getInteger(RS_ID);
		rsChannel2 = nbt.getByte(RS_CHANNEL2);
		rsId2 = nbt.getInteger(RS_ID2);
		hasSecond = nbt.getBoolean(HAS_SECOND_CHANNEL);
		horizontal = nbt.getBoolean(HORIZONTAL);
	}

	@Override
	public List<RawQuad> getQuads() {
		List<RawQuad> ret = new ArrayList<>();
		final float yOff = .001F;
		float xSize = horizontal ? length : WIDTH;
		float ySize = horizontal ? WIDTH : length;
		PanelUtils.addColoredQuad(ret, new Vector3f(0, yOff, 0), new Vector3f(0, yOff, ySize), new Vector3f(xSize, yOff, ySize), new Vector3f(xSize, yOff, 0),
				EnumFacing.UP, GRAY);
		float[] color = new float[4];
		color[3] = 1;
		for (int i = 0; i < 3; i++) {
			color[i] = ((this.color >> (8 * (2 - i))) & 255) / 255F * (.5F + out / 255F/2);
		}
		float val;
		if (horizontal) {
			val = (out / 255F) * (length - .0625F);
		} else {
			val = (1 - out / 255F) * (length - .0625F);
		}
		PanelUtils.addColoredBox(color, GRAY, null, new Vector3f(horizontal ? val : 0, 0, horizontal ? 0 : val),
				new Vector3f(.0625F, getHeight(), .0625F), ret, false);
		if (out>0) {
			ret.get(1).light = 0xff0ff;
		}
		return ret;
	}

	@Nonnull
	@Override
	public PanelComponent copyOf() {
		Slider ret = new Slider(length, color, horizontal, rsId, rsChannel, hasSecond, rsId2, rsChannel2);
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
			aabb = new AxisAlignedBB(x, 0, y, x + (horizontal ? length : WIDTH), getHeight(), y + (horizontal ? WIDTH : length));
		}
		return aabb;
	}

	@Override
	public void interactWith(Vec3d hitRelative, TileEntityPanel tile, EntityPlayerMP player) {
		double pos = horizontal ? hitRelative.x : (length - hitRelative.z);
		int newLevel = (int) Math.min(pos * 256 / length, 255);
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
		if (matchesId(rsId2, id)) {
			secOutputs.add(out);
			out.accept((int) rsChannel, (byte) (this.out&0xf), this);
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
		return .0625F / 2;
	}

	@Override
	public void renderInGUI(GuiPanelCreator gui) {
		renderInGUIDefault(gui, GRAY_INT);
		double middleX = (getX() + (horizontal ? length : .0625) / 2);
		double middleY = (getY() + (horizontal ? .0625 : length) / 2);
		int left = gui.getX0() + (int) ((middleX - .0625 / 2) * gui.panelSize);
		int right = gui.getX0() + (int) ((middleX + .0625 / 2) * gui.panelSize);
		int top = gui.getY0() + (int) ((middleY - .0625 / 2) * gui.panelSize);
		int bottom = gui.getY0() + (int) ((middleY + .0625 / 2) * gui.panelSize);
		Gui.drawRect(left, top, right, bottom, 0xff000000 | color);
	}

	@Override
	public void invalidate(TileEntityPanel te) {
		setOut(0);
	}

	public void setOut(int value) {
		super.setOut(rsChannel, value>>4);
		if (hasSecond) {
			for (TriConsumer<Integer, Byte, PanelComponent> cons:secOutputs) {
				cons.accept((int) rsChannel2, (byte) (value&0xf), this);
			}
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		Slider slider = (Slider) o;

		if (Float.compare(slider.length, length) != 0) return false;
		if (color != slider.color) return false;
		if (horizontal != slider.horizontal) return false;
		if (out != slider.out) return false;
		if (rsChannel != slider.rsChannel) return false;
		if (rsId != slider.rsId) return false;
		if (hasSecond != slider.hasSecond) return false;
		if (rsChannel2 != slider.rsChannel2) return false;
		return rsId2 == slider.rsId2;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (length != +0.0f ? Float.floatToIntBits(length) : 0);
		result = 31 * result + color;
		result = 31 * result + (horizontal ? 1 : 0);
		result = 31 * result + out;
		result = 31 * result + (int) rsChannel;
		result = 31 * result + rsId;
		result = 31 * result + (hasSecond ? 1 : 0);
		result = 31 * result + (int) rsChannel2;
		result = 31 * result + rsId2;
		return result;
	}

	@Override
	public void applyConfigOption(ConfigType type, int id, NBTBase value) {
		switch (type) {
		case BOOL:
			horizontal = ((NBTTagByte) value).getByte() != 0;
			break;
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
		case FLOAT:
			if (id < 3) {
				color = PanelUtils.setColor(color, id, value);
			} else {
				length = scaleToRangePercent(((NBTTagFloat) value).getFloat(), .125F, 1);
			}
			break;
		}
	}

	private float scaleToRangePercent(float x, float min, float max) {
		return min + x / 100F * (max - min);
	}

	@Override
	public String fomatConfigName(ConfigType type, int id) {
		switch (type) {
		case BOOL:
			return I18n.format(IndustrialWires.MODID + ".tooltip.horizontal");
		case RS_CHANNEL:
		case INT:
			return null;
		case FLOAT:
			return I18n.format(IndustrialWires.MODID + ".desc." + (id == 0 ? "red" : (id == 1 ? "green" : id == 2 ? "blue" : "length")));
		default:
			return "INVALID";
		}
	}

	@Override
	public String fomatConfigDescription(ConfigType type, int id) {
		switch (type) {
		case BOOL:
			return null;
		case RS_CHANNEL:
			return I18n.format(IndustrialWires.MODID + ".desc.rschannel_info"+(id==0?"":"2"));
		case INT:
			return I18n.format(IndustrialWires.MODID + ".desc.rsid_info"+(id==0?"":"2"));
		case FLOAT:
			return null;
		default:
			return "INVALID?";
		}
	}

	@Override
	public RSChannelConfig[] getRSChannelOptions() {
		return new RSChannelConfig[]{
				new RSChannelConfig("channel", 0, 0, rsChannel, true),
				new RSChannelConfig("channel", 30, 0, rsChannel2, true)
		};
	}

	@Override
	public IntConfig[] getIntegerOptions() {
		return new IntConfig[]{
				new IntConfig("rsId", 0, 30, rsId, 2, false),
				new IntConfig("rsId", 30, 30, rsId2, 2, true)
		};
	}

	@Override
	public BoolConfig[] getBooleanOptions() {
		return new BoolConfig[]{
				new BoolConfig("horizontal", 0, 70, horizontal)
		};
	}

	@Override
	public FloatConfig[] getFloatOptions() {
		float[] color = PanelUtils.getFloatColor(true, this.color);
		int x = 70;
		int yOffset = 10;
		return new FloatConfig[]{
				new FloatConfig("red", x, yOffset + 20, color[0], 60),
				new FloatConfig("green", x, yOffset + 40, color[1], 60),
				new FloatConfig("blue", x, yOffset + 60, color[2], 60),
				new FloatConfig("length", x, yOffset, (length - .125F) / (1 - .125F), 60)
		};
	}

	@Override
	public int getColor() {
		return color;
	}
}

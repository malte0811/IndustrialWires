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

package malte0811.industrialwires.controlpanel;

import malte0811.industrialwires.IndustrialWires;
import malte0811.industrialwires.client.RawQuad;
import malte0811.industrialwires.client.gui.GuiPanelCreator;
import malte0811.industrialwires.controlpanel.ControlPanelNetwork.RSChannel;
import malte0811.industrialwires.controlpanel.ControlPanelNetwork.RSChannelState;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static malte0811.industrialwires.util.NBTKeys.*;

public class Slider extends PanelComponent implements IConfigurableComponent {
	private static final float WIDTH = .0625F;
	private float length = .5F;
	private int color = 0xffff00;
	private boolean horizontal;
	private int out;
	@Nonnull
	private RSChannel primary = RSChannel.DEFAULT_CHANNEL;
	@Nonnull
	private RSChannel secondary = RSChannel.INVALID_CHANNEL;

	public Slider(float length, int color, boolean horizontal, @Nonnull RSChannel primary, @Nonnull RSChannel secondary) {
		this();
		this.color = color;
		this.length = length;
		this.horizontal = horizontal;
		this.primary = primary;
		this.secondary = secondary;
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
		nbt.setInteger(RS_ID, primary.getController());
		nbt.setByte(RS_CHANNEL, primary.getColor());
		nbt.setInteger(RS_ID2, secondary.getController());
		nbt.setByte(RS_CHANNEL2, secondary.getColor());
		nbt.setBoolean(HORIZONTAL, horizontal);
	}

	@Override
	protected void readCustomNBT(NBTTagCompound nbt) {
		color = nbt.getInteger(COLOR);
		length = nbt.getFloat(LENGTH);
		horizontal = nbt.getBoolean(HORIZONTAL);
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
		float xSize = horizontal ? length : WIDTH;
		float ySize = horizontal ? WIDTH : length;
		PanelUtils.addColoredQuad(ret, new Vector3f(0, 0, 0), new Vector3f(0, 0, ySize), new Vector3f(xSize, 0, ySize),
				new Vector3f(xSize, 0, 0), EnumFacing.UP, GRAY);
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
		Slider ret = new Slider(length, color, horizontal, primary, secondary);
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
	public void interactWith(Vec3d hitRelative, EntityPlayerMP player) {
		double pos = horizontal ? hitRelative.x : (length - hitRelative.z);
		int newLevel = (int) Math.min(pos * 256 / length, 255);
		if (newLevel != out) {
			setOut(newLevel);
		}
	}

	@Override
	public void update() {

	}

	@Override
	public void setNetwork(ControlPanelNetwork net) {
		super.setNetwork(net);
		network.setOutputs(this, new RSChannelState(primary, (byte) (out>>4)));
		network.setOutputs(this, new RSChannelState(secondary, (byte) (out&0xf)));
	}

	@Override
	public float getHeight() {
		return .0625F / 2;
	}

	@Override
	@SideOnly(Side.CLIENT)
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

	public void setOut(int value) {
		network.setOutputs(this, new RSChannelState(primary, (byte) (value>>4)));
		network.setOutputs(this, new RSChannelState(secondary, (byte) (value&0xf)));
		out = value;
		panel.markDirty();
		panel.triggerRenderUpdate();
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
		if (!primary.equals(slider.primary)) return false;
		return secondary.equals(slider.secondary);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (length != +0.0f ? Float.floatToIntBits(length) : 0);
		result = 31 * result + color;
		result = 31 * result + (horizontal ? 1 : 0);
		result = 31 * result + out;
		result = 31 * result + primary.hashCode();
		result = 31 * result + secondary.hashCode();
		return result;
	}

	@Override
	public void applyConfigOption(ConfigType type, int id, NBTBase value) {
		switch (type) {
			case BOOL:
				horizontal = ((NBTTagByte) value).getByte() != 0;
				break;
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
	@SideOnly(Side.CLIENT)
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
	@SideOnly(Side.CLIENT)
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
	public RSColorConfig[] getRSChannelOptions() {
		return new RSColorConfig[]{
				new RSColorConfig("channel", 0, 0, primary.getColor(), true),
				new RSColorConfig("channel2", 30, 0, secondary.getColor(), true)
		};
	}

	@Override
	public IntConfig[] getIntegerOptions() {
		return new IntConfig[]{
				new IntConfig("rsId", 0, 30, primary.getController(), 2, false),
				new IntConfig("rsId2", 30, 30, secondary.getController(), 2, true)
		};
	}

	@Override
	public BoolConfig[] getBooleanOptions() {
		return new BoolConfig[]{
				new BoolConfig("horizontal", 0, 40, horizontal)
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

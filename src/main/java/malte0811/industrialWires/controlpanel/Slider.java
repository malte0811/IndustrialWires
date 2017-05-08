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
import java.util.function.BiConsumer;

public class Slider extends PanelComponent implements IConfigurableComponent {
	private static final float WIDTH = .0625F;
	private float length = .5F;
	private int color = 0xffff00;
	private boolean horizontal;
	private byte out;
	private byte rsChannel;
	private int rsId;
	private Set<BiConsumer<Integer, Byte>> outputs = new HashSet<>();
	public Slider(float length, int color, boolean horizontal, int rsId, byte rsChannel) {
		this();
		this.color = color;
		this.length = length;
		this.horizontal = horizontal;
		this.rsChannel = rsChannel;
		this.rsId = rsId;
	}
	public Slider() {
		super("slider");
	}
	@Override
	protected void writeCustomNBT(NBTTagCompound nbt, boolean toItem) {
		nbt.setInteger(COLOR, color);
		nbt.setFloat(LENGTH, length);
		if (!toItem) {
			nbt.setByte("output", out);
		}
		nbt.setByte(RS_CHANNEL, rsChannel);
		nbt.setInteger(RS_ID, rsId);
		nbt.setBoolean(HORIZONTAL, horizontal);
	}

	@Override
	protected void readCustomNBT(NBTTagCompound nbt) {
		color = nbt.getInteger(COLOR);
		length = nbt.getFloat(LENGTH);
		out = nbt.getByte("output");
		rsChannel = nbt.getByte(RS_CHANNEL);
		rsId = nbt.getInteger(RS_ID);
		horizontal = nbt.getBoolean(HORIZONTAL);
	}

	@Override
	public List<RawQuad> getQuads() {
		List<RawQuad> ret = new ArrayList<>();
		final float yOff = .001F;
		float xSize = horizontal?length:WIDTH;
		float ySize = horizontal?WIDTH:length;
		PanelUtils.addColoredQuad(ret, new Vector3f(0, yOff, 0), new Vector3f(0, yOff, ySize), new Vector3f(xSize, yOff, ySize), new Vector3f(xSize, yOff, 0),
				EnumFacing.UP, GRAY);
		float[] color = new float[4];
		color[3] = 1;
		for (int i = 0;i<3;i++) {
			color[i] = ((this.color>>(8*(2-i)))&255)/255F*(.5F+out/30F);
		}
		float val;
		if (horizontal) {
			val = (out/15F)*(length-.0625F);
		} else {
			val = (1-out/15F)*(length-.0625F);
		}
		PanelUtils.addColoredBox(color, GRAY, null, new Vector3f(horizontal?val:0, 0, horizontal?0:val),
				new Vector3f(.0625F, getHeight(), .0625F), ret, false);
		return ret;
	}

	@Nonnull
	@Override
	public PanelComponent copyOf() {
		Slider ret = new Slider(length, color, horizontal, rsId, rsChannel);
		ret.out = out;
		ret.setX(x);
		ret.setY(y);
		ret.panelHeight = panelHeight;
		return ret;
	}

	@Nonnull
	@Override
	public AxisAlignedBB getBlockRelativeAABB() {
		if (aabb==null) {
			aabb = new AxisAlignedBB(x, 0, y, x+(horizontal?length:WIDTH), getHeight(), y+(horizontal?WIDTH:length));
		}
		return aabb;
	}

	@Override
	public boolean interactWith(Vec3d hitRelative, TileEntityPanel tile, EntityPlayerMP player) {
		double pos = horizontal?hitRelative.xCoord:(length-hitRelative.zCoord);
		byte newLevel = (byte)(Math.min(pos*16/length, 15));
		if (newLevel!=out) {
			for (BiConsumer<Integer, Byte> output:outputs) {
				output.accept((int)rsChannel, newLevel);
			}
			out = newLevel;
			tile.markDirty();
			tile.triggerRenderUpdate();
			return true;
		}
		return false;
	}

	@Override
	public void registerRSOutput(int id, @Nonnull BiConsumer<Integer, Byte> out) {
		if (id==rsId) {
			outputs.add(out);
			out.accept((int)rsChannel, this.out);
		}
	}

	@Override
	public void unregisterRSOutput(int id, @Nonnull BiConsumer<Integer, Byte> out) {
		if (id==rsId) {
			outputs.remove(out);
		}
	}

	@Override
	public void update(TileEntityPanel tile) {

	}

	@Override
	public float getHeight() {
		return .0625F/2;
	}

	@Override
	public void renderInGUI(GuiPanelCreator gui) {
		renderInGUIDefault(gui, GRAY_INT);
		double middleX = (getX()+(horizontal?length:.0625)/2);
		double middleY = (getY()+(horizontal?.0625:length)/2);
		int left = gui.getX0()+(int) ((middleX-.0625/2)*gui.panelSize);
		int right = gui.getX0()+(int) ((middleX+.0625/2)*gui.panelSize);
		int top = gui.getY0()+(int) ((middleY-.0625/2)*gui.panelSize);
		int bottom = gui.getY0()+(int) ((middleY+.0625/2)*gui.panelSize);
		Gui.drawRect(left, top, right, bottom, 0xff000000|color);
	}

	@Override
	public void invalidate(TileEntityPanel te) {
		for (BiConsumer<Integer, Byte> out:outputs) {
			out.accept((int)rsChannel, (byte) 0);
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
		return rsId == slider.rsId;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (length != +0.0f ? Float.floatToIntBits(length) : 0);
		result = 31 * result + color;
		result = 31 * result + (horizontal ? 1 : 0);
		result = 31 * result + (int) out;
		result = 31 * result + (int) rsChannel;
		result = 31 * result + rsId;
		return result;
	}

	@Override
	public void applyConfigOption(ConfigType type, int id, NBTBase value) {
		switch (type) {
		case BOOL:
			horizontal = ((NBTTagByte)value).getByte()!=0;
			break;
		case RS_CHANNEL:
			rsChannel = ((NBTTagByte)value).getByte();
			break;
		case INT:
			rsId = ((NBTTagInt)value).getInt();
			break;
		case FLOAT:
			if (id<3) {
				color = PanelUtils.setColor(color, id, value);
			} else {
				length = scaleToRangePercent(((NBTTagFloat)value).getFloat(), .125F, 1);
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
			return I18n.format(IndustrialWires.MODID+".tooltip.horizontal");
		case RS_CHANNEL:
		case INT:
			return null;
		case FLOAT:
			return I18n.format(IndustrialWires.MODID+".desc."+(id==0?"red":(id==1?"green":id==2?"blue":"length")));
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
			return I18n.format(IndustrialWires.MODID+".desc.rschannel_info");
		case INT:
			return I18n.format(IndustrialWires.MODID+".desc.rsid_info");
		case FLOAT:
			return null;
		default:
			return "INVALID?";
		}
	}

	@Override
	public RSChannelConfig[] getRSChannelOptions() {
		return new RSChannelConfig[] {
				new RSChannelConfig("channel", 0, 0, rsChannel)
		};
	}

	@Override
	public IntConfig[] getIntegerOptions() {
		return new IntConfig[] {
				new IntConfig("rsId", 0, 50, rsId, 2, false)
		};
	}

	@Override
	public BoolConfig[] getBooleanOptions() {
		return new BoolConfig[] {
				new BoolConfig("horizontal", 0, 70, horizontal)
		};
	}

	@Override
	public FloatConfig[] getFloatOptions() {
		float[] color = PanelUtils.getFloatColor(true, this.color);
		int x = 70;
		int yOffset = 10;
		return new FloatConfig[]{
				new FloatConfig("red", x, yOffset+20, color[0], 60),
				new FloatConfig("green", x, yOffset+40, color[1], 60),
				new FloatConfig("blue", x, yOffset+60, color[2], 60),
				new FloatConfig("length", x, yOffset, (length-.125F)/(1-.125F), 60)
		};
	}

	@Override
	public int getColor() {
		return color;
	}
}

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
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class IndicatorLight extends PanelComponent implements IConfigurableComponent {
	private int rsInputId;
	private byte rsInputChannel;
	private int colorA = 0xff00;
	private byte rsInput;
	public IndicatorLight() {
		super("indicator_light");
	}
	public IndicatorLight(int rsId, byte rsChannel, int color) {
		this();
		colorA = color;
		rsInputChannel = rsChannel;
		rsInputId = rsId;
	}

	@Override
	protected void writeCustomNBT(NBTTagCompound nbt, boolean toItem) {
		nbt.setInteger(RS_ID, rsInputId);
		nbt.setByte(RS_CHANNEL, rsInputChannel);
		nbt.setInteger(COLOR, colorA);
		if (!toItem) {
			nbt.setInteger("rsInput", rsInput);
		}
	}

	@Override
	protected void readCustomNBT(NBTTagCompound nbt) {
		rsInputId = nbt.getInteger(RS_ID);
		rsInputChannel = nbt.getByte(RS_CHANNEL);
		colorA = nbt.getInteger(COLOR);
		rsInput = nbt.getByte("rsInput");
	}

	private static final float size = .0625F;
	private static final float antiZOffset = .001F;
	@Override
	public List<RawQuad> getQuads() {
		float[] color = new float[4];
		color[3] = 1;
		for (int i = 0;i<3;i++) {
			color[i] = ((this.colorA>>(8*(2-i)))&255)/255F*(rsInput+15F)/30F;
		}
		List<RawQuad> ret = new ArrayList<>(1);
		PanelUtils.addColoredQuad(ret, new Vector3f(), new Vector3f(0, antiZOffset, size), new Vector3f(size, antiZOffset, size), new Vector3f(size, antiZOffset, 0), EnumFacing.UP, color);
		return ret;
	}

	@Nonnull
	@Override
	public PanelComponent copyOf() {
		IndicatorLight ret = new IndicatorLight(rsInputId, rsInputChannel, colorA);
		ret.rsInput = rsInput;
		ret.setX(x);
		ret.setY(y);
		ret.panelHeight = panelHeight;
		return ret;
	}

	@Nonnull
	@Override
	public AxisAlignedBB getBlockRelativeAABB() {
		if (aabb==null) {
			aabb = new AxisAlignedBB(x, 0, y, x+size, 0, y+size);
		}
		return aabb;
	}

	@Override
	public boolean interactWith(Vec3d hitRelative, TileEntityPanel tile) {
		return false;
	}

	@Override
	public void update(TileEntityPanel tile) {

	}
	private TileEntityPanel panel;
	private Consumer<byte[]> handler = (input)->{
		if (input[rsInputChannel]!=rsInput) {
			rsInput = input[rsInputChannel];
			panel.markDirty();
			panel.triggerRenderUpdate();
		}
	};
	@Nullable
	@Override
	public Consumer<byte[]> getRSInputHandler(int id, TileEntityPanel panel) {
		if (id==rsInputId) {
			this.panel = panel;
			return handler;
		}
		return null;
	}

	@Override
	public float getHeight() {
		return 0;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		IndicatorLight that = (IndicatorLight) o;

		if (colorA != that.colorA) return false;
		return rsInput == that.rsInput;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + colorA;
		result = 31 * result + (int) rsInput;
		return result;
	}

	@Override
	public void renderInGUI(GuiPanelCreator gui) {
		renderInGUIDefault(gui, colorA);
	}

	@Override
	public void applyConfigOption(ConfigType type, int id, NBTBase value) {
		switch (type) {
		case RS_CHANNEL:
			rsInputChannel = ((NBTTagByte)value).getByte();
			break;
		case INT:
			rsInputId = ((NBTTagInt)value).getInt();
			break;
		case FLOAT:
			colorA = PanelUtils.setColor(colorA, id, value);
			break;
		}
	}

	@Nullable
	@Override
	public String fomatConfigName(ConfigType type, int id) {
		switch (type) {
		case FLOAT:
			return I18n.format(IndustrialWires.MODID+".desc."+(id==0?"red":(id==1?"green":"blue")));
		case RS_CHANNEL:
		case INT:
		default:
			return null;
		}
	}

	@Nullable
	@Override
	public String fomatConfigDescription(ConfigType type, int id) {
		switch (type) {
		case FLOAT:
			return null;
		case RS_CHANNEL:
			return I18n.format(IndustrialWires.MODID+".desc.rschannel_info");
		case INT:
			return I18n.format(IndustrialWires.MODID+".desc.rsid_info");
		default:
			return null;
		}
	}

	@Override
	public RSChannelConfig[] getRSChannelOptions() {
		return new RSChannelConfig[] {
				new RSChannelConfig("channel", 0, 0, rsInputChannel)
		};
	}

	@Override
	public IntConfig[] getIntegerOptions() {
		return new IntConfig[] {
				new IntConfig("rsId", 0, 45, rsInputId, 2, false)
		};
	}

	@Override
	public FloatConfig[] getFloatOptions() {
		float[] color = PanelUtils.getFloatColor(true, this.colorA);
		int x = 70;
		int yOffset = 10;
		return new FloatConfig[]{
				new FloatConfig("red", x, yOffset, color[0], 60),
				new FloatConfig("green", x, yOffset+20, color[1], 60),
				new FloatConfig("blue", x, yOffset+40, color[2], 60)
		};
	}

	@Override
	public int getColor() {
		return colorA;
	}
}
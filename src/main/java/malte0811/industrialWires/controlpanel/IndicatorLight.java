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
import malte0811.industrialWires.client.RawQuad;
import malte0811.industrialWires.client.gui.GuiPanelCreator;
import malte0811.industrialWires.controlpanel.ControlPanelNetwork.RSChannel;
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
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static malte0811.industrialWires.util.NBTKeys.*;

public class IndicatorLight extends PanelComponent implements IConfigurableComponent {
	@Nonnull
	private RSChannel inputChannel = RSChannel.DEFAULT_CHANNEL;
	private int colorA = 0xff00;
	private byte rsInput;

	public IndicatorLight() {
		super("indicator_light");
	}

	public IndicatorLight(@Nonnull RSChannel input, int color) {
		this();
		colorA = color;
		inputChannel = input;
	}

	@Override
	protected void writeCustomNBT(NBTTagCompound nbt, boolean toItem) {
		nbt.setInteger(RS_ID, inputChannel.getController());
		nbt.setByte(RS_CHANNEL, inputChannel.getColor());
		nbt.setInteger(COLOR, colorA);
		if (!toItem) {
			nbt.setInteger("rsInput", rsInput);
		}
	}

	@Override
	protected void readCustomNBT(NBTTagCompound nbt) {
		int rsController = nbt.getInteger(RS_ID);
		byte rsColor = nbt.getByte(RS_CHANNEL);
		inputChannel = new RSChannel(rsController, rsColor);
		colorA = nbt.getInteger(COLOR);
		rsInput = nbt.getByte("rsInput");
	}

	private static final float size = .0625F;

	@Override
	@SideOnly(Side.CLIENT)
	public List<RawQuad> getQuads() {
		float[] color = new float[4];
		color[3] = 1;
		for (int i = 0; i < 3; i++) {
			color[i] = ((this.colorA >> (8 * (2 - i))) & 255) / 255F * (rsInput + 15F) / 30F;
		}
		List<RawQuad> ret = new ArrayList<>(1);
		PanelUtils.addColoredQuad(ret, new Vector3f(), new Vector3f(0, 0, size), new Vector3f(size, 0, size), new Vector3f(size, 0, 0), EnumFacing.UP, color);
		if (rsInput>0) {
			ret.get(ret.size()-1).light = 0xff0ff;
		}
		return ret;
	}

	@Nonnull
	@Override
	public PanelComponent copyOf() {
		IndicatorLight ret = new IndicatorLight(inputChannel, colorA);
		ret.rsInput = rsInput;
		ret.setX(x);
		ret.setY(y);
		ret.panelHeight = panelHeight;
		return ret;
	}

	@Nonnull
	@Override
	public AxisAlignedBB getBlockRelativeAABB() {
		if (aabb == null) {
			aabb = new AxisAlignedBB(x, 0, y, x + size, 0, y + size);
		}
		return aabb;
	}

	@Override
	public void interactWith(Vec3d hitRelative, EntityPlayerMP player) {
	}

	@Override
	public void update() {

	}

	private Consumer<ControlPanelNetwork.RSChannelState> handler = (state) -> {
		if (state.getStrength() != rsInput) {
			rsInput = state.getStrength();
			panel.markDirty();
			panel.triggerRenderUpdate();
		}
	};


	@Override
	public void setNetwork(ControlPanelNetwork net) {
		super.setNetwork(net);
		net.addListener(this, handler, inputChannel);
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
	@SideOnly(Side.CLIENT)
	public void renderInGUI(GuiPanelCreator gui) {
		renderInGUIDefault(gui, colorA);
	}

	@Override
	public void applyConfigOption(ConfigType type, int id, NBTBase value) {
		switch (type) {
			case RS_CHANNEL:
				if (id == 0) {
					inputChannel = inputChannel.withColor(value);
				}
				break;
			case INT:
				if (id == 0) {
					inputChannel = inputChannel.withController(value);
				}
				break;
		case FLOAT:
			colorA = PanelUtils.setColor(colorA, id, value);
			break;
		}
	}

	@Nullable
	@Override
	@SideOnly(Side.CLIENT)
	public String fomatConfigName(ConfigType type, int id) {
		switch (type) {
		case FLOAT:
			return I18n.format(IndustrialWires.MODID + ".desc." + (id == 0 ? "red" : (id == 1 ? "green" : "blue")));
		case RS_CHANNEL:
		case INT:
		default:
			return null;
		}
	}

	@Nullable
	@Override
	@SideOnly(Side.CLIENT)
	public String fomatConfigDescription(ConfigType type, int id) {
		switch (type) {
		case FLOAT:
			return null;
		case RS_CHANNEL:
			return I18n.format(IndustrialWires.MODID + ".desc.rschannel_info");
		case INT:
			return I18n.format(IndustrialWires.MODID + ".desc.rsid_info");
		default:
			return null;
		}
	}

	@Override
	public RSColorConfig[] getRSChannelOptions() {
		return new RSColorConfig[]{
				new RSColorConfig("channel", 0, 0, inputChannel.getColor())
		};
	}

	@Override
	public IntConfig[] getIntegerOptions() {
		return new IntConfig[]{
				new IntConfig("rsId", 0, 45, inputChannel.getController(), 2, false)
		};
	}

	@Override
	public FloatConfig[] getFloatOptions() {
		float[] color = PanelUtils.getFloatColor(true, this.colorA);
		int x = 70;
		int yOffset = 10;
		return new FloatConfig[]{
				new FloatConfig("red", x, yOffset, color[0], 60),
				new FloatConfig("green", x, yOffset + 20, color[1], 60),
				new FloatConfig("blue", x, yOffset + 40, color[2], 60)
		};
	}

	@Override
	public int getColor() {
		return colorA;
	}
}
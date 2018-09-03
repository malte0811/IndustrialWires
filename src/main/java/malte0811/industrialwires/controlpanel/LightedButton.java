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
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static malte0811.industrialwires.util.NBTKeys.*;
import static net.minecraftforge.fml.relauncher.Side.CLIENT;

public class LightedButton extends PanelComponent implements IConfigurableComponent {
	public int color = 0xFF0000;
	private boolean active;
	private boolean latching;
	@Nonnull
	private RSChannel outputChannel = RSChannel.DEFAULT_CHANNEL;
	private int ticksTillOff;

	LightedButton() {
		super("lighted_button");
	}

	public LightedButton(int color, boolean active, boolean latching, @Nonnull RSChannel out) {
		this();
		this.color = color;
		this.active = active;
		this.latching = latching;
		this.outputChannel = out;
	}

	@Override
	protected void writeCustomNBT(NBTTagCompound nbt, boolean toItem) {
		nbt.setInteger(COLOR, color);
		if (!toItem) {
			nbt.setInteger("timeout", ticksTillOff);
			nbt.setBoolean("active", active);
		}
		nbt.setBoolean(LATCHING, latching);
		nbt.setByte(RS_CHANNEL, outputChannel.getColor());
		nbt.setInteger(RS_ID, outputChannel.getController());
	}

	@Override
	protected void readCustomNBT(NBTTagCompound nbt) {
		color = nbt.getInteger(COLOR);
		ticksTillOff = nbt.getInteger("timeout");
		active = nbt.getBoolean("active");
		latching = nbt.getBoolean(LATCHING);
		byte rsOutputChannel = nbt.getByte(RS_CHANNEL);
		int rsOutputId = nbt.getInteger(RS_ID);
		this.outputChannel = new RSChannel(rsOutputId, rsOutputChannel);
	}

	private final static float size = .0625F;

	@Override
	@SideOnly(CLIENT)
	public List<RawQuad> getQuads() {
		float[] color = PanelUtils.getFloatColor(active, this.color);
		List<RawQuad> ret = new ArrayList<>(5);
		PanelUtils.addColoredBox(color, GRAY, null, new Vector3f(0, 0, 0), new Vector3f(size, size / 2, size), ret, false);
		if (active) {
			ret.get(0).light = 0xff0ff;
		}

		return ret;
	}

	@Override
	@Nonnull
	public PanelComponent copyOf() {
		LightedButton ret = new LightedButton(color, active, latching, outputChannel);
		ret.setX(x);
		ret.setY(y);
		ret.panelHeight = panelHeight;
		return ret;
	}

	@Nonnull
	@Override
	public AxisAlignedBB getBlockRelativeAABB() {
		if (aabb == null) {
			aabb = new AxisAlignedBB(x, 0, y, x + size, getHeight(), y + size);
		}
		return aabb;
	}

	@Override
	public void interactWith(Vec3d hitRel, EntityPlayerMP player) {
		if (!latching && active) {
			ticksTillOff = 10;
		} else {
			setOut(!active);
			if (!latching) {
				ticksTillOff = 10;
			}
		}
		panel.markDirty();
		panel.triggerRenderUpdate();
	}

	@Override
	public void update() {
		if (!latching && active) {
			ticksTillOff--;
			panel.markDirty();
			if (ticksTillOff <= 0) {
				setOut(false);
			}
		}
	}

	@Override
	public void setNetwork(ControlPanelNetwork net) {
		super.setNetwork(net);
		net.setOutputs(this, new RSChannelState(outputChannel, (byte) (active?15:0)));
	}

	@Override
	public float getHeight() {
		return size / 2;
	}

	@Override
	@SideOnly(CLIENT)
	public void renderInGUI(GuiPanelCreator gui) {
		renderInGUIDefault(gui, 0xff000000 | color);
	}

	private void setOut(boolean on) {
		active = on;
		panel.markDirty();
		panel.triggerRenderUpdate();
		network.setOutputs(this, new RSChannelState(outputChannel, (byte)(active?15:0)));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		LightedButton that = (LightedButton) o;

		if (color != that.color) return false;
		if (active != that.active) return false;
		return latching == that.latching;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + color;
		result = 31 * result + (active ? 1 : 0);
		result = 31 * result + (latching ? 1 : 0);
		return result;
	}

	@Override
	public void applyConfigOption(ConfigType type, int id, NBTBase value) {
		switch (type) {
		case BOOL:
			if (id == 0) {
				latching = ((NBTTagByte) value).getByte() != 0;
			}
			break;
		case RS_CHANNEL:
			if (id == 0) {
				outputChannel = outputChannel.withColor(value);
			}
			break;
		case INT:
			if (id == 0) {
				outputChannel = outputChannel.withController(value);
			}
			break;
		case FLOAT:
			color = PanelUtils.setColor(color, id, value);
			break;
		}
	}

	@Override
	@SideOnly(CLIENT)
	public String fomatConfigName(ConfigType type, int id) {
		switch (type) {
		case BOOL:
			return I18n.format(IndustrialWires.MODID + ".desc.latching");
		case RS_CHANNEL:
		case INT:
			return null;
		case FLOAT:
			return I18n.format(IndustrialWires.MODID + ".desc." + (id == 0 ? "red" : (id == 1 ? "green" : "blue")));
		default:
			return "INVALID";
		}
	}

	@Override
	@SideOnly(CLIENT)
	public String fomatConfigDescription(ConfigType type, int id) {
		switch (type) {
		case BOOL:
			return I18n.format(IndustrialWires.MODID + ".desc.latching_info");
		case RS_CHANNEL:
			return I18n.format(IndustrialWires.MODID + ".desc.rschannel_info");
		case INT:
			return I18n.format(IndustrialWires.MODID + ".desc.rsid_info");
		case FLOAT:
			return null;
		default:
			return "INVALID?";
		}
	}

	@Override
	public RSColorConfig[] getRSChannelOptions() {
		return new RSColorConfig[]{new RSColorConfig("channel", 0, 0, outputChannel.getColor())};
	}

	@Override
	public IntConfig[] getIntegerOptions() {
		return new IntConfig[]{new IntConfig("rsId", 0, 50,
				outputChannel.getController(), 2, false)};
	}

	@Override
	public BoolConfig[] getBooleanOptions() {
		return new BoolConfig[]{new BoolConfig("latching", 0, 70, latching)};
	}

	@Override
	public FloatConfig[] getFloatOptions() {
		float[] color = PanelUtils.getFloatColor(true, this.color);
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
		return color;
	}
}
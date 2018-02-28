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
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;
import java.util.List;

import static malte0811.industrialWires.util.NBTKeys.*;

public class CoveredToggleSwitch extends ToggleSwitch {
	private int color = 0xff0000;
	private SwitchState state = SwitchState.CLOSED;

	public CoveredToggleSwitch() {
		super("toggle_switch_covered");
		sizeY = .125F;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public List<RawQuad> getQuads() {
		float[] color = PanelUtils.getFloatColor(true, this.color);
		active = state.active;
		List<RawQuad> ret = super.getQuads();
		Matrix4 rot = null;
		if (state.open) {
			rot = new Matrix4();
			rot.rotate(-Math.PI * .4, 1, 0, 0);
		}
		PanelUtils.addColoredBox(color, color, null, new Vector3f(0, 0, 0), new Vector3f(sizeX, getHeight(), sizeY), ret,
				false, rot, true);
		ret.remove(ret.size() - 2);//remove front face
		ret.remove(ret.size() - 1);//remove front face
		return ret;
	}

	@Override
	public void interactWith(Vec3d hitRel, TileEntityPanel tile, EntityPlayerMP player) {
		if (player.isSneaking() && state == SwitchState.OPEN) {
			state = SwitchState.CLOSED;
		} else {
			state = state.next();
		}
		setOut(state.active, tile);
		tile.markDirty();
		tile.triggerRenderUpdate();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderInGUI(GuiPanelCreator gui) {
		super.renderInGUIDefault(gui, 0xff000000 | this.color);
		super.renderInGUI(gui);
	}


	@Override
	protected void writeCustomNBT(NBTTagCompound nbt, boolean toItem) {
		if (!toItem) {
			nbt.setInteger("state", state.ordinal());
		}
		nbt.setByte(RS_CHANNEL, rsOutputChannel);
		nbt.setInteger(RS_ID, rsOutputId);
		nbt.setInteger(COLOR, color);
	}

	@Override
	protected void readCustomNBT(NBTTagCompound nbt) {
		state = SwitchState.values()[nbt.getInteger("state")];
		color = nbt.getInteger(COLOR);
		rsOutputChannel = nbt.getByte(RS_CHANNEL);
		rsOutputId = nbt.getInteger(RS_ID);
	}

	@Nonnull
	@Override
	public PanelComponent copyOf() {
		CoveredToggleSwitch ret = new CoveredToggleSwitch();
		ret.color = color;
		ret.state = state;
		ret.rsOutputChannel = rsOutputChannel;
		ret.rsOutputId = rsOutputId;
		ret.active = active;
		ret.setX(getX());
		ret.setY(getY());
		ret.setPanelHeight(panelHeight);
		return ret;
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
	public void applyConfigOption(ConfigType type, int id, NBTBase value) {
		super.applyConfigOption(type, id, value);
		if (type == ConfigType.FLOAT) {
			color = PanelUtils.setColor(color, id, value);
		}
	}

	@Override
	public String fomatConfigName(ConfigType type, int id) {
		if (type == ConfigType.FLOAT) {
			return I18n.format(IndustrialWires.MODID + ".desc." + (id == 0 ? "red" : (id == 1 ? "green" : "blue")));
		}
		return super.fomatConfigName(type, id);
	}

	@Override
	public String fomatConfigDescription(ConfigType type, int id) {
		if (type == ConfigType.FLOAT) {
			return null;
		}
		return super.fomatConfigDescription(type, id);
	}

	@Override
	public int getColor() {
		return color;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		CoveredToggleSwitch that = (CoveredToggleSwitch) o;

		if (rsOutputId != that.rsOutputId) return false;
		if (rsOutputChannel != that.rsOutputChannel) return false;
		if (color != that.color) return false;
		return state == that.state;
	}


	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + rsOutputId;
		result = 31 * result + (int) rsOutputChannel;
		result = 31 * result + color;
		result = 31 * result + (state != null ? state.hashCode() : 0);
		return result;
	}

	private enum SwitchState {
		CLOSED(false, false),
		OPEN(false, true),
		ACTIVE(true, true);
		public boolean active;
		public boolean open;

		SwitchState(boolean active, boolean open) {
			this.open = open;
			this.active = active;
		}

		SwitchState next() {
			return values()[(ordinal() + 1) % values().length];
		}
	}
}

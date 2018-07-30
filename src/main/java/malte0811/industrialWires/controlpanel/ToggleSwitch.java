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
import malte0811.industrialWires.controlpanel.ControlPanelNetwork.RSChannelState;
import net.minecraft.client.gui.Gui;
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

import static malte0811.industrialWires.util.NBTKeys.RS_CHANNEL;
import static malte0811.industrialWires.util.NBTKeys.RS_ID;

public class ToggleSwitch extends PanelComponent implements IConfigurableComponent {
	protected boolean active;
	@Nonnull
	protected RSChannel outputChannel = RSChannel.INVALID_CHANNEL;

	public ToggleSwitch() {
		super("toggle_switch");
	}

	protected ToggleSwitch(String name) {
		super(name);
	}

	public ToggleSwitch(boolean active, @Nonnull RSChannel outputChannel) {
		this();
		this.active = active;
		this.outputChannel = outputChannel;
	}

	@Override
	protected void writeCustomNBT(NBTTagCompound nbt, boolean toItem) {
		if (!toItem) {
			nbt.setBoolean("active", active);
		}
		nbt.setByte(RS_CHANNEL, outputChannel.getColor());
		nbt.setInteger(RS_ID, outputChannel.getController());
	}

	@Override
	protected void readCustomNBT(NBTTagCompound nbt) {
		active = nbt.getBoolean("active");
		int rsController = nbt.getInteger(RS_ID);
		byte rsColor = nbt.getByte(RS_CHANNEL);
		outputChannel = new RSChannel(rsController, rsColor);
	}

	protected float sizeX = .0625F;
	protected float sizeY = 1.5F * sizeX;
	protected float rodRadius = sizeX * .25F;
	protected float rodLength = 3 / 32F;

	@Override
	@SideOnly(Side.CLIENT)
	public List<RawQuad> getQuads() {
		List<RawQuad> ret = new ArrayList<>();
		PanelUtils.addColoredQuad(ret, new Vector3f(sizeX, 0, (sizeY - sizeX) / 2),
				new Vector3f(0, 0, (sizeY - sizeX) / 2),
				new Vector3f(0, 0, (sizeY + sizeX) / 2),
				new Vector3f(sizeX, 0, (sizeY + sizeX) / 2), EnumFacing.UP, GRAY);
		Matrix4 rot = new Matrix4();
		rot.translate((sizeX) / 2, -.01F, sizeY / 2);
		rot.rotate(Math.PI * 1 / 16 * (active ? -1 : 1), 1, 0, 0);
		PanelUtils.addColoredBox(GRAY, GRAY, null, new Vector3f(-rodRadius, 0, -rodRadius), new Vector3f(2 * rodRadius, rodLength, 2 * rodRadius), ret,
				false, rot);
		return ret;
	}

	@Override
	@Nonnull
	public PanelComponent copyOf() {
		ToggleSwitch ret = new ToggleSwitch(active, outputChannel);
		ret.setX(x);
		ret.setY(y);
		ret.panelHeight = panelHeight;
		return ret;
	}

	@Nonnull
	@Override
	public AxisAlignedBB getBlockRelativeAABB() {
		if (aabb == null) {
			aabb = new AxisAlignedBB(x, 0, y, x + sizeX, getHeight(), y + sizeY);
		}
		return aabb;
	}

	@Override
	public void interactWith(Vec3d hitRel, EntityPlayerMP player) {
		setOut(!active);
	}

	@Override
	public void update() {

	}

	@Override
	public float getHeight() {
		return .0625F * 3 / 2;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderInGUI(GuiPanelCreator gui) {
		AxisAlignedBB aabb = getBlockRelativeAABB();
		double zOffset = (aabb.maxZ - aabb.minZ - sizeX) / 2;
		int left = (int) (gui.getX0() + aabb.minX * gui.panelSize);
		int top = (int) Math.ceil(gui.getY0() + (aabb.minZ + zOffset) * gui.panelSize);
		int right = (int) (gui.getX0() + aabb.maxX * gui.panelSize);
		int bottom = (int) Math.floor(gui.getY0() + (aabb.maxZ - zOffset) * gui.panelSize);
		Gui.drawRect(left, top, right, bottom, GRAY_INT);
		double xOffset = (aabb.maxX - aabb.minX - rodRadius) / 2;
		left = (int) (gui.getX0() + (aabb.minX + xOffset) * gui.panelSize);
		top = (int) Math.floor(gui.getY0() + (aabb.minZ + aabb.maxZ) / 2 * gui.panelSize);
		right = (int) (gui.getX0() + (aabb.maxX - xOffset) * gui.panelSize);
		bottom = (int) Math.ceil(gui.getY0() + aabb.maxZ * gui.panelSize);
		Gui.drawRect(left, top, right, bottom, GRAY_INT + 0x101010);

	}

	protected void setOut(boolean on) {
		active = on;
		panel.markDirty();
		panel.triggerRenderUpdate();
		network.setOutputs(this, new RSChannelState(outputChannel, (byte) (active ? 15 : 0)));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		ToggleSwitch that = (ToggleSwitch) o;

		if (active != that.active) return false;
		if (Float.compare(that.sizeX, sizeX) != 0) return false;
		if (Float.compare(that.sizeY, sizeY) != 0) return false;
		if (Float.compare(that.rodRadius, rodRadius) != 0) return false;
		if (Float.compare(that.rodLength, rodLength) != 0) return false;
		return outputChannel.equals(that.outputChannel);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (active ? 1 : 0);
		result = 31 * result + outputChannel.hashCode();
		result = 31 * result + (sizeX != +0.0f ? Float.floatToIntBits(sizeX) : 0);
		result = 31 * result + (sizeY != +0.0f ? Float.floatToIntBits(sizeY) : 0);
		result = 31 * result + (rodRadius != +0.0f ? Float.floatToIntBits(rodRadius) : 0);
		result = 31 * result + (rodLength != +0.0f ? Float.floatToIntBits(rodLength) : 0);
		return result;
	}

	@Override
	public void applyConfigOption(ConfigType type, int id, NBTBase value) {
		switch (type) {
		case RS_CHANNEL:
			outputChannel = outputChannel.withColor(value);
			break;
		case INT:
			outputChannel = outputChannel.withController(value);
			break;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String fomatConfigName(ConfigType type, int id) {
		switch (type) {
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
	@SideOnly(Side.CLIENT)
	public String fomatConfigDescription(ConfigType type, int id) {
		switch (type) {
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
		return new IntConfig[]{new IntConfig("rsId", 0, 50, outputChannel.getController(), 2, false)};
	}

	@Override
	public int getColor() {
		return 0xffffffff;
	}
}
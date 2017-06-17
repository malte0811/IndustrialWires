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

public class ToggleSwitch extends PanelComponent implements IConfigurableComponent {
	public boolean active;
	public int rsOutputId;
	public byte rsOutputChannel;

	public ToggleSwitch() {
		super("toggle_switch");
	}

	public ToggleSwitch(String name) {
		super(name);
	}

	public ToggleSwitch(boolean active, int rsOutputId, byte rsOutputChannel) {
		this();
		this.active = active;
		this.rsOutputChannel = rsOutputChannel;
		this.rsOutputId = rsOutputId;
	}

	@Override
	protected void writeCustomNBT(NBTTagCompound nbt, boolean toItem) {
		if (!toItem) {
			nbt.setBoolean("active", active);
		}
		nbt.setByte(RS_CHANNEL, rsOutputChannel);
		nbt.setInteger(RS_ID, rsOutputId);
	}

	@Override
	protected void readCustomNBT(NBTTagCompound nbt) {
		active = nbt.getBoolean("active");
		rsOutputChannel = nbt.getByte(RS_CHANNEL);
		rsOutputId = nbt.getInteger(RS_ID);
	}

	protected float sizeX = .0625F;
	protected float sizeY = 1.5F * sizeX;
	protected float rodRadius = sizeX * .25F;
	protected float rodLength = 3 / 32F;
	protected float yOffset = .0001F;

	@Override
	public List<RawQuad> getQuads() {
		List<RawQuad> ret = new ArrayList<>();
		PanelUtils.addColoredQuad(ret, new Vector3f(sizeX, yOffset, (sizeY - sizeX) / 2),
				new Vector3f(0, yOffset, (sizeY - sizeX) / 2),
				new Vector3f(0, yOffset, (sizeY + sizeX) / 2),
				new Vector3f(sizeX, yOffset, (sizeY + sizeX) / 2), EnumFacing.UP, GRAY);
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
		ToggleSwitch ret = new ToggleSwitch(active, rsOutputId, rsOutputChannel);
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
	public void interactWith(Vec3d hitRel, TileEntityPanel tile, EntityPlayerMP player) {
		setOut(!active, tile);
		tile.markDirty();
		tile.triggerRenderUpdate();
	}

	@Override
	public void update(TileEntityPanel tile) {

	}

	@Override
	public void registerRSOutput(int id, @Nonnull TriConsumer<Integer, Byte, PanelComponent> out) {
		if (id == rsOutputId) {
			super.registerRSOutput(id, out);
			out.accept((int) rsOutputChannel, (byte) (active ? 15 : 0), this);
		}
	}

	@Override
	public float getHeight() {
		return .0625F * 3 / 2;
	}

	@Override
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

	@Override
	public void invalidate(TileEntityPanel te) {
		setOut(false, te);
	}

	protected void setOut(boolean on, TileEntityPanel tile) {
		active = on;
		tile.markDirty();
		tile.triggerRenderUpdate();
		setOut(rsOutputChannel, active ? 15 : 0);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		ToggleSwitch that = (ToggleSwitch) o;

		if (active != that.active) return false;
		if (rsOutputId != that.rsOutputId) return false;
		return rsOutputChannel == that.rsOutputChannel;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (active ? 1 : 0);
		result = 31 * result + rsOutputId;
		result = 31 * result + (int) rsOutputChannel;
		return result;
	}

	@Override
	public void applyConfigOption(ConfigType type, int id, NBTBase value) {
		switch (type) {
		case RS_CHANNEL:
			if (id == 0) {
				rsOutputChannel = ((NBTTagByte) value).getByte();
			}
			break;
		case INT:
			if (id == 0) {
				rsOutputId = ((NBTTagInt) value).getInt();
			}
			break;
		}
	}

	@Override
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
	public RSChannelConfig[] getRSChannelOptions() {
		return new RSChannelConfig[]{new RSChannelConfig("channel", 0, 0, (byte) rsOutputChannel)};
	}

	@Override
	public IntConfig[] getIntegerOptions() {
		return new IntConfig[]{new IntConfig("rsId", 0, 50, rsOutputId, 2, false)};
	}

	@Override
	public int getColor() {
		return 0xffffffff;
	}
}
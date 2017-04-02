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

package malte0811.industrialWires.blocks.controlpanel;

import malte0811.industrialWires.client.RawQuad;
import malte0811.industrialWires.client.panelmodel.PanelUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

public class LightedButton extends PanelComponent {
	public int color;
	public boolean active;
	public boolean latching;
	public int rsOutputId;
	public int rsOutputChannel;
	private AxisAlignedBB aabb;
	private int ticksTillOff;
	private Set<BiConsumer<Integer, Byte>> rsOut = new HashSet<>();
	public LightedButton() {
		super("lightedButton");
	}
	public LightedButton(int color, boolean active, boolean latching, int rsOutputId, int rsOutputChannel) {
		this();
		this.color = color;
		this.active = active;
		this.latching = latching;
		this.rsOutputChannel = rsOutputChannel;
		this.rsOutputId = rsOutputId;
	}

	@Override
	protected void writeCustomNBT(NBTTagCompound nbt) {
		nbt.setInteger("color", color);
		nbt.setInteger("timeout", ticksTillOff);
		nbt.setBoolean("active", active);
		nbt.setBoolean("latching", latching);
		nbt.setInteger("rsChannel", rsOutputChannel);
		nbt.setInteger("rsId", rsOutputId);
	}

	@Override
	protected void readCustomNBT(NBTTagCompound nbt) {
		color = nbt.getInteger("color");
		ticksTillOff = nbt.getInteger("timeout");
		active = nbt.getBoolean("active");
		latching = nbt.getBoolean("latching");
		rsOutputChannel = nbt.getInteger("rsChannel");
		rsOutputId = nbt.getInteger("rsId");
	}
	private final static float[] sideColor = {.8F, .8F, .8F};
	private final static float size = .0625F;
	@Override
	public List<RawQuad> getQuads() {
		float[] color = new float[4];
		color[3] = 1;
		for (int i = 0;i<3;i++) {
			color[i] = ((this.color>>(8*(2-i)))&255)/255F*(active?1:.5F);
		}
		List<RawQuad> ret = new ArrayList<>(5);
		PanelUtils.addColoredBox(color, sideColor, null, new Vector3f(0, 0, 0), new Vector3f(size, size/2, size), ret, false);
		return ret;
	}

	@Override
	@Nonnull
	public PanelComponent copyOf() {
		LightedButton ret = new LightedButton(color, active, latching, rsOutputId, rsOutputChannel);
		ret.setX(x);
		ret.setY(y);
		ret.panelHeight = panelHeight;
		return ret;
	}

	@Override
	public AxisAlignedBB getBlockRelativeAABB() {
		if (aabb==null) {
			aabb = new AxisAlignedBB(x, 0, y, x+size, size/2, y+size);
		}
		return aabb;
	}

	@Override
	public boolean interactWith(Vec3d hitRel, TileEntityPanel tile) {
		if (!latching&&active) {
			return false;
		}
		setOut(!active, tile);
		if (!latching) {
			ticksTillOff = 10;
		}
		tile.markDirty();
		tile.triggerRenderUpdate();
		return true;
	}

	@Override
	public void update(TileEntityPanel tile) {
		if (!latching&&ticksTillOff>0) {
			ticksTillOff--;
			tile.markDirty();
			if (ticksTillOff==0) {
				setOut(false, tile);
			}
		}
	}

	@Override
	public void registerRSOutput(int id, @Nonnull BiConsumer<Integer, Byte> out) {
		if (id==rsOutputId) {
			rsOut.add(out);
			out.accept(rsOutputChannel, (byte) (active?15:0));
		}
	}

	@Override
	public void unregisterRSOutput(int id, @Nonnull BiConsumer<Integer, Byte> out) {
		if (id==rsOutputId) {
			rsOut.remove(out);
		}
	}

	private void setOut(boolean on, TileEntityPanel tile) {
		active = on;
		tile.markDirty();
		tile.triggerRenderUpdate();
		for (BiConsumer<Integer, Byte> rs:rsOut) {
			rs.accept(rsOutputChannel, (byte)(active?15:0));
		}
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
}

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

package malte0811.industrialWires.blocks.wire;

public final class DummyTEs {
	public static class TinDummy extends TileEntityIC2ConnectorTin {
		@Override
		public void update() {
			TileEntityIC2ConnectorTin newTe = getRaw();
			newTe.bufferToNet = bufferToNet;
			newTe.bufferToMachine = bufferToMachine;
			newTe.maxToNet = maxToNet;
			newTe.maxToMachine = maxToMachine;
			newTe.relay = relay;
			newTe.facing = facing;
			world.setTileEntity(pos, newTe);
		}
		protected TileEntityIC2ConnectorTin getRaw() {
			return new TileEntityIC2ConnectorTin();
		}
	}
	public static class CopperDummy extends TinDummy {
		@Override
		protected TileEntityIC2ConnectorTin getRaw() {
			return new TileEntityIC2ConnectorCopper();
		}
	}
	public static class GoldDummy extends TinDummy {
		@Override
		protected TileEntityIC2ConnectorTin getRaw() {
			return new TileEntityIC2ConnectorGold();
		}
	}
	public static class HVDummy extends TinDummy {
		@Override
		protected TileEntityIC2ConnectorTin getRaw() {
			return new TileEntityIC2ConnectorHV();
		}
	}
	public static class GlassDummy extends TinDummy {
		@Override
		protected TileEntityIC2ConnectorTin getRaw() {
			return new TileEntityIC2ConnectorGlass();
		}
	}
}

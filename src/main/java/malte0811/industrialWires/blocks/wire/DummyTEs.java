package malte0811.industrialWires.blocks.wire;

public final class DummyTEs {
	public static class TinDummy extends TileEntityIC2ConnectorTin {
		@Override
		public void update() {
			TileEntityIC2ConnectorTin newTe = getRaw();
			newTe.inBuffer = inBuffer;
			newTe.outBuffer = outBuffer;
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

package malte0811.industrialWires.blocks.controlpanel;

import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.api.energy.wires.redstone.IRedstoneConnector;
import blusunrize.immersiveengineering.api.energy.wires.redstone.RedstoneWireNetwork;
import malte0811.industrialWires.blocks.TileEntityIWBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

//TODO implement IRedstoneConnector once there is a Maven build with it
public class TileEntityRSPanelConn extends TileEntityIWBase implements IRedstoneConnector, ITickable {
	private byte[] out = new byte[16];
	private boolean dirty = true;
	@Nonnull
	private RedstoneWireNetwork network = new RedstoneWireNetwork().add(this);
	private boolean hasConn = false;//TODO write to NBT
	{
		for (int i = 0;i<16;i++) {
			out[i] = -1;
		}
	}
	private boolean loaded = false;

	@Override
	public void update() {
		if(hasWorldObj() && !worldObj.isRemote && !loaded) {
			loaded = true;
			// completely reload the network
			network.removeFromNetwork(null);
		}
	}
	@Override
	public void writeNBT(NBTTagCompound out, boolean updatePacket) {
		out.setByteArray("out", this.out);
	}

	@Override
	public void readNBT(NBTTagCompound in, boolean updatePacket) {
		out = in.getByteArray("out");
	}
	// <0 means don't care
	public void updateInternalRSValues(byte[] output) {
		out = output;
		dirty = true;

	}
	public void flushRS() {
		if (dirty) {
			network.updateValues();
		}
	}
	public byte[] getInput() {
		return network.channelValues;
	}
	public byte[] getCachedOutput() {
		return out;
	}

	@Override
	public void setNetwork(@Nonnull RedstoneWireNetwork net) {
		network = net;
	}

	@Nonnull
	@Override
	public RedstoneWireNetwork getNetwork() {
		return network;
	}

	@Override
	public void onChange() {

	}

	@Override
	public void updateInput(byte[] currIn) {
		for (int i = 0;i<16;i++) {
			currIn[i] = (byte) Math.max(currIn[i], out[i]);
		}
	}

	@Override
	public boolean canConnect() {
		return !hasConn;
	}

	@Override
	public boolean isEnergyOutput() {
		return false;
	}

	@Override
	public int outputEnergy(int i, boolean b, int i1) {
		return 0;
	}

	@Override
	public BlockPos getConnectionMaster(@Nullable WireType wire, TargetingInfo target) {
		return pos;
	}

	@Override
	public boolean canConnectCable(WireType wire, TargetingInfo targetingInfo) {
		return wire==WireType.REDSTONE&&!hasConn;
	}

	@Override
	public void connectCable(WireType wireType, TargetingInfo targetingInfo, IImmersiveConnectable other) {
		hasConn = true;
		if (other instanceof IRedstoneConnector&&((IRedstoneConnector) other).getNetwork()!=network) {
			network.mergeNetwork(((IRedstoneConnector) other).getNetwork());
		}
	}

	@Override
	public WireType getCableLimiter(TargetingInfo targetingInfo) {
		return hasConn?WireType.REDSTONE:null;
	}

	@Override
	public boolean allowEnergyToPass(ImmersiveNetHandler.Connection connection) {
		return false;
	}

	@Override
	public void onEnergyPassthrough(int i) {}

	@Override
	public void removeCable(ImmersiveNetHandler.Connection connection) {
		hasConn = false;
		network.removeFromNetwork(this);
	}

	@Override
	public Vec3d getRaytraceOffset(IImmersiveConnectable other) {
		return new Vec3d(.5, .5, .5);//TODO better values
	}

	@Override
	public Vec3d getConnectionOffset(ImmersiveNetHandler.Connection connection) {
		return new Vec3d(.5, .5, .5);//TODO better values
	}
}

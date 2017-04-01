package malte0811.industrialWires.blocks.controlpanel;

import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.api.energy.wires.redstone.IRedstoneConnector;
import blusunrize.immersiveengineering.api.energy.wires.redstone.RedstoneWireNetwork;
import malte0811.industrialWires.util.MiscUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class TileEntityRSPanelConn extends TileEntityImmersiveConnectable implements IRedstoneConnector, ITickable {
	private byte[] out = new byte[16];
	private boolean dirty = true;
	private byte[] oldInput = new byte[16];
	private Set<Consumer<byte[]>> changeListeners = new HashSet<>();
	@Nonnull
	private RedstoneWireNetwork network = new RedstoneWireNetwork().add(this);
	private boolean hasConn = false;
	private int id;
	{
		for (int i = 0;i<16;i++) {
			oldInput[i] = -1;
		}
	}
	private boolean loaded = false;

	@Override
	public void update() {
		if(hasWorldObj() && !worldObj.isRemote) {
			if (!loaded) {
				loaded = true;
				// completely reload the network
				network.removeFromNetwork(null);
				List<BlockPos> parts = MiscUtils.discoverPanelParts(worldObj, pos);
				for (BlockPos bp:parts) {
					TileEntity te = worldObj.getTileEntity(bp);
					if (te instanceof TileEntityPanel) {
						requestRSConn(((TileEntityPanel) te));
					}
				}
			}
			if (dirty) {
				network.updateValues();
				dirty = false;
			}
		}
	}
	@Override
	public void writeCustomNBT(NBTTagCompound out, boolean updatePacket) {
		super.writeCustomNBT(out, updatePacket);
		out.setByteArray("out", this.out);
		out.setBoolean("hasConn", hasConn);
		out.setInteger("rsId", id);
	}

	@Override
	public void readCustomNBT(NBTTagCompound in, boolean updatePacket) {
		super.readCustomNBT(in, updatePacket);
		out = in.getByteArray("out");
		hasConn = in.getBoolean("hasConn");
		id = in.getInteger("rsId");
	}

	public void requestRSConn(TileEntityPanel panel) {
		PropertyComponents.PanelRenderProperties p = panel.getComponents();
		for (PanelComponent pc:p) {
			Consumer<byte[]> listener = pc.getRSInputHandler(id, panel);
			if (listener!=null) {
				changeListeners.add(listener);
			}
			pc.registerRSOutput(id, (channel, value)->{
				if (value!=out[channel]) {
					dirty = true;
					out[channel] = value;
				}
			});
		}
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
		if (!Arrays.equals(oldInput, network.channelValues)) {
			oldInput = Arrays.copyOf(network.channelValues, 16);
			for (Consumer<byte[]> c:changeListeners) {
				c.accept(oldInput);
			}
		}
	}

	@Override
	public void updateInput(byte[] currIn) {
		for (int i = 0;i<16;i++) {
			currIn[i] = (byte) Math.max(currIn[i], out[i]);
		}
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
		return WireType.REDSTONE;
	}

	@Override
	public boolean allowEnergyToPass(ImmersiveNetHandler.Connection connection) {
		return false;
	}

	@Override
	public void removeCable(ImmersiveNetHandler.Connection connection) {
		hasConn = false;
		network.removeFromNetwork(this);
		this.markDirty();
		if(worldObj != null) {
			IBlockState state = worldObj.getBlockState(pos);
			worldObj.notifyBlockUpdate(pos, state,state, 3);
		}
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

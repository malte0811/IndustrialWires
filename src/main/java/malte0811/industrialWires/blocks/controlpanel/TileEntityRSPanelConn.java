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

import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.api.energy.wires.redstone.IRedstoneConnector;
import blusunrize.immersiveengineering.api.energy.wires.redstone.RedstoneWireNetwork;
import malte0811.industrialWires.blocks.INetGUI;
import malte0811.industrialWires.util.MiscUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TileEntityRSPanelConn extends TileEntityImmersiveConnectable implements IRedstoneConnector, ITickable, INetGUI {
	private byte[] out = new byte[16];
	private boolean dirty = true;
	private byte[] oldInput = new byte[16];
	private Set<Consumer<byte[]>> changeListeners = new HashSet<>();
	private Set<TileEntityPanel> connectedPanels = new HashSet<>();
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
						registerPanel(((TileEntityPanel) te));
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

	private BiConsumer<Integer, Byte> rsOut = (channel, value)->{
		if (value!=out[channel]) {
			dirty = true;
			out[channel] = value;
		}
	};

	public void registerPanel(TileEntityPanel panel) {
		PropertyComponents.PanelRenderProperties p = panel.getComponents();
		for (PanelComponent pc:p) {
			Consumer<byte[]> listener = pc.getRSInputHandler(id, panel);
			if (listener!=null) {
				changeListeners.add(listener);
			}
			pc.registerRSOutput(id, rsOut);
		}
		panel.registerRS(this);
		connectedPanels.add(panel);
	}

	public void unregisterPanel(TileEntityPanel panel, boolean remove) {
		PropertyComponents.PanelRenderProperties p = panel.getComponents();
		for (PanelComponent pc:p) {
			Consumer<byte[]> listener = pc.getRSInputHandler(id, panel);
			if (listener!=null) {
				changeListeners.remove(listener);
			}
			pc.unregisterRSOutput(id, rsOut);
		}
		panel.unregisterRS(this);
		if (remove) {
			connectedPanels.remove(panel);
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

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		for (TileEntityPanel panel:connectedPanels) {
			unregisterPanel(panel, false);
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		for (TileEntityPanel panel:connectedPanels) {
			unregisterPanel(panel, false);
		}
	}

	@Override
	public void onChange(NBTTagCompound nbt, EntityPlayer p) {
		if (nbt.hasKey("rsId")) {
			List<BlockPos> parts = MiscUtils.discoverPanelParts(worldObj, pos);
			List<TileEntityPanel> tes = new ArrayList<>(parts.size());
			for (BlockPos bp:parts) {
				TileEntity te = worldObj.getTileEntity(bp);
				if (te instanceof TileEntityPanel) {
					tes.add((TileEntityPanel) te);
					unregisterPanel((TileEntityPanel) te, true);
				}
			}
			id = nbt.getInteger("rsId");
			out = new byte[16];
			for (TileEntityPanel panel:tes) {
				registerPanel(panel);
			}
			network.updateValues();
			IBlockState state = worldObj.getBlockState(pos);
			worldObj.notifyBlockUpdate(pos, state, state, 3);
			worldObj.addBlockEvent(pos, state.getBlock(), 255, 0);
		}
	}

	public int getRsId() {
		return id;
	}
}

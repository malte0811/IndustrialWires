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

package malte0811.industrialWires.blocks.controlpanel;

import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.api.energy.wires.redstone.IRedstoneConnector;
import blusunrize.immersiveengineering.api.energy.wires.redstone.RedstoneWireNetwork;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import malte0811.industrialWires.blocks.IBlockBoundsIW;
import malte0811.industrialWires.blocks.INetGUI;
import malte0811.industrialWires.controlpanel.ControlPanelNetwork;
import malte0811.industrialWires.controlpanel.ControlPanelNetwork.RSChannel;
import malte0811.industrialWires.controlpanel.ControlPanelNetwork.RSChannelState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.function.Consumer;

import static blusunrize.immersiveengineering.api.energy.wires.WireType.REDSTONE_CATEGORY;

public class TileEntityRSPanelConn extends TileEntityGeneralCP//TODO what parts of TEIIC do I need?
		implements IRedstoneConnector, INetGUI, IEBlockInterfaces.IDirectionalTile, IBlockBoundsIW,
		ITickable {
	private byte[] out = new byte[16];
	private boolean dirty = true;
	private byte[] oldInput = new byte[16];
	private final RSChannel[] channels = new RSChannel[16];
	private EnumFacing facing = EnumFacing.NORTH;
	@Nonnull
	private RedstoneWireNetwork wireNetwork = new RedstoneWireNetwork().add(this);
	private boolean hasConn = false;
	private int controller = 0;

	{
		for (int i = 0; i < 16; i++) {
			oldInput[i] = -1;
		}
		updateChannelsArray();
	}

	@Override
	public void update() {
		if (dirty) {
			wireNetwork.updateValues();
			dirty = false;
		}
	}

	private void updateChannelsArray() {
		for (byte i = 0;i<16;i++) {
			channels[i] = new RSChannel(controller, i);
		}
	}

	@Override
	public void writeNBT(NBTTagCompound nbt, boolean updatePacket) {
		nbt.setByteArray("out", this.out);
		nbt.setBoolean("hasConn", hasConn);
		nbt.setInteger("rsId", controller);
		nbt.setInteger("facing", facing.getIndex());
	}

	@Override
	public void readNBT(NBTTagCompound nbt, boolean updatePacket) {
		out = nbt.getByteArray("out");
		hasConn = nbt.getBoolean("hasConn");
		controller = nbt.getInteger("rsId");
		updateChannelsArray();
		facing = EnumFacing.VALUES[nbt.getInteger("facing")];
		aabb = null;
	}

	@Override
	public void setNetwork(@Nonnull RedstoneWireNetwork net) {
		wireNetwork = net;
	}

	@Nonnull
	@Override
	public RedstoneWireNetwork getNetwork() {
		return wireNetwork;
	}

	@Override
	public void onChange() {
		if (!Arrays.equals(oldInput, wireNetwork.channelValues)) {
			RSChannelState[] newStates = new RSChannelState[16];
			for (byte i = 0; i < 16; i++) {
				if (wireNetwork.channelValues[i]>out[i]) {
					newStates[i] = new RSChannelState(channels[i], wireNetwork.channelValues[i]);
				} else {
					newStates[i] = new RSChannelState(channels[i], (byte) 0);
				}
			}
			panelNetwork.setOutputs(this, newStates);
			oldInput = Arrays.copyOf(wireNetwork.channelValues, 16);
		}
	}

	@Override
	public void updateInput(byte[] currIn) {
		byte[] oldIn = Arrays.copyOf(currIn, 16);
		for (int i = 0; i < 16; i++) {
			currIn[i] = (byte) Math.max(currIn[i], out[i]);
		}
	}

	@Override
	public boolean canConnect() {
		return true;
	}

	@Override
	public boolean isEnergyOutput() {
		return false;
	}

	@Override
	public int outputEnergy(int amount, boolean simulate, int energyType) {
		return 0;
	}

	@Override
	public BlockPos getConnectionMaster(@Nullable WireType wire, TargetingInfo target) {
		return pos;
	}

	@Override
	public boolean canConnectCable(WireType wire, TargetingInfo targetingInfo, Vec3i offset) {
		return REDSTONE_CATEGORY.equals(wire.getCategory()) && !hasConn;
	}

	@Override
	public void connectCable(WireType wireType, TargetingInfo targetingInfo, IImmersiveConnectable other) {
		hasConn = true;
		if (other instanceof IRedstoneConnector && ((IRedstoneConnector) other).getNetwork() != wireNetwork) {
			wireNetwork.mergeNetwork(((IRedstoneConnector) other).getNetwork());
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
		wireNetwork.removeFromNetwork(this);
		this.markDirty();
		if (world != null) {
			IBlockState state = world.getBlockState(pos);
			world.notifyBlockUpdate(pos, state, state, 3);
		}
	}

	@Override
	public Vec3d getConnectionOffset(ImmersiveNetHandler.Connection connection) {
		EnumFacing side = facing.getOpposite();
		double conRadius = connection.cableType.getRenderDiameter() / 2;
		return new Vec3d(.5 - conRadius * side.getFrontOffsetX(), .5 - conRadius * side.getFrontOffsetY(), .5 - conRadius * side.getFrontOffsetZ());
	}

	@Override
	public void setNetworkAndInit(ControlPanelNetwork newNet) {
		super.setNetworkAndInit(newNet);
		onChange();
		Consumer<RSChannelState> listener = state -> {
			if (out[state.getColor()] != state.getStrength()) {
				out[state.getColor()] = state.getStrength();
				dirty = true;
				Thread.dumpStack();
			}
		};
		panelNetwork.addListener(this, listener, channels);
	}

	@Override
	public void onChange(NBTTagCompound nbt, EntityPlayer p) {
		if (nbt.hasKey("rsId")) {
			panelNetwork.removeIOFor(this);
			setNetworkAndInit(panelNetwork);
		}
	}

	@Override
	public World getConnectorWorld() {
		return world;
	}

	public int getRsId() {
		return controller;
	}

	@Nonnull
	@Override
	public EnumFacing getFacing() {
		return facing;
	}

	@Override
	public void setFacing(@Nonnull EnumFacing facing) {
		this.facing = facing;
	}

	@Override
	public int getFacingLimitation() {
		return 0;
	}

	@Override
	public boolean mirrorFacingOnPlacement(@Nonnull EntityLivingBase placer) {
		return true;
	}

	@Override
	public boolean canHammerRotate(@Nonnull EnumFacing side, float hitX, float hitY, float hitZ, @Nonnull EntityLivingBase entity) {
		return false;
	}

	@Override
	public boolean canRotate(@Nonnull EnumFacing axis) {
		return false;
	}

	private AxisAlignedBB aabb;

	@Override
	public AxisAlignedBB getBoundingBox() {
		if (aabb == null) {
			double h = 9 / 16D;
			switch (facing) {
			case DOWN:
				aabb = new AxisAlignedBB(0, 0, 0, 1, h, 1);
				break;
			case UP:
				aabb = new AxisAlignedBB(0, 1 - h, 0, 1, 1, 1);
				break;
			case NORTH:
				aabb = new AxisAlignedBB(0, 0, 0, 1, 1, h);
				break;
			case SOUTH:
				aabb = new AxisAlignedBB(0, 0, 1 - h, 1, 1, 1);
				break;
			case WEST:
				aabb = new AxisAlignedBB(0, 0, 0, h, 1, 1);
				break;
			case EAST:
				aabb = new AxisAlignedBB(1 - h, 0, 0, 1, 1, 1);
				break;
			}
		}
		return aabb;
	}
}

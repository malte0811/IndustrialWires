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

package malte0811.industrialwires.blocks.controlpanel;

import malte0811.industrialwires.blocks.INetGUI;
import malte0811.industrialwires.controlpanel.ControlPanelNetwork;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;

import java.util.Arrays;
import java.util.function.Consumer;

public abstract class TileEntityRSPanel extends TileEntityGeneralCP implements INetGUI, ITickable {
	protected byte[] out = new byte[16];
	private boolean dirty = true;
	private byte[] currInput = new byte[16];
	private final ControlPanelNetwork.RSChannel[] channels = new ControlPanelNetwork.RSChannel[16];
	private int controller = 0;


	{
		for (int i = 0; i < 16; i++) {
			currInput[i] = -1;
		}
		updateChannelsArray();
	}

	private void updateChannelsArray() {
		for (byte i = 0;i<16;i++) {
			channels[i] = new ControlPanelNetwork.RSChannel(controller, i);
		}
	}

	@Override
	public void update() {
		if (dirty) {
			updateOutput();
			dirty = false;
		}
	}


	@Override
	public void writeNBT(NBTTagCompound nbt, boolean updatePacket) {
		nbt.setByteArray("out", this.out);
		nbt.setInteger("rsId", controller);
	}

	@Override
	public void readNBT(NBTTagCompound nbt, boolean updatePacket) {
		out = nbt.getByteArray("out");
		controller = nbt.getInteger("rsId");
		updateChannelsArray();
	}

	protected void markRSDirty() {
		dirty = true;
	}

	protected void onInputChanged(byte[] newIn) {
		if (!Arrays.equals(currInput, newIn)) {
			ControlPanelNetwork.RSChannelState[] newStates = new ControlPanelNetwork.RSChannelState[16];
			for (byte i = 0; i < 16; i++) {
				if (newIn[i]>out[i]) {
					newStates[i] = new ControlPanelNetwork.RSChannelState(channels[i], newIn[i]);
				} else {
					newStates[i] = new ControlPanelNetwork.RSChannelState(channels[i], (byte) 0);
				}
			}
			panelNetwork.setOutputs(this, newStates);
			currInput = Arrays.copyOf(newIn, 16);
		}
	}

	@Override
	public void setNetworkAndInit(ControlPanelNetwork newNet) {
		super.setNetworkAndInit(newNet);
		onInputChanged(currInput);
		Consumer<ControlPanelNetwork.RSChannelState> listener = state -> {
			if (out[state.getColor()] != state.getStrength()) {
				out[state.getColor()] = state.getStrength();
				dirty = true;
			}
		};
		panelNetwork.addListener(this, listener, channels);
	}

	@Override
	public void onChange(NBTTagCompound nbt, EntityPlayer p) {
		if (nbt.hasKey("rsId")) {
			controller = nbt.getInteger("rsId");
			markDirty();
			panelNetwork.removeIOFor(this);
			setNetworkAndInit(panelNetwork);
			IBlockState state = world.getBlockState(pos);
			world.notifyBlockUpdate(pos, state, state, 3);
		}
	}

	public int getRsId() {
		return controller;
	}


	protected abstract void updateOutput();

	protected abstract void updateInput();

	@Override
	public void onLoad() {
		super.onLoad();
		if (!world.isRemote) {
			updateInput();
			updateOutput();
		}
	}
}

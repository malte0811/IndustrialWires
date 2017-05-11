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
package malte0811.industrialWires.blocks;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public abstract class TileEntityIWBase extends TileEntity {
	protected static final String ENERGY_TAG = "energy";
	protected static final String BUFFER_TAG = "buffer";
	protected static final String DIR_TAG = "dir";
	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound nbt = super.getUpdateTag();
		writeNBT(nbt, true);
		return nbt;
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(pos, getBlockMetadata(), getUpdateTag());
	}
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		writeNBT(compound, false);
		return super.writeToNBT(compound);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		readNBT(compound, false);
		super.readFromNBT(compound);

	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		super.onDataPacket(net, pkt);
		readNBT(pkt.getNbtCompound(), true);
	}

	public abstract void writeNBT(NBTTagCompound out, boolean updatePacket);

	public abstract void readNBT(NBTTagCompound in, boolean updatePacket);
}

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

package malte0811.industrialWires.network;

import io.netty.buffer.ByteBuf;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.ISyncReceiver;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

//simplified&adapted version of blusunrize.immersiveengineering.common.util.network.MessageTileSync
public class MessageTileSyncIW implements IMessage {
	BlockPos pos;
	NBTTagCompound nbt;

	public MessageTileSyncIW(TileEntity tile, NBTTagCompound nbt) {
		this.pos = tile.getPos();
		this.nbt = nbt;
	}

	public MessageTileSyncIW() {
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
		this.nbt = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.pos.getX());
		buf.writeInt(this.pos.getY());
		buf.writeInt(this.pos.getZ());
		ByteBufUtils.writeTag(buf, this.nbt);
	}

	public static class HandlerClient implements IMessageHandler<MessageTileSyncIW, IMessage> {
		@Override
		public IMessage onMessage(MessageTileSyncIW message, MessageContext ctx) {
			Minecraft.getMinecraft().addScheduledTask(()-> {
				World world = IndustrialWires.proxy.getClientWorld();
				if (world != null) {
					TileEntity tile = world.getTileEntity(message.pos);
					if (tile instanceof ISyncReceiver) {
						((ISyncReceiver) tile).onSync(message.nbt);
					}
				}
			});
			return null;
		}
	}
}
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

package malte0811.industrialWires.network;

import io.netty.buffer.ByteBuf;
import malte0811.industrialWires.blocks.INetGUI;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageGUIInteract implements IMessage {
	private BlockPos pos;
	private NBTTagCompound data;

	public MessageGUIInteract(TileEntity tile, NBTTagCompound data) {
		pos = tile.getPos();
		this.data = data;
	}

	public MessageGUIInteract() {}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
		data = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.pos.getX());
		buf.writeInt(this.pos.getY());
		buf.writeInt(this.pos.getZ());
		ByteBufUtils.writeTag(buf, data);
	}

	public static class HandlerServer implements IMessageHandler<MessageGUIInteract, IMessage> {
		@Override
		public IMessage onMessage(MessageGUIInteract message, MessageContext ctx) {
			ctx.getServerHandler().playerEntity.getServerWorld().addScheduledTask(()->handle(message, ctx.getServerHandler().playerEntity));
			return null;
		}
		private void handle(MessageGUIInteract msg, EntityPlayerMP player) {
			if (player.getDistanceSqToCenter(msg.pos)<100) {//closer than 10 blocks TODO use player reach distance?
				TileEntity te = player.worldObj.getTileEntity(msg.pos);
				if (te instanceof INetGUI) {
					((INetGUI) te).onChange(msg.data, player);
					te.markDirty();
				}
			}
		}
	}
}
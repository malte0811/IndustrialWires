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
import malte0811.industrialWires.items.INetGUIItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageItemSync implements IMessage {
	private EnumHand hand;
	private NBTTagCompound data;

	public MessageItemSync(EnumHand h, NBTTagCompound data) {
		hand = h;
		this.data = data;
	}

	public MessageItemSync() {
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		hand = EnumHand.values()[buf.readInt()];
		data = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(hand.ordinal());
		ByteBufUtils.writeTag(buf, data);
	}

	public static class HandlerServer implements IMessageHandler<MessageItemSync, IMessage> {
		@Override
		public IMessage onMessage(MessageItemSync message, MessageContext ctx) {
			ctx.getServerHandler().player.getServerWorld()
					.addScheduledTask(()-> {
				EntityPlayer player = ctx.getServerHandler().player;
				ItemStack held = player.getHeldItem(message.hand);
				if (held.getItem() instanceof INetGUIItem) {
					ctx.getServerHandler().player.getServerWorld().addScheduledTask(() ->
							((INetGUIItem) held.getItem()).onChange(message.data, player, message.hand));
				}
			});
			return null;
		}
	}
}
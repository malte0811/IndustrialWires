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

import blusunrize.immersiveengineering.api.ApiUtils;
import io.netty.buffer.ByteBuf;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.controlpanel.IConfigurableComponent;
import malte0811.industrialWires.controlpanel.PanelComponent;
import malte0811.industrialWires.items.ItemPanelComponent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageComponentSync implements IMessage {
	public static final String TYPE = "type";
	public static final String ID = "cfgId";
	public static final String VALUE = "value";
	private EnumHand hand;
	private NBTTagCompound data;

	public MessageComponentSync(EnumHand h, NBTTagCompound data) {
		hand = h;
		this.data = data;
	}

	public MessageComponentSync() {}

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

	public static class HandlerServer implements IMessageHandler<MessageComponentSync, IMessage> {
		@Override
		public IMessage onMessage(MessageComponentSync message, MessageContext ctx) {
			ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> handle(message, ctx.getServerHandler().player));
			return null;
		}
		private void handle(MessageComponentSync msg, EntityPlayerMP player) {
			ItemStack held = player.getHeldItem(msg.hand);
			if (!held.isEmpty() && held.getItem() == IndustrialWires.panelComponent) {
				PanelComponent old = ItemPanelComponent.componentFromStack(held);
				if (old instanceof IConfigurableComponent) {
					NBTTagList changes = msg.data.getTagList("data", 10);
					IConfigurableComponent cmp = (IConfigurableComponent) old;
					for (int i = 0; i < changes.tagCount(); i++) {
						NBTTagCompound curr = changes.getCompoundTagAt(i);
						IConfigurableComponent.ConfigType type = IConfigurableComponent.ConfigType.values()[curr.getInteger(TYPE)];
						try {
							cmp.applyConfigOption(type, curr.getInteger(ID), curr.getTag(VALUE));
						} catch (Exception x) {
							x.printStackTrace();
						}
					}
					ItemStack newCmp = ApiUtils.copyStackWithAmount(ItemPanelComponent.stackFromComponent(old), held.getCount());
					player.setHeldItem(msg.hand, newCmp);
				}
			}
		}
	}
}
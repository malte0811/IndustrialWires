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
import malte0811.industrialWires.blocks.controlpanel.TileEntityPanel;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessagePanelInteract implements IMessage {
	private BlockPos pos;
	private int pcId;
	private Vec3d hitRelative;

	public MessagePanelInteract(TileEntityPanel tile, int id, Vec3d hit) {
		pos = tile.getPos();
		pcId = id;
		hitRelative = hit;
	}

	public MessagePanelInteract() {}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
		pcId = buf.readInt();
		hitRelative = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.pos.getX());
		buf.writeInt(this.pos.getY());
		buf.writeInt(this.pos.getZ());
		buf.writeInt(pcId);
		buf.writeDouble(hitRelative.xCoord).writeDouble(hitRelative.yCoord).writeDouble(hitRelative.zCoord);
	}

	public static class HandlerServer implements IMessageHandler<MessagePanelInteract, IMessage> {
		@Override
		public IMessage onMessage(MessagePanelInteract message, MessageContext ctx) {
			ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> handle(message, ctx.getServerHandler().player));
			return null;
		}
		private void handle(MessagePanelInteract msg, EntityPlayerMP player) {
			if (player.getDistanceSqToCenter(msg.pos)<100) {//closer than 10 blocks
				TileEntity te = player.world.getTileEntity(msg.pos);
				if (te instanceof TileEntityPanel) {
					((TileEntityPanel) te).interactServer(msg.hitRelative, msg.pcId, player);
				}
			}
		}
	}
}
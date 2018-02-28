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

package malte0811.industrialWires.util;

import malte0811.industrialWires.client.render.TileRenderMarx;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class CommandIWClient extends CommandBase {
	@Nonnull
	@Override
	public String getName() {
		return "ciw";
	}

	@Nonnull
	@Override
	public String getUsage(@Nonnull ICommandSender sender) {
		return "/ciw <triggermarxscreenshot>";
	}

	@Override
	public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
		if (args.length==0)
			throw new CommandException("1 parameter is required");
		switch (args[0].toLowerCase()) {
			//TODO more immersive way of doing this?
			case "triggermarxscreenshot":
				TileRenderMarx.screenShot = true;
				break;
		}
	}

	@Nonnull
	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		if (args.length==1) {
			return getListOfStringsMatchingLastWord(args, "triggermarxscreenshot");
		}
		return super.getTabCompletions(server, sender, args, targetPos);
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}
}

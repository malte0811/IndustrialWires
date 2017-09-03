package malte0811.industrialWires.util;

import malte0811.industrialWires.IWSaveData;
import malte0811.industrialWires.hv.MarxOreHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class CommandIW extends CommandBase {
	@Nonnull
	@Override
	public String getName() {
		return "iw";
	}

	@Nonnull
	@Override
	public String getUsage(@Nonnull ICommandSender sender) {
		return "/iw <getmarx|resetmarx|setmarx <value>>";
	}

	@Override
	public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
		if (args.length==0)
			throw new CommandException("1 parameter is required");
		switch (args[0].toLowerCase()) {
			case "getmarx":
				sender.sendMessage(new TextComponentString("Marx energy factor: "+ MarxOreHandler.modifier));
				break;
			case "resetmarx":
				MarxOreHandler.resetModifier();
				IWSaveData.INSTANCE.markDirty();
				sender.sendMessage(new TextComponentString("Marx energy factor reset was successful"));
				break;
			case "setmarx":
				if (args.length!=2)
					throw new CommandException("2 parameters are required for setmarx");
				MarxOreHandler.modifier = parseDouble(args[1], .9, 1.1);
				IWSaveData.INSTANCE.markDirty();
				sender.sendMessage(new TextComponentString("Successfully set Marx energy factor"));
				break;
		}
	}

	@Nonnull
	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		if (args.length==1) {
			return getListOfStringsMatchingLastWord(args, "getmarx", "setmarx", "resetmarx");
		}
		return super.getTabCompletions(server, sender, args, targetPos);
	}
}

package malte0811.industrialWires.compat;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.api.block.IBlock;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import malte0811.industrialWires.hv.MarxOreHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.function.BiPredicate;
import java.util.function.Supplier;

@ZenClass("mods.industrialwires.MarxGenerator")
public class CTMarxGenerator {
	@ZenMethod
	public static void addRecipe(IBlock in, double avgRelEnergy, double maxMain, IItemStack outMain, @Optional int smallLargeRatio, @Optional IItemStack outSmall) {
		IBlock properIn;
		if (in instanceof IItemStack)
			properIn = ((IItemStack) in).asBlock();
		else
			properIn = in;
		if (!(properIn instanceof IBlock)) {
			throw new IllegalArgumentException("What did you pass to MarxGenerator.addRecipe?"+properIn);
		}
		Supplier<ItemStack> out = ()->CraftTweakerMC.getItemStack(outMain);
		Supplier<ItemStack> supSmall = ()->CraftTweakerMC.getItemStack(outSmall);
		CraftTweakerAPI.apply(new Add((world, pos)-> CraftTweakerMC.getBlock(world, pos.getX(), pos.getY(), pos.getZ()).matches(properIn),
				new MarxOreHandler.OreInfo(avgRelEnergy, maxMain, out, supSmall, smallLargeRatio)));
	}

	private static class Add implements IAction {
		private final BiPredicate<World, BlockPos> isValid;
		private final MarxOreHandler.OreInfo output;

		public Add(BiPredicate<World, BlockPos> isValid, MarxOreHandler.OreInfo output) {
			this.isValid = isValid;
			this.output = output;
		}

		@Override
		public void apply() {
			MarxOreHandler.put(isValid, output);
		}

		@Override
		public String describe() {
			return "Adding Marx Generator Recipe for "+output.output.get();
		}
	}
}

package malte0811.industrialWires.compat;

import com.google.common.collect.ImmutableList;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.api.block.IBlock;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.oredict.IOreDictEntry;
import malte0811.industrialWires.hv.MarxOreHandler;
import malte0811.industrialWires.hv.MarxOreHandler.OreChecker;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.function.Supplier;

@ZenClass("mods.industrialwires.MarxGenerator")
public class CTMarxGenerator {
	@ZenMethod
	public static void addRecipe(IIngredient in, double avgRelEnergy, double maxMain, IItemStack outMain, @Optional int smallLargeRatio, @Optional IItemStack outSmall) {
		Supplier<ItemStack> out = () -> CraftTweakerMC.getItemStack(outMain);
		Supplier<ItemStack> supSmall = outSmall!=null?() -> CraftTweakerMC.getItemStack(outSmall):null;
		if (in instanceof IItemStack) {
			IBlock properIn = ((IItemStack) in).asBlock();
			if (properIn!=null) {
				CraftTweakerAPI.apply(new Add(new MarxOreHandler.OreInfo((world, pos) -> CraftTweakerMC.getBlock(world, pos.getX(), pos.getY(), pos.getZ()).matches(properIn),
						ImmutableList.of(CraftTweakerMC.getItemStack(in)), avgRelEnergy, maxMain, out, supSmall, smallLargeRatio)));
				return;
			}
		} else if (in instanceof IOreDictEntry) {
			String oreName = ((IOreDictEntry) in).getName();
			CraftTweakerAPI.apply(new Add(new MarxOreHandler.OreInfo(new OreChecker(oreName), OreDictionary.getOres(oreName),
					avgRelEnergy, maxMain, out, supSmall, smallLargeRatio)));
			return;
		}
		throw new IllegalArgumentException("Invalid parameter "+in);
	}

	private static class Add implements IAction {
		private final MarxOreHandler.OreInfo recipe;

		public Add(MarxOreHandler.OreInfo recipe) {
			this.recipe = recipe;
		}

		@Override
		public void apply() {
			MarxOreHandler.put(recipe);
		}

		@Override
		public String describe() {
			return "Adding Marx Generator Recipe for "+ recipe.output.get();
		}
	}
}

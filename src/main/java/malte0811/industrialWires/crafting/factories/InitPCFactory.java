package malte0811.industrialWires.crafting.factories;

import com.google.gson.JsonObject;
import malte0811.industrialWires.crafting.RecipeInitPC;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class InitPCFactory implements IRecipeFactory {
	@Override
	public IRecipe parse(JsonContext context, JsonObject json) {
		return new RecipeInitPC(ShapedOreRecipe.factory(context, json));
	}
}

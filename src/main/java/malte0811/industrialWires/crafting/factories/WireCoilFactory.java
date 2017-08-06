package malte0811.industrialWires.crafting.factories;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import malte0811.industrialWires.crafting.RecipeCoilLength;
import malte0811.industrialWires.crafting.RecipeInitPC;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class WireCoilFactory implements IRecipeFactory {
	@Override
	public RecipeCoilLength parse(JsonContext context, JsonObject json) {
		JsonObject coil = json.getAsJsonObject("coil");
		JsonObject cable = json.getAsJsonObject("cable");
		return new RecipeCoilLength(CraftingHelper.getItemStack(coil, context), CraftingHelper.getIngredient(cable, context));
	}
}

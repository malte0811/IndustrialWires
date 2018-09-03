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

package malte0811.industrialwires.crafting.factories;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import malte0811.industrialwires.crafting.RecipeCoilLength;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class WireCoilFactory implements IRecipeFactory {
	@Override
	public RecipeCoilLength parse(JsonContext context, JsonObject json) {
		JsonObject coil = json.getAsJsonObject("coil");
		JsonArray cables = json.getAsJsonArray("cables");
		List<Pair<Ingredient, Integer>> cablesList = new ArrayList<>(cables.size());
		for (JsonElement ele:cables) {
			JsonObject obj = ele.getAsJsonObject();
			int length = obj.get("length").getAsInt();
			Ingredient ingred = CraftingHelper.getIngredient(obj.getAsJsonObject("ingredient"), context);
			cablesList.add(new ImmutablePair<>(ingred, length));
		}
		return new RecipeCoilLength(CraftingHelper.getItemStack(coil, context), cablesList);
	}
}

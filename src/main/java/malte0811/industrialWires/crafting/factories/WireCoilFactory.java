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

package malte0811.industrialWires.crafting.factories;

import com.google.gson.JsonObject;
import malte0811.industrialWires.crafting.RecipeCoilLength;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;

public class WireCoilFactory implements IRecipeFactory {
	@Override
	public RecipeCoilLength parse(JsonContext context, JsonObject json) {
		JsonObject coil = json.getAsJsonObject("coil");
		JsonObject cable = json.getAsJsonObject("cable");
		return new RecipeCoilLength(CraftingHelper.getItemStack(coil, context), CraftingHelper.getIngredient(cable, context));
	}
}

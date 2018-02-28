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

package malte0811.industrialWires.compat;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.tool.ToolboxHandler;
import crafttweaker.CraftTweakerAPI;
import ic2.api.item.IBoxable;
import ic2.api.item.IC2Items;
import malte0811.industrialWires.hv.MarxOreHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Optional;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Consumer;

public class Compat {
	public static Consumer<MarxOreHandler.OreInfo> addMarx = (o)->{};
	public static Consumer<MarxOreHandler.OreInfo> removeMarx = (o)->{};

	public static void preInit() {
		callAllForClass(PreInit.class);
	}
	public static void init() {
		callAllForClass(Init.class);
	}

	private static void callAllForClass(Class c) {
		Method[] methods = c.getDeclaredMethods();
		for (Method m : methods) {
			if (m.getReturnType() == void.class && m.getParameterCount() == 0) {
				try {
					m.setAccessible(true);
					m.invoke(null);
				} catch (IllegalAccessException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static class PreInit {
		@Optional.Method(modid = "crafttweaker")
		private static void preInitCraftTweaker() {
			CraftTweakerAPI.registerClass(CTMarxGenerator.class);
		}
	}

	private static class Init {
		@Optional.Method(modid = "ic2")
		private static void initIC2() {
			Item tinnedFood = IC2Items.getItem("filled_tin_can").getItem();
			ItemStack emptyMug = IC2Items.getItem("mug", "empty");
			ToolboxHandler.addFoodType((s) -> s.getItem() == tinnedFood);
			ToolboxHandler.addFoodType((s) ->
					s.getItem() == emptyMug.getItem() && !ItemStack.areItemStacksEqual(emptyMug, ApiUtils.copyStackWithAmount(s, 1))
			);
			Item cable = IC2Items.getItem("cable", "type:copper,insulation:0").getItem();
			ToolboxHandler.addWiringType((s, w) -> s.getItem() == cable);
			ToolboxHandler.addToolType((s) -> {
				Item a = s.getItem();
				return a instanceof IBoxable && ((IBoxable) a).canBeStoredInToolbox(s);
			});
		}
	}
}
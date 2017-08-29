package malte0811.industrialWires.compat;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.tool.ToolboxHandler;
import crafttweaker.CraftTweakerAPI;
import ic2.api.item.IBoxable;
import ic2.api.item.IC2Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Optional;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Compat {

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
package malte0811.industrialWires;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.tool.ToolboxHandler;
import ic2.api.item.IBoxable;
import ic2.api.item.IC2Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ExtraIC2Compat {
	public static void addToolConmpat() {
		Item tinnedFood = IC2Items.getItem("filled_tin_can").getItem();
		ItemStack emptyMug = IC2Items.getItem("mug", "empty");
		ToolboxHandler.addFoodType((s)->s.getItem()==tinnedFood);
		ToolboxHandler.addFoodType((s)->
				s.getItem()==emptyMug.getItem()&&!ItemStack.areItemStacksEqual(emptyMug, ApiUtils.copyStackWithAmount(s, 1))
		);
		Item cable = IC2Items.getItem("cable", "type:copper,insulation:0").getItem();
		ToolboxHandler.addWiringType((s, w)->s.getItem()==cable);
		ToolboxHandler.addToolType((s)-> {
			Item a = s.getItem();
			return a instanceof IBoxable && ((IBoxable) a).canBeStoredInToolbox(s);
		});
	}
}

package malte0811.industrialWires.crafting;

import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.items.ItemIC2Coil;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

public class RecipeCoilLength implements IRecipe {
	public final ItemStack coil;
	public final ItemStack cable;
	final int maxLength;
	public RecipeCoilLength(int meta) {
		coil = new ItemStack(IndustrialWires.coil, 1, meta);
		cable = ItemIC2Coil.getUninsulatedCable(coil);
		maxLength = ItemIC2Coil.getMaxWireLength(coil);
	}

	@Override
	public boolean matches(InventoryCrafting inv, World worldIn) {
		int l = getLength(inv);
		return l>0;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		ItemStack ret = new ItemStack(IndustrialWires.coil, 1, coil.getItemDamage());
		ItemIC2Coil.setLength(ret, Math.min(maxLength, getLength(inv)));
		return ret;
	}

	@Override
	public int getRecipeSize() {
		return 0;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return null;
	}

	@Override
	public ItemStack[] getRemainingItems(InventoryCrafting inv) {
		ItemStack[] ret = new ItemStack[inv.getSizeInventory()];
		int length = Math.min(getLength(inv), maxLength);
		for (int i = 0;i<ret.length&&length>0;i++) {
			ItemStack curr = inv.getStackInSlot(i);
			if (OreDictionary.itemMatches(curr, coil, false)) {
				length-=ItemIC2Coil.getLength(curr);
				if (length<0) {
					ret[i] = new ItemStack(IndustrialWires.coil, 1);
					ItemIC2Coil.setLength(ret[i], -length);
				}
			} else if (OreDictionary.itemMatches(curr, cable, false)) {
				length--;
			}
		}
		return ret;
	}
	private int getLength(InventoryCrafting inv) {
		int cableLength = 0;
		for (int i = 0;i<inv.getSizeInventory();i++) {
			ItemStack curr = inv.getStackInSlot(i);
			if (OreDictionary.itemMatches(curr, coil, false)) {
				cableLength+=ItemIC2Coil.getLength(curr);
			} else if (OreDictionary.itemMatches(curr, cable, false)) {
				cableLength++;
			} else if (curr!=null) {
				return -1;
			}
		}
		return cableLength;
	}
}

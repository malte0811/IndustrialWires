/*
 * This file is part of Industrial Wires.
 * Copyright (C) 2016-2017 malte0811
 *
 * Industrial Wires is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Industrial Wires is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Industrial Wires.  If not, see <http://www.gnu.org/licenses/>.
 */

package malte0811.industrialWires.items;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.client.ClientProxy;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.controlpanel.IConfigurableComponent;
import malte0811.industrialWires.controlpanel.PanelComponent;
import malte0811.industrialWires.controlpanel.PanelUtils;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemPanelComponent extends Item implements INetGUIItem {
	public static final String[] types = {
			"lighted_button", "label", "indicator_light", "slider", "variac", "toggle_switch", "toggle_switch_covered",
			"lock", "panel_meter"
	};
	public static final String TYPE = "type";
	public static final String ID = "cfgId";
	public static final String VALUE = "value";

	public ItemPanelComponent() {
		setUnlocalizedName(IndustrialWires.MODID + ".panel_component");
		setHasSubtypes(true);
		this.setCreativeTab(IndustrialWires.creativeTab);
		setMaxStackSize(64);
		setRegistryName(new ResourceLocation(IndustrialWires.MODID, "panel_component"));
		IndustrialWires.items.add(this);
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
		if (GuiScreen.isShiftKeyDown()) {
			NBTTagCompound nbt = getTagCompound(stack);
			NBTTagCompound data = nbt.getCompoundTag("data");
			PanelUtils.addInfo(stack, list, data);
		} else {
			list.add(I18n.format("desc.immersiveengineering.info.holdShiftForInfo"));
		}
	}

	@Nonnull
	@SideOnly(Side.CLIENT)
	@Override
	public FontRenderer getFontRenderer(ItemStack stack) {
		return ClientProxy.itemFont;//TODO non-core-IE solution?
	}

	@Nonnull
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return this.getUnlocalizedName() + "." + types[stack.getMetadata()];
	}

	@Override
	public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> subItems) {
		if (tab==IndustrialWires.creativeTab) {
			for (int i = 0; i < types.length; i++) {
				subItems.add(new ItemStack(this, 1, i));
			}
		}
	}

	@Nullable
	public static PanelComponent componentFromStack(ItemStack stack) {
		if (stack.getItem()!=IndustrialWires.panelComponent) {
			return null;
		}
		NBTTagCompound loadFrom = getTagCompound(stack).getCompoundTag("data").copy();
		loadFrom.setString("type", types[stack.getMetadata()]);
		return PanelComponent.read(loadFrom);
	}

	@Nonnull
	public static ItemStack stackFromComponent(PanelComponent pc) {
		NBTTagCompound inner = new NBTTagCompound();
		pc.writeToNBT(inner, true);
		NBTTagCompound outer = new NBTTagCompound();
		outer.setTag("data", inner);
		int meta = getMetaFromPC(inner.getString("type"));
		removeIrrelevantTags(inner);
		if (meta >= 0) {
			ItemStack ret = new ItemStack(IndustrialWires.panelComponent, 1, meta);
			ret.setTagCompound(outer);
			return ret;
		}
		return ItemStack.EMPTY;
	}

	private static void removeIrrelevantTags(NBTTagCompound inner) {
		inner.removeTag("x");
		inner.removeTag("y");
		inner.removeTag("type");
		inner.removeTag("panelHeight");
	}

	private static int getMetaFromPC(String pc) {
		for (int i = 0; i < types.length; i++) {
			if (pc.equals(types[i])) {
				return i;
			}
		}
		return -1;
	}

	@Nonnull
	public static NBTTagCompound getTagCompound(ItemStack stack) {
		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		NBTTagCompound nbt = stack.getTagCompound();
		assert nbt != null;
		if (!nbt.hasKey("data")) {
			PanelComponent asCmp = PanelComponent.baseCreaters.get(types[stack.getMetadata()]).get();
			if (asCmp != null) {
				NBTTagCompound written = new NBTTagCompound();
				asCmp.writeToNBT(written, true);
				removeIrrelevantTags(written);
				nbt.setTag("data", written);
			}
		}
		return nbt;
	}

	@Override
	@Nonnull
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, @Nonnull EnumHand hand) {
		if (!worldIn.isRemote) {
			playerIn.openGui(IndustrialWires.MODID, 1, worldIn, 0, 0, hand == EnumHand.MAIN_HAND ? 1 : 0);
		}
		return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(hand));
	}

	@Override
	public void onChange(NBTTagCompound data, EntityPlayer player, EnumHand hand) {
		ItemStack held = player.getHeldItem(hand);
		if (!held.isEmpty() && held.getItem() == IndustrialWires.panelComponent) {
			PanelComponent old = ItemPanelComponent.componentFromStack(held);
			if (old instanceof IConfigurableComponent) {
				NBTTagList changes = data.getTagList("data", 10);
				IConfigurableComponent cmp = (IConfigurableComponent) old;
				for (int i = 0; i < changes.tagCount(); i++) {
					NBTTagCompound curr = changes.getCompoundTagAt(i);
					IConfigurableComponent.ConfigType type = IConfigurableComponent.ConfigType.values()[curr.getInteger(TYPE)];
					try {
						cmp.applyConfigOption(type, curr.getInteger(ID), curr.getTag(VALUE));
					} catch (Exception x) {
						x.printStackTrace();
					}
				}
				ItemStack newCmp = ApiUtils.copyStackWithAmount(ItemPanelComponent.stackFromComponent(old), held.getCount());
				player.setHeldItem(hand, newCmp);
			}
		}
	}
}
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

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.ClientProxy;
import blusunrize.immersiveengineering.client.ClientUtils;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.controlpanel.PanelComponent;
import malte0811.industrialWires.controlpanel.PanelUtils;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static malte0811.industrialWires.controlpanel.PanelComponent.*;

public class ItemPanelComponent extends Item {
	private static final String[] types = {
			"lighted_button", "label", "indicator_light", "slider"
	};

	public ItemPanelComponent() {
		setUnlocalizedName(IndustrialWires.MODID + ".panel_component");
		setHasSubtypes(true);
		this.setCreativeTab(IndustrialWires.creativeTab);
		setMaxStackSize(64);
		setRegistryName(new ResourceLocation(IndustrialWires.MODID, "panel_component"));
		GameRegistry.register(this);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean adv) {
		if (GuiScreen.isShiftKeyDown()) {
			NBTTagCompound nbt = getTagCompound(stack);
			NBTTagCompound data = nbt.getCompoundTag("data");
			PanelUtils.addInfo(stack, list, data);
		} else {
			list.add(I18n.format("desc.immersiveengineering.info.holdShiftForInfo"));
		}
	}

	@Nonnull
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
	public void getSubItems(@Nonnull Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
		for (int i = 0; i < types.length; i++) {
			subItems.add(new ItemStack(itemIn, 1, i));
		}
	}

	@Nullable
	public PanelComponent componentFromStack(ItemStack stack) {
		NBTTagCompound loadFrom = getTagCompound(stack).getCompoundTag("data").copy();
		loadFrom.setString("type", types[stack.getMetadata()]);
		return PanelComponent.read(loadFrom);
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
				nbt.setTag("data", written);
			}
		}
		return nbt;
	}
}
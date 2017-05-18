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

import malte0811.industrialWires.IndustrialWires;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;

public class ItemKey extends Item implements INetGUIItem {
	private static final String lockId = "lockId";

	public ItemKey() {
		setUnlocalizedName(IndustrialWires.MODID + ".key");
		setHasSubtypes(true);
		this.setCreativeTab(IndustrialWires.creativeTab);
		setMaxStackSize(64);
		setRegistryName(new ResourceLocation(IndustrialWires.MODID, "key"));
		GameRegistry.register(this);
	}

	@Nonnull
	@Override
	public String getItemStackDisplayName(@Nonnull ItemStack stack) {
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt!=null&&nbt.hasKey("name")) {
			return I18n.format("item."+IndustrialWires.MODID+".key_named.name")+nbt.getString("name");
		}
		return super.getItemStackDisplayName(stack);
	}

	@Nonnull
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt==null||!nbt.hasKey(lockId)) {
			return I18n.format("item."+IndustrialWires.MODID+".key_raw.name");
		}
		return super.getUnlocalizedName(stack);
	}

	public static void setId(ItemStack stack, int lockID) {
		stack.setTagInfo(lockId, new NBTTagInt(lockID));
	}

	public static int idForKey(@Nonnull ItemStack held) {
		if (held.getItem()!=IndustrialWires.key) {
			return 0;
		}
		NBTTagCompound nbt = held.getTagCompound();
		if (nbt!=null) {
			return nbt.getInteger(lockId);
		}
		return 0;
	}

	@Override
	public int getItemStackLimit(ItemStack stack) {
		return 1;
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
	public void onChange(NBTTagCompound nbt, EntityPlayer p, EnumHand hand) {
		ItemStack held = p.getHeldItem(hand);
		held.setTagInfo("name", nbt.getTag("name"));
	}
}

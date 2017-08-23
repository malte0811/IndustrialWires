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
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemKey extends Item implements INetGUIItem {
	public static final String LOCK_ID = "lockId";
	public static final String RING_KEYS = "ringkeys";
	public static final String NAME = "name";
	public static final String[] types = {"blank_key", "key", "key_ring"};
	public static final String ITEM_NAME = "key";

	public ItemKey() {
		setUnlocalizedName(IndustrialWires.MODID + "."+ITEM_NAME);
		setHasSubtypes(true);
		this.setCreativeTab(IndustrialWires.creativeTab);
		setMaxStackSize(64);
		setRegistryName(new ResourceLocation(IndustrialWires.MODID, ITEM_NAME));
		IndustrialWires.items.add(this);
	}

	@Nonnull
	@Override
	public String getItemStackDisplayName(@Nonnull ItemStack stack) {
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt!=null&&nbt.hasKey(NAME)&&!nbt.getString(NAME).trim().isEmpty()) {
			return I18n.format("item."+IndustrialWires.MODID+".key.key_named.name")+" "+nbt.getString(NAME);
		}
		return super.getItemStackDisplayName(stack);
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		if (stack.getMetadata()==2&&stack.getTagCompound()!=null) {
			NBTTagList keys = stack.getTagCompound().getTagList(RING_KEYS, 10);
			for (int i = 0;i< keys.tagCount()-1;i++) {
				tooltip.add(I18n.format("item."+IndustrialWires.MODID+".key.key_named.name")+" "+keys.getCompoundTagAt(i).getString(NAME));
			}
		}
	}

	@Override
	public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> subItems) {
		if (isInCreativeTab(tab)) {
			subItems.add(new ItemStack(this, 1, 0));
			subItems.add(new ItemStack(this, 1, 2));
		}
	}

	@Nonnull
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return "item."+IndustrialWires.MODID+".key."+types[stack.getMetadata()];
	}

	public static void setId(ItemStack stack, int lockID) {
		stack.setTagInfo(LOCK_ID, new NBTTagInt(lockID));
	}

	public static int idForKey(@Nonnull ItemStack held) {
		if (held.getItem()!=IndustrialWires.key||held.getMetadata()==0) {
			return 0;
		}
		NBTTagCompound nbt = held.getTagCompound();
		if (nbt!=null) {
			return nbt.getInteger(LOCK_ID);
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
		ItemStack held = playerIn.getHeldItem(hand);
		if (!worldIn.isRemote) {
			if (playerIn.isSneaking()&&held.getMetadata()==2) {
				//select next key
				NBTTagCompound nbt = held.getTagCompound();
				if (nbt!=null) {
					NBTTagList allKeys = nbt.getTagList(RING_KEYS, 10);
					if (allKeys.tagCount()>1) {
						NBTTagCompound next = allKeys.getCompoundTagAt(0);
						allKeys.removeTag(0);
						allKeys.appendTag(next);
						nbt.setInteger(LOCK_ID, next.getInteger(LOCK_ID));
						nbt.setString(NAME, next.getString(NAME));
						playerIn.inventory.markDirty();
					}
				}
			} else if (idForKey(held)!=0&&held.getMetadata()==1) {
				playerIn.openGui(IndustrialWires.MODID, 1, worldIn, 0, 0, hand == EnumHand.MAIN_HAND ? 1 : 0);
			}
		}
		return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(hand));
	}

	@Override
	public void onChange(NBTTagCompound nbt, EntityPlayer p, EnumHand hand) {
		ItemStack held = p.getHeldItem(hand);
		String name = nbt.getString(NAME);
		if (!name.trim().isEmpty()) {
			held.setTagInfo(NAME, new NBTTagString(name));
		} else {
			NBTTagCompound heldNBT = held.getTagCompound();
			if (heldNBT!=null) {
				heldNBT.removeTag(NAME);
			}
		}
	}
}

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
package malte0811.industrialWires.items;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.wires.IWireCoil;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import malte0811.industrialWires.IWConfig;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.util.ConversionUtil;
import malte0811.industrialWires.wires.MixedWireType;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemIC2Coil extends Item implements IWireCoil {
	public final static String[] subNames = {
			"tin", "copper", "gold", "hv", "glass", "tin_ins", "copper_ins", "gold_ins"
	};
	public final static String lengthKey = "wireLength";
	public final static String NAME = "ic2_wire_coil";

	public ItemIC2Coil() {
		setUnlocalizedName(IndustrialWires.MODID + "."+NAME);
		setHasSubtypes(true);
		this.setCreativeTab(IndustrialWires.creativeTab);
		setMaxStackSize(1);
		setRegistryName(new ResourceLocation(IndustrialWires.MODID, NAME));
		IndustrialWires.items.add(this);
	}


	@Override
	public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> subItems) {
		if (isInCreativeTab(tab)) {
			for (int i = 0; i < subNames.length; i++) {
				ItemStack tmp = new ItemStack(this, 1, i);
				setLength(tmp, getMaxWireLength(tmp));
				subItems.add(tmp);
			}
		}
	}

	@Nonnull
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return this.getUnlocalizedName() + "." + subNames[stack.getMetadata()];
	}

	@Override
	public MixedWireType getWireType(ItemStack stack) {
		return MixedWireType.ALL[stack.getMetadata()];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		tooltip.add(I18n.format(IndustrialWires.MODID + ".desc.wireLength", getLength(stack)));
		MixedWireType wireType = MixedWireType.ALL[stack.getMetadata()];
		double ioRate = wireType.getIORate();
		double transferRate = ioRate*wireType.getFactor();
		tooltip.add(I18n.format(IndustrialWires.MODID + ".tooltip.transfer_rate",
				transferRate*ConversionUtil.euPerJoule(), transferRate*ConversionUtil.ifPerJoule()*IWConfig.wireRatio));
		tooltip.add(I18n.format(IndustrialWires.MODID + ".tooltip.input_rate",
				ioRate*ConversionUtil.euPerJoule(), ioRate*ConversionUtil.ifPerJoule()*IWConfig.wireRatio));
		if (stack.getTagCompound() != null && stack.getTagCompound().hasKey("linkingPos")) {
			int[] link = stack.getTagCompound().getIntArray("linkingPos");
			if (link.length > 3) {
				tooltip.add(I18n.format(Lib.DESC_INFO + "attachedToDim", link[1], link[2], link[3], link[0]));
			}
		}
		tooltip.add(I18n.format(IndustrialWires.MODID + ".desc.recipe"));
	}

	//mostly copied from IE
	@Nonnull
	@Override
	public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
		return ApiUtils.doCoilUse(this, player, world, pos, hand, side, hitX, hitY, hitZ);
	}

	public static void setLength(ItemStack i, int blocks) {
		i.setTagInfo(lengthKey, new NBTTagInt(blocks));
	}

	public static int getLength(ItemStack i) {
		if (i.getTagCompound() == null) {
			setLength(i, 4);
		}
		return ItemNBTHelper.getInt(i, lengthKey)*i.getCount();
	}

	public static int getMaxWireLength(ItemStack i) {
		return IWConfig.maxLengthOnCoil[i.getItemDamage()%5];
	}

	@Override
	public int getMaxLength(ItemStack stack) {
		return getLength(stack);
	}

	@Override
	public void consumeWire(ItemStack stack, int lengthConsumed) {
		int lengthOnStack = getLength(stack);
		if (lengthConsumed < lengthOnStack) {
			setLength(stack, lengthOnStack - lengthConsumed);
		} else {
			stack.shrink(1);
		}
	}
}

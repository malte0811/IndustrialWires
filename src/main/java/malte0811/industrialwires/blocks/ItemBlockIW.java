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
package malte0811.industrialwires.blocks;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import malte0811.industrialwires.IndustrialWires;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class ItemBlockIW extends ItemBlock {
	private final Object[] values;

	public ItemBlockIW(Block b) {
		super(b);
		if (b instanceof IMetaEnum) {
			values = ((IMetaEnum) b).getValues();
		} else {
			values = null;
		}
		hasSubtypes = true;
		setRegistryName(b.getRegistryName());
		setCreativeTab(IndustrialWires.creativeTab);
	}

	@Nonnull
	@Override
	public String getTranslationKey(ItemStack stack) {
		int meta = stack.getMetadata();
		if (values != null) {
			return block.getTranslationKey() + "." + values[meta].toString().toLowerCase();
		} else {
			return block.getTranslationKey();
		}
	}

	@Override
	public int getMetadata(int damage) {
		return damage;
	}

	@Override
	public boolean placeBlockAt(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, World world, @Nonnull BlockPos pos,
								EnumFacing side, float hitX, float hitY, float hitZ, @Nonnull IBlockState newState) {
		if (block instanceof IPlacementCheck && !((IPlacementCheck) block).canPlaceBlockAt(world, pos, stack)) {
			return false;
		}
		boolean ret = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
		if (ret) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof IEBlockInterfaces.IDirectionalTile) {
				EnumFacing dir = ((IEBlockInterfaces.IDirectionalTile) te).getFacingForPlacement(player, pos, side, hitX, hitY, hitZ);
				((IEBlockInterfaces.IDirectionalTile) te).setFacing(dir);
			}
			if (te instanceof IEBlockInterfaces.ITileDrop) {
				((IEBlockInterfaces.ITileDrop) te).readOnPlacement(player, stack);
			}
			if (te instanceof IHasDummyBlocksIW) {
				((IHasDummyBlocksIW) te).placeDummies(world.getBlockState(pos));
			}
		}
		return ret;
	}
}

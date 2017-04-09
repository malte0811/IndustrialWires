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
package malte0811.industrialWires.blocks;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemBlockIW extends ItemBlock {
	private final Object[] values;
	public ItemBlockIW(Block b) {
		super(b);
		if (b instanceof IMetaEnum) {
			values = ((IMetaEnum)b).getValues();
		} else {
			values = null;
		}
		hasSubtypes = true;
	}
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		int meta = stack.getMetadata();
		if (values!=null) {
			return block.getUnlocalizedName() + "." + values[meta].toString().toLowerCase();
		} else {
			return block.getUnlocalizedName();
		}
	}
	@Override
	public int getMetadata(int damage) {
		return damage;
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
		if (block instanceof IPlacementCheck&&!((IPlacementCheck) block).canPlaceBlockAt(world, pos, stack)) {
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
		}
		return ret;
	}
}

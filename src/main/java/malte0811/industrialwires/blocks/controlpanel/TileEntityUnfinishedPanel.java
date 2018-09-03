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

package malte0811.industrialwires.blocks.controlpanel;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;

import javax.annotation.Nonnull;

public class TileEntityUnfinishedPanel extends TileEntityPanel {
	public TileEntityUnfinishedPanel() {
		super();
		getComponents().clear();
	}

	@Override
	public boolean interact(@Nonnull EnumFacing side, @Nonnull EntityPlayer player, @Nonnull EnumHand hand, @Nonnull ItemStack heldItem, float hitX, float hitY, float hitZ) {
		return false;
	}

	@Nonnull
	@Override
	public ItemStack getTileDrop(@Nonnull EntityPlayer player, @Nonnull IBlockState state) {
		ItemStack ret = super.getTileDrop(player, state);
		ret.setItemDamage(BlockTypes_Panel.UNFINISHED.ordinal());
		NBTTagCompound nbt = ret.getTagCompound();
		if (nbt != null && nbt.hasKey("components"))
			nbt.removeTag("components");
		return ret;
	}
}

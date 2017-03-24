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

package malte0811.industrialWires.blocks.controlpanel;

import blusunrize.immersiveengineering.api.IEProperties;
import malte0811.industrialWires.blocks.BlockIWBase;
import malte0811.industrialWires.blocks.IPlacementCheck;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class BlockPanel extends BlockIWBase implements IPlacementCheck {
	public BlockPanel() {
		super(Material.IRON, "control_panel");
	}

	@Override
	public boolean canPlaceBlockAt(World w, BlockPos pos, ItemStack stack) {
		return true;//TODO actually check for space
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityPanel();
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	protected IProperty[] getProperties() {
		return new IProperty[]{IEProperties.FACING_HORIZONTAL};
	}

	@Override
	protected BlockStateContainer createBlockState() {
		BlockStateContainer base = super.createBlockState();
		return new ExtendedBlockState(this, base.getProperties().toArray(new IProperty[0]), new IUnlistedProperty[]{
				PropertyComponents.INSTANCE
		});
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		state = super.getExtendedState(state, world, pos);
		if (state instanceof IExtendedBlockState) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof TileEntityPanel) {
				state = ((IExtendedBlockState) state).withProperty(PropertyComponents.INSTANCE, ((TileEntityPanel) te).components);
			}
		}
		return state;
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return 0;
	}
}

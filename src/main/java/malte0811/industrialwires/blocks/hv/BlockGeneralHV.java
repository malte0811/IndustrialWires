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

package malte0811.industrialwires.blocks.hv;

import blusunrize.immersiveengineering.api.IEProperties;
import malte0811.industrialwires.blocks.BlockIWBase;
import malte0811.industrialwires.blocks.IMetaEnum;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockGeneralHV extends BlockIWBase implements IMetaEnum {
	public static final PropertyEnum<BlockTypes_GeneralHV> PROPERTY = PropertyEnum.create("type",
			BlockTypes_GeneralHV.class);
	public static final String NAME = "general_hv";

	public BlockGeneralHV() {
		super(Material.IRON, NAME);
	}

	@Override
	protected IProperty[] getProperties() {
		return new IProperty[] {
				IEProperties.BOOLEANS[0], PROPERTY, IEProperties.FACING_HORIZONTAL
		};
	}

	@Override
	public boolean isTopSolid(IBlockState state) {
		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullBlock(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Nonnull
	@Override
	public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		state = super.getActualState(state, worldIn, pos);
		TileEntity te = worldIn.getTileEntity(pos);
		if (te instanceof TileEntityDischargeMeter)
			state = state.withProperty(IEProperties.BOOLEANS[0], ((TileEntityDischargeMeter) te).hasWire);
		return state;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		switch (state.getValue(PROPERTY)) {
			case DISCHARGE_METER:
				return new TileEntityDischargeMeter();
		}
		return null;
	}

	@Override
	public BlockTypes_GeneralHV[] getValues() {
		return BlockTypes_GeneralHV.values();
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(PROPERTY).ordinal();
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(PROPERTY, getValues()[meta]);
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}
}

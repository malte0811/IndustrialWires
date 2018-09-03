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
import malte0811.industrialwires.blocks.BlockIWMultiblock;
import malte0811.industrialwires.blocks.IMetaEnum;
import malte0811.industrialwires.blocks.IWProperties;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockHVMultiblocks extends BlockIWMultiblock implements IMetaEnum {
	public static final PropertyEnum<BlockTypes_HVMultiblocks> type = PropertyEnum.create("type", BlockTypes_HVMultiblocks.class);
	public static final String NAME = "hv_multiblock";
	public BlockHVMultiblocks() {
		super(Material.IRON, NAME);
	}

	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		// No MB's in the creative inventory!
	}

	@Override
	public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
		if (state.getValue(IWProperties.MARX_TYPE)!= IWProperties.MarxType.CONNECTOR)
			return super.canRenderInLayer(state, layer);
		else
			return layer==BlockRenderLayer.TRANSLUCENT||layer==BlockRenderLayer.SOLID;
	}

	@Override
	protected IProperty[] getProperties() {
		return new IProperty[]{type, IWProperties.MARX_TYPE, IEProperties.FACING_HORIZONTAL, IEProperties.BOOLEANS[0]};
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		switch (state.getValue(type)) {
		case MARX:
			return new TileEntityMarx(state.getValue(IEProperties.FACING_HORIZONTAL), state.getValue(IWProperties.MARX_TYPE), state.getValue(IEProperties.BOOLEANS[0]));
		}
		return null;
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(type).getMeta();
	}

	@Nonnull
	@Override
	public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		IBlockState ret = super.getActualState(state, worldIn, pos);
		TileEntity te = worldIn.getTileEntity(pos);
		if (te instanceof TileEntityMarx) {
			ret = ret.withProperty(IWProperties.MARX_TYPE, ((TileEntityMarx) te).type);
			ret = ret.withProperty(IEProperties.BOOLEANS[0], ((TileEntityMarx)te).mirrored);
		}
		return ret;
	}

	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
		BlockStateContainer base = super.createBlockState();
		return new ExtendedBlockState(this, base.getProperties().toArray(new IProperty[0]), new IUnlistedProperty[]{
				IEProperties.CONNECTIONS
		});
	}

	@Override
	public Object[] getValues() {
		return BlockTypes_HVMultiblocks.values();
	}
}

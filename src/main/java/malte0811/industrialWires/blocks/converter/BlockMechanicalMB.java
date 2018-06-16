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

package malte0811.industrialWires.blocks.converter;

import blusunrize.immersiveengineering.api.IEProperties;
import malte0811.industrialWires.blocks.BlockIWMultiblock;
import malte0811.industrialWires.blocks.IMetaEnum;
import malte0811.industrialWires.blocks.IWProperties;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockMechanicalMB extends BlockIWMultiblock implements IMetaEnum {
    public static final PropertyEnum<MechanicalMBBlockType> TYPE = PropertyEnum.create("type", MechanicalMBBlockType.class);
    public static final String NAME = "mech_mb";
    public BlockMechanicalMB() {
        super(Material.IRON, NAME);
    }

	@Override
	public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
		//NOP
	}

	@Override
    protected IProperty[] getProperties() {
        return new IProperty[] {
                IEProperties.FACING_HORIZONTAL, TYPE
        };
    }

	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
		BlockStateContainer base = super.createBlockState();
		return new ExtendedBlockState(this, base.getProperties().toArray(new IProperty[0]), new IUnlistedProperty[]{
				IWProperties.MB_SIDES
		});
	}

	@Nonnull
	@Override
	public IBlockState getExtendedState(@Nonnull IBlockState state, IBlockAccess world, BlockPos pos) {
		TileEntity te = world.getTileEntity(pos);
		state = super.getExtendedState(state, world, pos);
		if (te instanceof TileEntityMechMB)
			state = ((TileEntityMechMB) te).getExtState(state);
		return state;
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		return new TileEntityMechMB();
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(TYPE).ordinal();
	}

	@Nonnull
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(TYPE, MechanicalMBBlockType.VALUES[meta]);
	}

	@Override
	public Object[] getValues() {
		return TYPE.getAllowedValues().toArray();
	}

	@Override
	public boolean eventReceived(IBlockState state, World worldIn, BlockPos pos, int id, int param) {
		if ((id&255)==255) {
			IBlockState s = worldIn.getBlockState(pos);
			worldIn.notifyBlockUpdate(pos, s, s, 3);
			if (param>=0) {
				TileEntity te = worldIn.getTileEntity(pos);
				if (te instanceof TileEntityMechMB && !((TileEntityMechMB) te).isLogicDummy()) {
					int[] offsets = ((TileEntityMechMB) te).offsets;
					if (offsets!=null && param<offsets.length) {
						BlockPos otherPos = pos.offset(((TileEntityMechMB) te).getFacing(), -offsets[param]);
						s = worldIn.getBlockState(otherPos);
						worldIn.notifyBlockUpdate(otherPos, s, s, 3);
					}
				}
			}
		}
    	return super.eventReceived(state, worldIn, pos, id, param);
	}
}

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

import blusunrize.immersiveengineering.api.IEProperties;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.TileEntityJacobsLadder.LadderSize;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class BlockJacobsLadder extends BlockIWBase implements IMetaEnum, IPlacementCheck {
	private static PropertyEnum<LadderSize> size_property = PropertyEnum.create("size", LadderSize.class);

	public BlockJacobsLadder() {
		super(Material.IRON, "jacobs_ladder");
	}

	@Nonnull
	@Override
	public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		state = super.getActualState(state, worldIn, pos);
		TileEntity tile = worldIn.getTileEntity(pos);
		if (tile instanceof TileEntityJacobsLadder) {
			state = applyProperty(state, size_property, ((TileEntityJacobsLadder) tile).size);
		}
		return state;
	}

	@Override
	protected IProperty<?>[] getProperties() {
		return new IProperty[]{
				size_property, IEProperties.MULTIBLOCKSLAVE, IEProperties.FACING_HORIZONTAL
		};
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(size_property).ordinal();
	}

	@Nonnull
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return super.getStateFromMeta(meta).withProperty(size_property, LadderSize.values()[meta]);
	}

	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		if (tab== IndustrialWires.creativeTab) {
			for (int i = 0; i < LadderSize.values().length; i++) {
				list.add(new ItemStack(this, 1, i));
			}
		}
	}

	@Override
	public Object[] getValues() {
		return LadderSize.values();
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		return new TileEntityJacobsLadder(state.getValue(size_property));
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
	public IBlockState getStateForPlacement(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing facing,
											float hitX, float hitY, float hitZ, int meta, @Nonnull EntityLivingBase placer, EnumHand hand) {
		EnumFacing f = facing.getOpposite();
		if (facing.getAxis() == EnumFacing.Axis.Y) {
			double dX = hitX - .5;
			double dZ = hitZ - .5;
			if (Math.abs(dX) > Math.abs(dZ)) {
				f = EnumFacing.getFacingFromAxis(dX > 0 ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE, EnumFacing.Axis.X);
			} else {
				f = EnumFacing.getFacingFromAxis(dZ > 0 ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE, EnumFacing.Axis.Z);
			}
		}
		return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand).withProperty(IEProperties.FACING_HORIZONTAL, f);
	}

	@Nonnull
	@Override
	public ItemStack getPickBlock(@Nonnull IBlockState state, RayTraceResult target, @Nonnull World world, @Nonnull BlockPos pos, EntityPlayer player) {
		return new ItemStack(this, 1, getMetaFromState(state));
	}

	@Override
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
		super.onEntityCollidedWithBlock(worldIn, pos, state, entityIn);
		TileEntity te = worldIn.getTileEntity(pos);
		if (te instanceof TileEntityJacobsLadder) {
			((TileEntityJacobsLadder) te).onEntityTouch(entityIn);
		}
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileEntity te = worldIn.getTileEntity(pos);
		if (te instanceof TileEntityJacobsLadder) {
			return ((TileEntityJacobsLadder) te).onActivated(playerIn, hand);
		}
		return super.onBlockActivated(worldIn, pos, state, playerIn, hand, side, hitX, hitY, hitZ);
	}

	@Override
	public boolean canPlaceBlockAt(World w, BlockPos pos, ItemStack stack) {
		int dummyCount = LadderSize.values()[stack.getMetadata()].dummyCount;
		for (int i = 1; i <= dummyCount; i++) {
			if (!w.isAirBlock(pos.up(i))) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean rotateBlock(World world, @Nonnull BlockPos pos, @Nonnull EnumFacing axis) {
		TileEntity te = world.getTileEntity(pos);
		return te instanceof TileEntityJacobsLadder && ((TileEntityJacobsLadder) te).rotate(world, pos, axis);
	}
}

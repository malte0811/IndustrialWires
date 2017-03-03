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
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class BlockJacobsLadder extends Block implements IMetaEnum {
	static PropertyEnum<LadderSize> size_property = PropertyEnum.create("size", LadderSize.class);

	public BlockJacobsLadder() {
		super(Material.IRON);
		setHardness(3.0F);
		setResistance(15.0F);
		String name = "jacobs_ladder";
		GameRegistry.register(this, new ResourceLocation(IndustrialWires.MODID, name));
		GameRegistry.register(new ItemBlockIW(this), new ResourceLocation(IndustrialWires.MODID, name));
		setUnlocalizedName(IndustrialWires.MODID+"."+name);
		setCreativeTab(IndustrialWires.creativeTab);
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		TileEntity te = worldIn.getTileEntity(pos);
		if (te instanceof IHasDummyBlocksIW) {
			((IHasDummyBlocksIW) te).breakDummies();
		}
		super.breakBlock(worldIn, pos, state);
		worldIn.removeTileEntity(pos);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		BlockStateContainer cont = super.createBlockState();
		IProperty[] props = cont.getProperties().toArray(new IProperty[0]);
		props = Arrays.copyOf(props, props.length + 3);
		props[props.length - 3] = size_property;
		props[props.length - 2] = IEProperties.MULTIBLOCKSLAVE;
		props[props.length - 1] = IEProperties.FACING_HORIZONTAL;
		return new BlockStateContainer(this, props);
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		TileEntity tile = worldIn.getTileEntity(pos);
		if (tile instanceof IHasDummyBlocksIW) {
			state = applyProperty(state, IEProperties.MULTIBLOCKSLAVE, ((IHasDummyBlocksIW) tile).isDummy());
		}
		if (tile instanceof TileEntityJacobsLadder) {
			state = applyProperty(state, size_property, ((TileEntityJacobsLadder) tile).size);
			state = applyProperty(state, IEProperties.FACING_HORIZONTAL, ((TileEntityJacobsLadder) tile).facing);
		}
		return super.getActualState(state, worldIn, pos);
	}

	private <V extends Comparable<V>> IBlockState applyProperty(IBlockState in, IProperty<V> prop, V val) {
		return in.withProperty(prop, val);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(size_property).ordinal();
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return super.getStateFromMeta(meta).withProperty(size_property, LadderSize.values()[meta]);
	}

	@Override
	public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
		for (int i = 0; i < LadderSize.values().length; i++) {
			list.add(new ItemStack(this, 1, i));
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
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityJacobsLadder(state.getValue(size_property));
	}

	@Override
	public boolean isFullyOpaque(IBlockState state) {
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

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, ItemStack stack) {
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
		return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, stack).withProperty(IEProperties.FACING_HORIZONTAL, f);
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
		TileEntity te = worldIn.getTileEntity(pos);
		if (te instanceof TileEntityJacobsLadder) {
			((TileEntityJacobsLadder) te).facing = state.getValue(IEProperties.FACING_HORIZONTAL);
		}
		if (te instanceof IHasDummyBlocksIW) {
			((IHasDummyBlocksIW) te).placeDummies(state);
		}
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		TileEntity te = source.getTileEntity(pos);
		if (!(te instanceof TileEntityJacobsLadder)) {
			return FULL_BLOCK_AABB;
		}
		TileEntityJacobsLadder tile = (TileEntityJacobsLadder) te;
		if (!tile.isDummy()) {
			//transformer
			return FULL_BLOCK_AABB;
		} else {
			Vec3d min;
			Vec3d max;
			LadderSize size = tile.size;
			double distX = (1 - size.topDistance) / 2;
			double distZ = .5 - .0625 * (size.ordinal() + 1);
			double h = Math.min(1, 1 + size.height - tile.dummy);
			if (tile.facing.getAxis() == EnumFacing.Axis.Z) {
				min = new Vec3d(distX, 0, distZ);
				max = new Vec3d(1 - distX, h, 1 - distZ);
			} else {
				min = new Vec3d(distZ, 0, distX);
				max = new Vec3d(1 - distZ, h, 1 - distX);
			}
			return new AxisAlignedBB(min, max);
		}
	}

	@Nullable
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, World worldIn, BlockPos pos) {
		return getBoundingBox(blockState, worldIn, pos);
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn) {
		AxisAlignedBB axisalignedbb = getBoundingBox(state, worldIn, pos).offset(pos);
		if (entityBox.intersectsWith(axisalignedbb)) {
			collidingBoxes.add(axisalignedbb);
		}
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
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
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileEntity te = worldIn.getTileEntity(pos);
		if (te instanceof TileEntityJacobsLadder) {
			return ((TileEntityJacobsLadder) te).onActivated(playerIn, hand, heldItem);
		}
		return super.onBlockActivated(worldIn, pos, state, playerIn, hand, heldItem, side, hitX, hitY, hitZ);
	}
}

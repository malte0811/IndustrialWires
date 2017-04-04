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
package malte0811.industrialWires.blocks.converter;

import blusunrize.immersiveengineering.api.IEProperties;
import malte0811.industrialWires.blocks.BlockIWBase;
import malte0811.industrialWires.blocks.IMetaEnum;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import java.util.List;

public class BlockMechanicalConverter extends BlockIWBase implements IMetaEnum {
	private static PropertyEnum<MechanicalBlockType> type = PropertyEnum.create("type", MechanicalBlockType.class);
	public BlockMechanicalConverter() {
		super(Material.IRON, "mechanical_converter");
	}

	@Override
	public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
		for (int i = 0;i<3;i++) {
			list.add(new ItemStack(itemIn, 1, i));
		}
	}

	@Override
	protected IProperty[] getProperties() {
		return new IProperty[]{type, IEProperties.FACING_ALL};
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(type, MechanicalBlockType.values[meta]);
	}

	@Override
	public Object[] getValues() {
		return MechanicalBlockType.values;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		switch (state.getValue(type)) {
		case IE_MOTOR:
			return new TileEntityIEMotor();
		case IE_TO_IC2:
			return new TileEntityMechIEtoIC();
		case IC2_TO_IE:
			return new TileEntityMechICtoIE();
		}
		return null;
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(type).ordinal();
	}
	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
			float hitZ, int meta, EntityLivingBase placer, ItemStack stack) {
		IBlockState base = super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, stack);
		return base.withProperty(type, MechanicalBlockType.values[meta]);
	}
	@Override
	public int damageDropped(IBlockState state) {
		return state.getValue(type).ordinal();
	}
	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos,
			EntityPlayer player) {
		return new ItemStack(this, 1, damageDropped(state));
	}
	@Override
	@SuppressWarnings("deprecation")
	public boolean eventReceived(IBlockState state, World worldIn, BlockPos pos, int id, int param) {
		boolean def = super.eventReceived(state, worldIn, pos, id, param);
		if ((id&255)==255) {
			IBlockState s = worldIn.getBlockState(pos);
			worldIn.notifyBlockUpdate(pos, s, s, 3);
			return true;
		}
		return def;
	}
}

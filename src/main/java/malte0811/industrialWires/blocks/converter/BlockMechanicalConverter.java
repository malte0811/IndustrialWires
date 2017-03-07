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
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.util.Utils;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.IMetaEnum;
import malte0811.industrialWires.blocks.ItemBlockIW;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.List;

public class BlockMechanicalConverter extends Block implements IMetaEnum, ITileEntityProvider {
	PropertyEnum<MechanicalBlockType> type;
	public BlockMechanicalConverter() {
		super(Material.IRON);
		setHardness(3.0F);
		setResistance(15.0F);
		String name = "mechanical_converter";
		GameRegistry.register(this, new ResourceLocation(IndustrialWires.MODID, name));
		GameRegistry.register(new ItemBlockIW(this), new ResourceLocation(IndustrialWires.MODID, name));
		setUnlocalizedName(name);
		setCreativeTab(IndustrialWires.creativeTab);
	}

	@Override
	public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
		for (int i = 0;i<3;i++) {
			list.add(new ItemStack(itemIn, 1, i));
		}
	}

	@Override
	protected BlockStateContainer createBlockState() {
		type = PropertyEnum.create("type", MechanicalBlockType.class);
		BlockStateContainer container = new BlockStateContainer(this, type, IEProperties.FACING_ALL);
		return container;
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		IBlockState ret = super.getActualState(state, world, pos);
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof IDirectionalTile) {
			ret = ret.withProperty(IEProperties.FACING_ALL, ((IDirectionalTile) te).getFacing());
		}
		return ret;
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
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		switch (MechanicalBlockType.values[meta]) {
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
	public int getMetaFromState(IBlockState state) {
		return state.getValue(type).ordinal();
	}
	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
			float hitZ, int meta, EntityLivingBase placer, ItemStack stack) {
		IBlockState base = super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, stack);
		base = base.withProperty(type, MechanicalBlockType.values[meta]);
		return base.withProperty(IEProperties.FACING_ALL, facing.getOpposite());
	}
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer,
			ItemStack stack) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
		TileEntity te = worldIn.getTileEntity(pos);
		if (te instanceof IDirectionalTile) {
			((IDirectionalTile)te).setFacing(state.getValue(IEProperties.FACING_ALL));
		}
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
	//mostly copied from IE
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
			EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof IDirectionalTile && Utils.isHammer(heldItem) && !world.isRemote) {
			IDirectionalTile directionalTe = (IDirectionalTile) te;
			if (directionalTe.canHammerRotate(side, hitX, hitY, hitZ, player)) {
				EnumFacing f = directionalTe.getFacing();
				final EnumFacing original = f;
				int limit = directionalTe.getFacingLimitation();

				if(limit==0) {
					f = EnumFacing.VALUES[(f.ordinal() + 1) % EnumFacing.VALUES.length];
				} else if(limit==1) {
					f = player.isSneaking()?f.rotateAround(side.getAxis()).getOpposite():f.rotateAround(side.getAxis());
				} else if(limit == 2 || limit == 5) {
					f = player.isSneaking()?f.rotateYCCW():f.rotateY();
				}
				if (f!=original) {
					directionalTe.setFacing(f);
					te.markDirty();
					world.notifyBlockUpdate(pos,state,state,3);
					world.addBlockEvent(pos, this, 255, 0);
				}
				return true;
			}
		}
		return false;
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

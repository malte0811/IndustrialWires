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
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.BlockIWBase;
import malte0811.industrialWires.blocks.IMetaEnum;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import javax.annotation.Nonnull;
import java.util.List;

public class BlockPanel extends BlockIWBase implements IMetaEnum {
	public static final PropertyEnum<BlockTypes_Panel> type = PropertyEnum.create("type", BlockTypes_Panel.class);

	public BlockPanel() {
		super(Material.IRON, "control_panel");
		lightOpacity = 0;
	}

	@Override
	public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
		switch (state.getValue(type)) {
		case TOP:
			return layer == BlockRenderLayer.CUTOUT;
		case RS_WIRE:
			return layer == BlockRenderLayer.TRANSLUCENT || layer == BlockRenderLayer.SOLID;
		default:
			return super.canRenderInLayer(state, layer);
		}
	}

	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		switch (state.getValue(type)) {
		case TOP:
			return new TileEntityPanel();
		case RS_WIRE:
			return new TileEntityRSPanelConn();
		case CREATOR:
			return new TileEntityPanelCreator();
		default:
			return null;
		}
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	protected IProperty<?>[] getProperties() {
		return new IProperty[]{IEProperties.FACING_ALL, type};
	}

	@Override
	protected BlockStateContainer createBlockState() {
		BlockStateContainer base = super.createBlockState();
		return new ExtendedBlockState(this, base.getProperties().toArray(new IProperty[0]), new IUnlistedProperty[]{
				PropertyComponents.INSTANCE, IEProperties.CONNECTIONS
		});
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		state = super.getActualState(state, worldIn, pos);
		TileEntity te = worldIn.getTileEntity(pos);
		if (te instanceof TileEntityPanel) {
			state.withProperty(type, BlockTypes_Panel.TOP);
		}
		if (te instanceof TileEntityRSPanelConn) {
			state.withProperty(type, BlockTypes_Panel.RS_WIRE);
		}
		return state;
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		state = super.getExtendedState(state, world, pos);
		if (state instanceof IExtendedBlockState) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof TileEntityPanel) {
				state = ((IExtendedBlockState) state).withProperty(PropertyComponents.INSTANCE, ((TileEntityPanel) te).getComponents());
			}
		}
		return state;
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(type).ordinal();
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return super.getStateFromMeta(meta).withProperty(type, BlockTypes_Panel.values()[meta]);
	}

	@Override
	public Object[] getValues() {
		return BlockTypes_Panel.values();
	}

	@Override
	public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
		list.add(new ItemStack(itemIn, 1, 0));
		list.add(new ItemStack(itemIn, 1, 1));
		list.add(new ItemStack(itemIn, 1, 2));
		list.add(new ItemStack(itemIn, 1, 3));
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
	public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	@Override
	public boolean isVisuallyOpaque() {
		return false;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!super.onBlockActivated(world, pos, state, player, hand, heldItem, side, hitX, hitY, hitZ)&&hand==EnumHand.MAIN_HAND) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof TileEntityRSPanelConn) {
				if (!world.isRemote) {
					player.openGui(IndustrialWires.instance, 0, te.getWorld(), te.getPos().getX(), te.getPos().getY(), te.getPos().getZ());
				}
				return true;
			}
			if (te instanceof TileEntityPanelCreator) {
				if (!world.isRemote) {
					player.openGui(IndustrialWires.instance, 0, te.getWorld(), te.getPos().getX(), te.getPos().getY(), te.getPos().getZ());
				}
				return true;
			}
			return false;
		}
		return state.getValue(type) == BlockTypes_Panel.TOP;
	}

	@Override
	@Nonnull
	public ItemStack getPickBlock(@Nonnull IBlockState state, RayTraceResult target, @Nonnull World world, @Nonnull BlockPos pos, EntityPlayer player) {
		if (state.getValue(type) == BlockTypes_Panel.TOP) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof TileEntityPanel) {
				return ((TileEntityPanel) te).getTileDrop(player, state);
			}
		}
		return super.getPickBlock(state, target, world, pos, player);
	}
}

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
import malte0811.industrialWires.controlpanel.PanelComponent;
import malte0811.industrialWires.controlpanel.PanelUtils;
import malte0811.industrialWires.controlpanel.PropertyComponents;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
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
	public ItemBlock createItemBlock() {
		return new ItemBlockPanel(this);
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
			case UNFINISHED:
				return new TileEntityUnfinishedPanel();
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

	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
		BlockStateContainer base = super.createBlockState();
		return new ExtendedBlockState(this, base.getProperties().toArray(new IProperty[0]), new IUnlistedProperty[]{
				PropertyComponents.INSTANCE, IEProperties.CONNECTIONS
		});
	}

	@Nonnull
	@Override
	public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess worldIn, BlockPos pos) {
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

	@Nonnull
	@Override
	public IBlockState getExtendedState(@Nonnull IBlockState state, IBlockAccess world, BlockPos pos) {
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

	@Nonnull
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return super.getStateFromMeta(meta).withProperty(type, BlockTypes_Panel.values()[meta]);
	}

	@Override
	public Object[] getValues() {
		return BlockTypes_Panel.values();
	}

	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		if (tab==IndustrialWires.creativeTab) {
			for (int i = 0; i < BlockTypes_Panel.values().length; i++) {
				list.add(new ItemStack(this, 1, i));
			}
		}
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
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ) && hand == EnumHand.MAIN_HAND) {
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
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityPanel) {
			return ((TileEntityPanel) te).getTileDrop(player, state);
		}
		return super.getPickBlock(state, target, world, pos, player);
	}

	@Override
	public void harvestBlock(@Nonnull World worldIn, EntityPlayer player, @Nonnull BlockPos pos, @Nonnull IBlockState state, TileEntity te, ItemStack stack) {
		super.harvestBlock(worldIn, player, pos, state, te, stack);
		if (te instanceof TileEntityPanel) {
			for (PanelComponent pc:((TileEntityPanel) te).getComponents()) {
				pc.dropItems((TileEntityPanel)te);
			}
		}
	}

	@Override
	public void breakBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
		super.breakBlock(worldIn, pos, state);
		//break connections
		List<BlockPos> panels = PanelUtils.discoverPanelParts(worldIn, pos, 11 * 11 * 11);
		for (BlockPos p : panels) {
			if (!p.equals(pos)) {
				TileEntity panelPart = worldIn.getTileEntity(p);
				if (panelPart instanceof TileEntityPanel) {
					((TileEntityPanel) panelPart).removeAllRSCons();
				}
			}
		}
	}

	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		super.onBlockAdded(worldIn, pos, state);
		List<BlockPos> panels = PanelUtils.discoverPanelParts(worldIn, pos, 11 * 11 * 11);
		for (BlockPos p : panels) {
			if (!p.equals(pos)) {
				TileEntity panelPart = worldIn.getTileEntity(p);
				if (panelPart instanceof TileEntityPanel) {
					((TileEntityPanel) panelPart).firstTick = true;
				}
			}
		}
	}
}

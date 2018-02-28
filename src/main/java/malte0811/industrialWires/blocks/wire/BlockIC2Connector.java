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
package malte0811.industrialWires.blocks.wire;

import blusunrize.immersiveengineering.api.IEProperties;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.BlockIWBase;
import malte0811.industrialWires.blocks.IMetaEnum;
import malte0811.industrialWires.wires.IC2Wiretype;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class BlockIC2Connector extends BlockIWBase implements IMetaEnum {
	public static final PropertyEnum<BlockTypes_IC2_Connector> TYPE = PropertyEnum.create("type", BlockTypes_IC2_Connector.class);
	public static final String NAME = "ic2_connector";

	public BlockIC2Connector() {
		super(Material.IRON, NAME);
		setHardness(3.0F);
		setResistance(15.0F);
		lightOpacity = 0;
	}

	@Override
	public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos posNeighbor) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityIC2ConnectorTin) {
			TileEntityIC2ConnectorTin connector = (TileEntityIC2ConnectorTin) te;
			if (world.isAirBlock(pos.offset(connector.facing))) {
				this.dropBlockAsItem(connector.getWorld(), pos, world.getBlockState(pos), 0);
				connector.getWorld().setBlockToAir(pos);
			}
		}
	}

	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		for (int i = 0; i < TYPE.getAllowedValues().size(); i++) {
			list.add(new ItemStack(this, 1, i));
		}
	}

	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
		BlockStateContainer base = super.createBlockState();
		IUnlistedProperty<?>[] unlisted = (base instanceof ExtendedBlockState) ? ((ExtendedBlockState) base).getUnlistedProperties().toArray(new IUnlistedProperty[0]) : new IUnlistedProperty[0];
		unlisted = Arrays.copyOf(unlisted, unlisted.length + 1);
		unlisted[unlisted.length - 1] = IEProperties.CONNECTIONS;
		return new ExtendedBlockState(this, base.getProperties().toArray(new IProperty[0]), unlisted);
	}

	@Override
	protected IProperty<?>[] getProperties() {
		return new IProperty[]{TYPE, IEProperties.FACING_ALL};
	}

	@Nonnull
	@Override
	public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		state = super.getActualState(state, worldIn, pos);
		TileEntity te = worldIn.getTileEntity(pos);
		if (te instanceof TileEntityIC2ConnectorTin) {
			state.withProperty(IEProperties.FACING_ALL, ((TileEntityIC2ConnectorTin) te).getFacing());
		}
		return state;
	}

	@Nonnull
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return super.getStateFromMeta(meta).withProperty(TYPE, BlockTypes_IC2_Connector.values()[meta]);
	}

	@Override
	public boolean isSideSolid(IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, EnumFacing side) {
		return false;
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		switch (state.getValue(TYPE)) {
		case TIN_CONN:
			return new TileEntityIC2ConnectorTin(false);
		case TIN_RELAY:
			return new TileEntityIC2ConnectorTin(true);
		case COPPER_CONN:
			return new TileEntityIC2ConnectorCopper(false);
		case COPPER_RELAY:
			return new TileEntityIC2ConnectorCopper(true);
		case GOLD_CONN:
			return new TileEntityIC2ConnectorGold(false);
		case GOLD_RELAY:
			return new TileEntityIC2ConnectorGold(true);
		case HV_CONN:
			return new TileEntityIC2ConnectorHV(false);
		case HV_RELAY:
			return new TileEntityIC2ConnectorHV(true);
		case GLASS_CONN:
			return new TileEntityIC2ConnectorGlass(false);
		case GLASS_RELAY:
			return new TileEntityIC2ConnectorGlass(true);
		}
		return null;
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag advanced) {
		super.addInformation(stack, world, tooltip, advanced);
		if (!stack.isEmpty() && stack.getMetadata() % 2 == 0) {
			int type = stack.getMetadata() / 2;
			tooltip.add(I18n.format(IndustrialWires.MODID + ".tooltip.power_tier", (type%5) + 1));
			IC2Wiretype wire = IC2Wiretype.ALL[type];
			tooltip.add(I18n.format(IndustrialWires.MODID + ".tooltip.eu_per_tick",
					wire.getTransferRate() / wire.getFactor()));
		}
	}

	@Override
	public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
		return layer == BlockRenderLayer.TRANSLUCENT || layer == BlockRenderLayer.SOLID;
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

	/*@Override
	public boolean isVisuallyOpaque() {
		return false;
	}*/
	@Override
	public Object[] getValues() {
		return BlockTypes_IC2_Connector.values();
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(TYPE).ordinal();
	}
}

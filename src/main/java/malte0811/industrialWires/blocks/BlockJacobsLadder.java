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
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import malte0811.industrialWires.IndustrialWires;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.Arrays;
import java.util.List;

public class BlockJacobsLadder extends Block implements IMetaEnum {
    static PropertyEnum<TileEntityJacobsLadder.LadderSize> size_property = PropertyEnum.create("size", TileEntityJacobsLadder.LadderSize.class);
    public BlockJacobsLadder() {
        super(Material.IRON);
        setHardness(3.0F);
        setResistance(15.0F);
        String name = "jacobs_ladder";
        GameRegistry.register(this, new ResourceLocation(IndustrialWires.MODID, name));
        GameRegistry.register(new ItemBlockIW(this), new ResourceLocation(IndustrialWires.MODID, name));
        setUnlocalizedName(name);
        setCreativeTab(IndustrialWires.creativeTab);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof IEBlockInterfaces.IHasDummyBlocks) {
            ((IEBlockInterfaces.IHasDummyBlocks) te).breakDummies(pos, state);
        }
        super.breakBlock(worldIn, pos, state);
        worldIn.removeTileEntity(pos);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        BlockStateContainer cont = super.createBlockState();
        IProperty[] props = cont.getProperties().toArray(new IProperty[0]);
        props = Arrays.copyOf(props, props.length+2);
        props[props.length-2] = size_property;
        props[props.length-1] = IEProperties.MULTIBLOCKSLAVE;
        return new BlockStateContainer(this, props);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof IEBlockInterfaces.IHasDummyBlocks) {
            state = applyProperty(state, IEProperties.MULTIBLOCKSLAVE, ((IEBlockInterfaces.IHasDummyBlocks)tile).isDummy());
        }
        if (tile instanceof TileEntityJacobsLadder) {
            state = applyProperty(state, size_property, ((TileEntityJacobsLadder) tile).size);
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
        return super.getStateFromMeta(meta).withProperty(size_property, TileEntityJacobsLadder.LadderSize.values()[meta]);
    }

    @Override
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
        for (int i = 0;i< TileEntityJacobsLadder.LadderSize.values().length;i++) {
            list.add(new ItemStack(this, 1, i));
        }
    }

    @Override
    public Object[] getValues() {
        return TileEntityJacobsLadder.LadderSize.values();
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
    public boolean isVisuallyOpaque() {
        return false;
    }
}

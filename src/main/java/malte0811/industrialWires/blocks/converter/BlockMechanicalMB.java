package malte0811.industrialWires.blocks.converter;

import blusunrize.immersiveengineering.api.IEProperties;
import malte0811.industrialWires.blocks.BlockIWMultiblock;
import malte0811.industrialWires.blocks.IMetaEnum;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockMechanicalMB extends BlockIWMultiblock implements IMetaEnum {
    public static final PropertyEnum<MechanicalMBBlockType> TYPE = PropertyEnum.create("type", MechanicalMBBlockType.class);
    public static final String NAME = "mech_mb";
    public BlockMechanicalMB() {
        super(Material.IRON, NAME);

    }

    @Override
    protected IProperty[] getProperties() {
        return new IProperty[] {
                IEProperties.FACING_HORIZONTAL, TYPE
        };
    }

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		return new TileEntityMultiblockConverter();
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
}

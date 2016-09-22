package malte0811.industrialWires.blocks;

import java.util.Arrays;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.energy.wires.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.common.blocks.BlockIETileProvider;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import malte0811.industrialWires.IndustrialWires;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class BlockIC2Connector extends BlockIETileProvider<BlockTypes_IC2_Connector> {

	public BlockIC2Connector() {
		super("ic2Connector", Material.IRON, PropertyEnum.create("type", BlockTypes_IC2_Connector.class), ItemBlockIEBase.class, IEProperties.FACING_ALL);
		setHardness(3.0F);
		setResistance(15.0F);
		lightOpacity = 0;
		setAllNotNormalBlock();
		setBlockLayer(BlockRenderLayer.SOLID, BlockRenderLayer.TRANSLUCENT);
		this.setCreativeTab(IndustrialWires.creativeTab);
	}
	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn) {
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof TileEntityIC2ConnectorTin) {
			TileEntityIC2ConnectorTin connector = (TileEntityIC2ConnectorTin) te;
			if(world.isAirBlock(pos.offset(connector.f))) {
				this.dropBlockAsItem(connector.getWorld(), pos, world.getBlockState(pos), 0);
				connector.getWorld().setBlockToAir(pos);
			}
		}
	}
	@Override
	protected BlockStateContainer createBlockState() {
		BlockStateContainer base = super.createBlockState();
		IUnlistedProperty<?>[] unlisted = (base instanceof ExtendedBlockState) ? ((ExtendedBlockState) base).getUnlistedProperties().toArray(new IUnlistedProperty[0]) : new IUnlistedProperty[0];
		unlisted = Arrays.copyOf(unlisted, unlisted.length+1);
		unlisted[unlisted.length-1] = IEProperties.CONNECTIONS;
		return new ExtendedBlockState(this, base.getProperties().toArray(new IProperty[0]), unlisted);
	}
	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		state = super.getExtendedState(state, world, pos);
		if(state instanceof IExtendedBlockState) {
			IExtendedBlockState ext = (IExtendedBlockState) state;
			TileEntity te = world.getTileEntity(pos);
			if (!(te instanceof TileEntityImmersiveConnectable))
				return state;
			state = ext.withProperty(IEProperties.CONNECTIONS, ((TileEntityImmersiveConnectable)te).genConnBlockstate());
		}
		return state;
	}
	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return false;
	}
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		if (meta==0)
			return new TileEntityIC2ConnectorTin(false);
		else if (meta==1)
			return new TileEntityIC2ConnectorTin(true);
		else if (meta==2)
			return new TileEntityIC2ConnectorCopper(false);
		else if (meta==3)
			return new TileEntityIC2ConnectorCopper(true);
		else if (meta==4)
			return new TileEntityIC2ConnectorGold(false);
		else if (meta==5)
			return new TileEntityIC2ConnectorGold(true);
		else if (meta==6)
			return new TileEntityIC2ConnectorHV(false);
		else if (meta==7)
			return new TileEntityIC2ConnectorHV(true);
		else if (meta==8)
			return new TileEntityIC2ConnectorGlass(false);
		else if (meta==9)
			return new TileEntityIC2ConnectorGlass(true);
		
		return null;
	}
	@Override
	public String createRegistryName() {
		return IndustrialWires.MODID+":"+name;
	}
}

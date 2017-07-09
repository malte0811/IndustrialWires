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
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.util.Utils;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.util.MiscUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Set;

public abstract class BlockIWBase extends Block {
	private IProperty[] properties;

	public BlockIWBase(Material mat, String name) {
		super(mat);
		setHardness(3.0F);
		setResistance(15.0F);
		setUnlocalizedName(IndustrialWires.MODID + "." + name);
		setRegistryName(IndustrialWires.MODID, name);
		setCreativeTab(IndustrialWires.creativeTab);
		IndustrialWires.blocks.add(this);
	}

	public ItemBlock createItemBlock() {
		return new ItemBlockIW(this);
	}

	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
		if (properties == null) {
			properties = getProperties();
		}
		BlockStateContainer cont = super.createBlockState();
		IProperty<?>[] props = cont.getProperties().toArray(new IProperty[0]);
		int oldLength = props.length;
		props = Arrays.copyOf(props, oldLength + properties.length);
		System.arraycopy(properties, 0, props, oldLength, properties.length);
		return new BlockStateContainer(this, props);
	}

	@Nonnull
	@Override
	public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		state = super.getActualState(state, worldIn, pos);
		TileEntity tile = worldIn.getTileEntity(pos);
		if (tile instanceof IHasDummyBlocksIW) {
			state = applyProperty(state, IEProperties.MULTIBLOCKSLAVE, ((IHasDummyBlocksIW) tile).isDummy());
		}
		if (tile instanceof IEBlockInterfaces.IDirectionalTile && ((IEBlockInterfaces.IDirectionalTile) tile).getFacingLimitation() >= 0) {
			if (((IEBlockInterfaces.IDirectionalTile) tile).getFacingLimitation() == 2) {
				state = state.withProperty(IEProperties.FACING_HORIZONTAL, ((IEBlockInterfaces.IDirectionalTile) tile).getFacing());
			} else {
				state = state.withProperty(IEProperties.FACING_ALL, ((IEBlockInterfaces.IDirectionalTile) tile).getFacing());
			}
		}
		return state;
	}

	@Nonnull
	@Override
	public IBlockState getExtendedState(@Nonnull IBlockState state, IBlockAccess world, BlockPos pos) {
		state = super.getExtendedState(state, world, pos);
		if (state instanceof IExtendedBlockState) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof IImmersiveConnectable) {
				Set<ImmersiveNetHandler.Connection> conns = ImmersiveNetHandler.INSTANCE.getConnections(te.getWorld(), pos);
				state = ((IExtendedBlockState) state).withProperty(IEProperties.CONNECTIONS, MiscUtils.genConnBlockstate(conns, te.getWorld()));
			}
		}
		return state;
	}

	protected <V extends Comparable<V>> IBlockState applyProperty(IBlockState in, IProperty<V> prop, V val) {
		return in.withProperty(prop, val);
	}

	@Override
	public void breakBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
		TileEntity te = worldIn.getTileEntity(pos);
		if (te instanceof IHasDummyBlocksIW) {
			((IHasDummyBlocksIW) te).breakDummies();
		}
		if (te instanceof IImmersiveConnectable) {
			if (!worldIn.isRemote || !Minecraft.getMinecraft().isSingleplayer())
				ImmersiveNetHandler.INSTANCE.clearAllConnectionsFor(Utils.toCC(te), worldIn, !worldIn.isRemote && worldIn.getGameRules().getBoolean("doTileDrops"));
		}
		super.breakBlock(worldIn, pos, state);
		worldIn.removeTileEntity(pos);
	}

	@Nonnull
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		TileEntity te = source.getTileEntity(pos);
		if (te instanceof IBlockBoundsIW) {
			AxisAlignedBB ret = ((IBlockBoundsIW) te).getBoundingBox();
			if (ret != null) {
				return ret;
			}
		}
		return super.getBoundingBox(state, source, pos);
	}

	//mostly copied from IE
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
									EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileEntity te = world.getTileEntity(pos);
		ItemStack heldItem = player.getHeldItem(hand);
		if (te instanceof IEBlockInterfaces.IDirectionalTile && Utils.isHammer(heldItem) && !world.isRemote) {
			IEBlockInterfaces.IDirectionalTile directionalTe = (IEBlockInterfaces.IDirectionalTile) te;
			if (directionalTe.canHammerRotate(side, hitX, hitY, hitZ, player)) {
				EnumFacing f = directionalTe.getFacing();
				final EnumFacing original = f;
				int limit = directionalTe.getFacingLimitation();

				if (limit == 0) {
					f = EnumFacing.VALUES[(f.ordinal() + 1) % EnumFacing.VALUES.length];
				} else if (limit == 1) {
					f = player.isSneaking() ? f.rotateAround(side.getAxis()).getOpposite() : f.rotateAround(side.getAxis());
				} else if (limit == 2 || limit == 5) {
					f = player.isSneaking() ? f.rotateYCCW() : f.rotateY();
				}
				if (f != original) {
					directionalTe.setFacing(f);
					te.markDirty();
					world.notifyBlockUpdate(pos, state, state, 3);
					world.addBlockEvent(pos, this, 255, 0);
				}
				return true;
			}
		} else if (te instanceof IEBlockInterfaces.IPlayerInteraction) {
			if (((IEBlockInterfaces.IPlayerInteraction) te).interact(side, player, hand, heldItem, hitX, hitY, hitZ)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean eventReceived(IBlockState state, World worldIn, BlockPos pos, int id, int param) {
		boolean def = super.eventReceived(state, worldIn, pos, id, param);
		if ((id & 255) == 255) {
			IBlockState s = worldIn.getBlockState(pos);
			worldIn.notifyBlockUpdate(pos, s, s, 3);
			return true;
		}
		return def;
	}

	@Override
	public void harvestBlock(@Nonnull World worldIn, EntityPlayer player, @Nonnull BlockPos pos, @Nonnull IBlockState state, TileEntity te, ItemStack stack) {
		if (te instanceof IEBlockInterfaces.ITileDrop) {
			ItemStack drop = ((IEBlockInterfaces.ITileDrop) te).getTileDrop(player, state);
			if (!drop.isEmpty()) {
				spawnAsEntity(worldIn, pos, drop);
				return;
			}
		}
		super.harvestBlock(worldIn, player, pos, state, te, stack);
	}

	@Override
	public boolean rotateBlock(World world, @Nonnull BlockPos pos, @Nonnull EnumFacing axis) {
		return false;
	}

	@Override
	public int damageDropped(IBlockState state) {
		return getMetaFromState(state);
	}

	protected abstract IProperty[] getProperties();
}

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

package malte0811.industrialWires.blocks.hv;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_Connector;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration2;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDevice0;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.IWProperties;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import static blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_Connector.CONNECTOR_HV;
import static blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_Connector.CONNECTOR_REDSTONE;
import static malte0811.industrialWires.blocks.IWProperties.MarxType.*;
import static malte0811.industrialWires.blocks.hv.BlockTypes_HVMultiblocks.MARX;
import static malte0811.industrialWires.util.MiscUtils.offset;

public class MultiblockMarx implements IMultiblock {
	public static final IBlockState[][][] structure = new IBlockState[2][5][5];

	@Override
	public String getUniqueName() {
		return "iw:marx_generator";
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean isBlockTrigger(IBlockState state) {
		return state.getBlock() == IEContent.blockMetalDevice0 && state.getValue(IEContent.blockMetalDevice0.property) == BlockTypes_MetalDevice0.CAPACITOR_HV;
	}
	private EnumFacing facing;
	@SuppressWarnings("unchecked")
	@Override
	public boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player) {
		if (side.getAxis().isVertical()) {
			return false;
		}
		facing = side.rotateY();
		boolean mirrored = false;
		Predicate<BlockPos> hvCap = (local) -> {
			IBlockState b = world.getBlockState(local);
			return b.getBlock() == IEContent.blockMetalDevice0 && b.getValue(IEContent.blockMetalDevice0.property) == BlockTypes_MetalDevice0.CAPACITOR_HV;
		};
		Predicate<BlockPos> heavyEng = (local) -> {
			IBlockState b = world.getBlockState(local);
			IBlockState state = world.getBlockState(local);
			return b.getBlock() == IEContent.blockMetalDecoration0 && b.getValue(IEContent.blockMetalDecoration0.property) == BlockTypes_MetalDecoration0.HEAVY_ENGINEERING;
		};
		Predicate<BlockPos> steelBlock = (local) -> {
			IBlockState b = world.getBlockState(local);
			b = b.getBlock().getActualState(b, world, local);
			ItemStack stack = new ItemStack(b.getBlock(), 1, b.getBlock().getMetaFromState(b));
			return ApiUtils.compareToOreName(stack, "blockSteel");
		};
		BiPredicate<BlockPos, Boolean> wallmount = (local, up) -> {
			IBlockState b = world.getBlockState(local);
			if (b.getBlock()==IEContent.blockMetalDecoration2) {
				b = b.getBlock().getActualState(b, world, local);
				if (b.getValue(IEContent.blockMetalDecoration2.property)== BlockTypes_MetalDecoration2.STEEL_WALLMOUNT) {
					int int_4_wanted = up ? 0 : 1;
					return b.getValue(IEProperties.INT_4)==int_4_wanted;
				}
			}
			return false;
		};
		Predicate<BlockPos> steelFence = (local) -> {
			IBlockState b = world.getBlockState(local);
			b = b.getBlock().getActualState(b, world, local);
			ItemStack stack = new ItemStack(b.getBlock(), 1, b.getBlock().getMetaFromState(b));
			return ApiUtils.compareToOreName(stack, "fenceSteel");
		};
		Function<BlockPos, Byte> hvRelayWith = (local) -> {
			IBlockState state = world.getBlockState(local);
			state = state.getBlock().getActualState(state, world, local);
			if (state.getBlock() != IEContent.blockConnectors) {
				return (byte)-1;
			}
			if (state.getValue(IEContent.blockConnectors.property)!= BlockTypes_Connector.RELAY_HV) {
				return (byte)-1;
			}
			if (state.getValue(IEProperties.FACING_ALL)!=facing) {
				return (byte)-1;
			}
			byte ret = 0;
			Set<Connection> existingConns = ImmersiveNetHandler.INSTANCE.getConnections(world, local);
			if (existingConns==null) {
				return (byte)0;
			}
			for (Connection c:existingConns) {
				if (c.end.equals(local.up())) {
					ret |= 1;
				} else if (c.end.equals(local.down())) {
					ret |= 2;
				} else {
					return (byte) -1;
				}
			}
			return ret;
		};
		BiPredicate<BlockPos, BlockTypes_Connector> connNoConns = (local, type) -> {
			IBlockState state = world.getBlockState(local);
			state = state.getBlock().getActualState(state, world, local);
			if (state.getBlock() != IEContent.blockConnectors) {
				return false;
			}
			if (state.getValue(IEContent.blockConnectors.property)!= type) {
				return false;
			}
			if (state.getValue(IEProperties.FACING_ALL)!=(facing)) {
				return false;
			}
			Set<Connection> existingConns = ImmersiveNetHandler.INSTANCE.getConnections(world, local);
			return existingConns==null||existingConns.isEmpty();
		};

		mirrorLoop:for (int fakeI = 0; fakeI < 2; fakeI++) {
			mirrored = !mirrored;
			// PSU
			if (!connNoConns.test(offset(pos, facing, mirrored, 0, -3, 0), CONNECTOR_REDSTONE)) {
				continue;
			}
			if (!connNoConns.test(offset(pos, facing, mirrored, 1, -3, 0), CONNECTOR_HV)) {
				continue;
			}
			for (int i = 0;i<2;i++) {
				if (!heavyEng.test(offset(pos, facing, mirrored, i, -2, 0))) {
					continue mirrorLoop;
				}
			}
			//Ground discharge electrode
			for (int i = 0;i<4;i++) {
				if (!steelFence.test(offset(pos, facing, mirrored, 0, i+1, 0))) {
					continue mirrorLoop;
				}
			}
			if (!steelBlock.test(offset(pos, facing, mirrored, 1, 4, 0))) {
				continue;
			}
			// stage tower
			int stages = 0;
			while (pos.getY()+stages<=255) {
				boolean end = false;
				byte other = -1;
				for (int right = 0;right<2;right++) {
					if (!hvCap.test(offset(pos, facing, mirrored, right, 0, stages))) {
						continue mirrorLoop;
					}
					if (!wallmount.test(offset(pos, facing, mirrored, right, 1, stages), right!=0)) {
						if (right==0) {
							if (stages!=0) {
								continue mirrorLoop;
							}
						} else {
							end = true;
						}
					}
					byte here = hvRelayWith.apply(offset(pos, facing, mirrored, right, -1, stages));
					if (right==1&&here!=other) {
						continue mirrorLoop;
					}
					if (stages!=0&&(here&2)==0) {
						continue mirrorLoop;
					}
					if (here<=0) {
						continue mirrorLoop;
					}
					if ((here&1)==0) {
						end = true;
					}
					other = here;
				}
				stages++;
				if (end) {
					if (stages>=5) {
						break;
					} else {
						continue mirrorLoop;
					}
				}
			}
			// Top electrode
			for (int i = 0;i<4;i++) {
				if (!steelFence.test(offset(pos, facing, mirrored, 1, i+1, stages-1))) {
					continue mirrorLoop;
				}
			}
			//REPLACE STRUCTURE
			if (!world.isRemote) {
				IBlockState noModel = IndustrialWires.hvMultiblocks.getDefaultState().withProperty(BlockHVMultiblocks.type, MARX)
						.withProperty(IWProperties.MARX_TYPE, NO_MODEL).withProperty(IEProperties.BOOLEANS[0], mirrored);
				IBlockState stageModel = noModel.withProperty(IWProperties.MARX_TYPE, STAGE);
				IBlockState connModel = noModel.withProperty(IWProperties.MARX_TYPE, CONNECTOR);
				// Main tower
				for (int s = 0; s < stages; s++) {
					for (int f = -1; f < 2; f++) {
						for (int r = 0; r < 2; r++) {
							BlockPos p = offset(pos, facing, mirrored, r, f, s);
							if (f==-1) {
								ImmersiveNetHandler.INSTANCE.clearAllConnectionsFor(p, world, false);
							}
							if (f == 0 && r == 0) {
								if (s != 0 && s != stages - 1) {
									set(world, p, stageModel, stages, pos);
								}
							} else {
								set(world, p, noModel, stages, pos);
							}
						}
					}
				}
				//conns
				for (int i = 0; i < 2; i++) {
					set(world, offset(pos, facing, mirrored, i, -3, 0), connModel, stages, pos);
				}
				//bottom electrode
				for (int i = -2;i<5;i++) {
					if (i>-2&&i<2) {
						continue;
					}
					for (int j = 0;j<2;j++) {
						if (j==1&&i>1&&i<4) {
							continue;
						}
						set(world, offset(pos, facing, mirrored, j, i, 0), noModel, stages, pos);
					}
				}
				set(world, pos, noModel.withProperty(IWProperties.MARX_TYPE, BOTTOM), stages, pos);
				set(world, pos.up(stages-1), noModel.withProperty(IWProperties.MARX_TYPE, TOP), stages, pos);
				for (int i = 0;i<3;i++) {
					set(world, offset(pos, facing, mirrored, 1,2+i, stages-1), noModel, stages, pos);
				}
			}
			return true;
		}
		return false;
	}
	private void set(World world, BlockPos p, IBlockState state, int stages, BlockPos origin) {
		world.setBlockState(p, state);
		TileEntity te = world.getTileEntity(p);
		if (te instanceof TileEntityMarx) {
			TileEntityMarx marx = (TileEntityMarx) te;
			marx.offset = p.subtract(origin);
			marx.formed = true;
			marx.setStageCount(stages);
			marx.markDirty();
		}
	}

	@Override
	public ItemStack[][][] getStructureManual() {
		return new ItemStack[0][][];
	}

	@Override
	public IngredientStack[] getTotalMaterials() {
		return new IngredientStack[0];
	}

	@Override
	public boolean overwriteBlockRender(ItemStack stack, int iterator) {
		return false;
	}

	@Override
	public float getManualScale() {
		return 0;
	}

	@Override
	public boolean canRenderFormedStructure() {
		return false;
	}

	@Override
	public void renderFormedStructure() {

	}
}
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

package malte0811.industrialWires.hv;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_Connector;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.IWProperties;
import malte0811.industrialWires.blocks.hv.BlockHVMultiblocks;
import malte0811.industrialWires.blocks.hv.TileEntityMarx;
import malte0811.industrialWires.client.ClientUtilsIW;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import static blusunrize.immersiveengineering.api.IEProperties.*;
import static blusunrize.immersiveengineering.common.IEContent.*;
import static blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsIE.STEEL;
import static blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_Connector.*;
import static blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0.HEAVY_ENGINEERING;
import static blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration1.STEEL_FENCE;
import static blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration2.STEEL_WALLMOUNT;
import static blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDevice0.CAPACITOR_HV;
import static malte0811.industrialWires.blocks.IWProperties.MarxType.*;
import static malte0811.industrialWires.blocks.hv.BlockTypes_HVMultiblocks.MARX;
import static malte0811.industrialWires.util.MiscUtils.offset;

public class MultiblockMarx implements IMultiblock {
	//up forward right
	private static final ItemStack[][][] structureStacks = new ItemStack[5][8][2];
	private static ItemStack rsConnDummy;
	private static ItemStack hvConnDummy;
	private static ItemStack hvRel1Dummy;
	private static ItemStack hvRel0Dummy;
	private static ItemStack wallMountUpDummy;
	private static ItemStack wallMountDownDummy;
	public static MultiblockMarx INSTANCE;

	public MultiblockMarx() {
		if (rsConnDummy == null) {
			rsConnDummy = new ItemStack(Blocks.BRICK_BLOCK);
			hvConnDummy = new ItemStack(Blocks.BRICK_BLOCK);
			hvRel1Dummy = new ItemStack(Blocks.BRICK_BLOCK);
			hvRel0Dummy = new ItemStack(Blocks.BRICK_BLOCK);
			wallMountUpDummy = new ItemStack(Blocks.BRICK_BLOCK);
			wallMountDownDummy = new ItemStack(Blocks.BRICK_BLOCK);
		}
		for (int up = 0; up < 5; up++) {
			structureStacks[up][2][0] = structureStacks[up][2][1] = hvRel1Dummy;
			structureStacks[up][3][0] = structureStacks[up][3][1]
					= new ItemStack(blockMetalDevice0, 1, CAPACITOR_HV.getMeta());
			structureStacks[up][4][0] = wallMountDownDummy;
			structureStacks[up][4][1] = wallMountUpDummy;
			if (up == 0) {
				structureStacks[up][0][0] = rsConnDummy;
				structureStacks[up][0][1] = hvConnDummy;
				structureStacks[up][1][0] = structureStacks[0][1][1]
						= new ItemStack(blockMetalDecoration0, 1, HEAVY_ENGINEERING.getMeta());
				for (int i = 4; i < structureStacks[up].length; i++) {
					structureStacks[up][i][0] = new ItemStack(IEContent.blockMetalDecoration1, 1, STEEL_FENCE.getMeta());
				}
				structureStacks[up][structureStacks[0].length - 1][1] = new ItemStack(blockStorage, 1, STEEL.getMeta());
			} else if (up == 4) {
				structureStacks[up][2][0] = structureStacks[up][2][1] = hvRel0Dummy;
				for (int i = 4; i < structureStacks[up].length; i++) {
					structureStacks[up][i][1] = new ItemStack(IEContent.blockMetalDecoration1, 1, STEEL_FENCE.getMeta());
				}
			}
		}
	}


	@Override
	public String getUniqueName() {
		return "iw:marx_generator";
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean isBlockTrigger(IBlockState state) {
		return state.getBlock() == blockMetalDevice0 && state.getValue(blockMetalDevice0.property) == CAPACITOR_HV;
	}

	@Override
	public ItemStack[][][] getStructureManual() {
		return structureStacks;
	}

	@Override
	public IngredientStack[] getTotalMaterials() {
		return new IngredientStack[] {
				new IngredientStack(new ItemStack(blockMetalDevice0, 10, CAPACITOR_HV.getMeta())),
				new IngredientStack(new ItemStack(blockMetalDecoration0, 2, HEAVY_ENGINEERING.getMeta())),
				new IngredientStack(new ItemStack(blockConnectors, 1, CONNECTOR_HV.getMeta())),
				new IngredientStack(new ItemStack(blockConnectors, 1, CONNECTOR_REDSTONE.getMeta())),
				new IngredientStack(new ItemStack(blockConnectors, 10, RELAY_HV.getMeta())),
				new IngredientStack(new ItemStack(itemWireCoil, 8, 2)),
				new IngredientStack(new ItemStack(blockMetalDecoration2, 8, STEEL_WALLMOUNT.getMeta())),
				new IngredientStack("fenceSteel", 8),
				new IngredientStack("blockSteel", 1)
		};
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
			return b.getBlock() == blockMetalDevice0 && b.getValue(blockMetalDevice0.property) == CAPACITOR_HV;
		};
		Predicate<BlockPos> heavyEng = (local) -> {
			IBlockState b = world.getBlockState(local);
			b = b.getActualState(world, local);
			return b.getBlock() == blockMetalDecoration0 && b.getValue(blockMetalDecoration0.property) == HEAVY_ENGINEERING;
		};
		Predicate<BlockPos> steelBlock = (local) -> {
			IBlockState b = world.getBlockState(local);
			b = b.getActualState(world, local);
			ItemStack stack = new ItemStack(b.getBlock(), 1, b.getBlock().getMetaFromState(b));
			return ApiUtils.compareToOreName(stack, "blockSteel");
		};
		BiPredicate<BlockPos, Boolean> wallmount = (local, up) -> {
			IBlockState b = world.getBlockState(local);
			if (b.getBlock()==IEContent.blockMetalDecoration2) {
				b = b.getActualState(world, local);
				if (b.getValue(IEContent.blockMetalDecoration2.property)== STEEL_WALLMOUNT) {
					int int_4_wanted = up ? 0 : 1;
					return b.getValue(IEProperties.INT_4)==int_4_wanted;
				}
			}
			return false;
		};
		Predicate<BlockPos> steelFence = (local) -> {
			IBlockState b = world.getBlockState(local);
			b = b.getActualState(world, local);
			ItemStack stack = new ItemStack(b.getBlock(), 1, b.getBlock().getMetaFromState(b));
			return ApiUtils.compareToOreName(stack, "fenceSteel");
		};
		Function<BlockPos, Byte> hvRelayWith = (local) -> {
			IBlockState state = world.getBlockState(local);
			state = state.getActualState(world, local);
			if (state.getBlock() != IEContent.blockConnectors) {
				return (byte)-1;
			}
			if (state.getValue(IEContent.blockConnectors.property)!= BlockTypes_Connector.RELAY_HV) {
				return (byte)-1;
			}
			if (state.getValue(FACING_ALL)!=facing) {
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
			state = state.getActualState(world, local);
			if (state.getBlock() != IEContent.blockConnectors) {
				return false;
			}
			if (state.getValue(IEContent.blockConnectors.property)!= type) {
				return false;
			}
			if (state.getValue(FACING_ALL)!=(facing)) {
				return false;
			}
			Set<Connection> existingConns = ImmersiveNetHandler.INSTANCE.getConnections(world, local);
			return existingConns==null||existingConns.isEmpty();
		};

		mirrorLoop:for (int fakeI = 0; fakeI < 2; fakeI++) {
			mirrored = !mirrored;
			facing = facing.getOpposite();

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
				IBlockState noModel = IndustrialWires.hvMultiblocks.getDefaultState().withProperty(FACING_HORIZONTAL, facing).withProperty(BlockHVMultiblocks.type, MARX)
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

	@SuppressWarnings("unchecked")
	@Override
	public IBlockState getBlockstateFromStack(int index, ItemStack stack) {
		IBlockState connBase = blockConnectors.getDefaultState().withProperty(FACING_ALL, EnumFacing.EAST);
		IBlockState mountBase = blockMetalDecoration2.getDefaultState().withProperty(FACING_ALL, EnumFacing.WEST)
				.withProperty(blockMetalDecoration2.property, STEEL_WALLMOUNT);
		if (stack == rsConnDummy) {
			return connBase.withProperty(blockConnectors.property, CONNECTOR_REDSTONE);
		} else if (stack == hvConnDummy) {
			return connBase.withProperty(blockConnectors.property, CONNECTOR_HV);
		} else if (stack == hvRel0Dummy || stack == hvRel1Dummy) {
			return connBase.withProperty(blockConnectors.property, RELAY_HV);
		} else if (stack == wallMountDownDummy) {
			return mountBase.withProperty(INT_4, 1);
		} else if (stack == wallMountUpDummy) {
			return mountBase.withProperty(INT_4, 0);
		}
		return index==-1?null:IMultiblock.super.getBlockstateFromStack(index, stack);
	}

	@Override
	public boolean overwriteBlockRender(ItemStack stack, int iterator) {
		IBlockState here = getBlockstateFromStack(-1, stack);
		if (stack == hvRel1Dummy) {
			//Based on ClientUtils.tessellateConnection
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buffer = tessellator.getBuffer();
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			double radius = WireType.STEEL.getRenderDiameter()/2;
			int c = WireType.STEEL.getColour(null);
			int[] rgba = {
					c&255,
					(c>>8)&255,
					(c>>16)&255,
					(c>>24)&255
			};
			TextureAtlasSprite tex = WireType.STEEL.getIcon(null);
			double uMin = tex.getMinU();
			double uMax = tex.getMaxU();
			double vMin = tex.getMinV();
			double vMax = tex.getMaxV();
			buffer.setTranslation(.125, .5, .5);
			buffer.pos(- radius, 0, 0).tex(uMin, vMin).color(rgba[0], rgba[1], rgba[2], 255).endVertex();
			buffer.pos(- radius, 1, 0).tex(uMax, vMin).color(rgba[0], rgba[1], rgba[2], 255).endVertex();
			buffer.pos(+ radius, 1, 0).tex(uMax, vMax).color(rgba[0], rgba[1], rgba[2], 255).endVertex();
			buffer.pos(+ radius, 0, 0).tex(uMin, vMax).color(rgba[0], rgba[1], rgba[2], 255).endVertex();

			buffer.pos(- radius, 1, 0).tex(uMax, vMin).color(rgba[0], rgba[1], rgba[2], 255).endVertex();
			buffer.pos(- radius, 0, 0).tex(uMin, vMin).color(rgba[0], rgba[1], rgba[2], 255).endVertex();
			buffer.pos(+ radius, 0, 0).tex(uMin, vMax).color(rgba[0], rgba[1], rgba[2], 255).endVertex();
			buffer.pos(+ radius, 1, 0).tex(uMax, vMax).color(rgba[0], rgba[1], rgba[2], 255).endVertex();


			buffer.pos(0, 0, - radius).tex(uMin, vMin).color(rgba[0], rgba[1], rgba[2], 255).endVertex();
			buffer.pos(0, 1, - radius).tex(uMax, vMin).color(rgba[0], rgba[1], rgba[2], 255).endVertex();
			buffer.pos(0, 1, + radius).tex(uMax, vMax).color(rgba[0], rgba[1], rgba[2], 255).endVertex();
			buffer.pos(0, 0, + radius).tex(uMin, vMax).color(rgba[0], rgba[1], rgba[2], 255).endVertex();

			buffer.pos(0, 1, - radius).tex(uMax, vMin).color(rgba[0], rgba[1], rgba[2], 255).endVertex();
			buffer.pos(0, 0, - radius).tex(uMin, vMin).color(rgba[0], rgba[1], rgba[2], 255).endVertex();
			buffer.pos(0, 0, + radius).tex(uMin, vMax).color(rgba[0], rgba[1], rgba[2], 255).endVertex();
			buffer.pos(0, 1, + radius).tex(uMax, vMax).color(rgba[0], rgba[1], rgba[2], 255).endVertex();
			buffer.setTranslation(0, 0, 0);
			tessellator.draw();
		}
		if (here!=null&&IndustrialWires.isOldIE) {
			BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
			IBakedModel model = dispatcher.getModelForState(here);
			GlStateManager.disableBlend();
			ForgeHooksClient.setRenderLayer(BlockRenderLayer.SOLID);
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buffer = tessellator.getBuffer();
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
			ClientUtilsIW.renderModelTESRFast(model.getQuads(here, null, 13), buffer);
			tessellator.draw();
			GlStateManager.enableBlend();
			ForgeHooksClient.setRenderLayer(null);
			return true;
		}
		return false;
	}

	@Override
	public float getManualScale() {
		return 12;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderFormedStructure() {
		return true;
	}

	private List<BakedQuad> bottom = null;
	private List<BakedQuad> stage = null;
	private List<BakedQuad> top = null;
	@Override
	@SideOnly(Side.CLIENT)
	public void renderFormedStructure() {
		BlockRendererDispatcher disp = Minecraft.getMinecraft().getBlockRendererDispatcher();
		if (bottom==null) {
			IBlockState base = IndustrialWires.hvMultiblocks.getStateFromMeta(0);
			BlockModelShapes shapes = disp.getBlockModelShapes();
			base = base.withProperty(IWProperties.MARX_TYPE, BOTTOM);
			bottom = shapes.getModelForState(base).getQuads(base, null, 0);
			base = base.withProperty(IWProperties.MARX_TYPE, STAGE);
			stage = shapes.getModelForState(base).getQuads(base, null, 0);
			base = base.withProperty(IWProperties.MARX_TYPE, TOP);
			top = shapes.getModelForState(base).getQuads(base, null, 0);
		}
		GlStateManager.translate(1.5, 1.5, 2.5);
		GlStateManager.rotate(-90, 0, 1, 0);
		Tessellator tes = Tessellator.getInstance();
		BufferBuilder buf = tes.getBuffer();
		buf.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		ClientUtilsIW.renderModelTESRFast(bottom, buf);
		for (int i = 1;i<4;i++) {
			buf.setTranslation(0, i, 0);
			ClientUtilsIW.renderModelTESRFast(stage, buf);
		}
		buf.setTranslation(0, 4, 0);
		ClientUtilsIW.renderModelTESRFast(top, buf);
		buf.setTranslation(0, 0, 0);
		tes.draw();
	}
}
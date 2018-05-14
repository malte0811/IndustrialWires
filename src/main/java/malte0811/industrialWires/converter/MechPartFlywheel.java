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

package malte0811.industrialWires.converter;

import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.converter.MechanicalMBBlockType;
import malte0811.industrialWires.util.LocalSidedWorld;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BakedQuadRetextured;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import java.util.List;
import java.util.stream.Collectors;

import static net.minecraft.util.math.BlockPos.ORIGIN;

public class MechPartFlywheel extends MechMBPart {
	private static final double RADIUS = 1.25;
	private static final double THICKNESS = 1;
	private static final double VOLUME = Math.PI*RADIUS*RADIUS*THICKNESS;
	private Material material;
	//A flywheel simply adds mass (lots of mass!), it doesn't actively change speeds/energy
	@Override
	public void createMEnergy(MechEnergy e) {}

	@Override
	public double requestMEnergy(MechEnergy e) {
		return 0;
	}

	@Override
	public void insertMEnergy(double added) {}

	@Override
	public double getInertia() {
		return .5*material.density*VOLUME*RADIUS*RADIUS;
	}

	@Override
	public double getMaxSpeed() {
		return Math.sqrt(material.tensileStrength /material.density)/RADIUS;
	}

	@Override
	public void writeToNBT(NBTTagCompound out) {
		out.setInteger("material", material.ordinal());
	}

	@Override
	public void readFromNBT(NBTTagCompound in) {
		material = Material.values()[in.getInteger("material")];
	}

	@Override
	public ResourceLocation getRotatingBaseModel() {
		return new ResourceLocation(IndustrialWires.MODID, "block/mech_mb/flywheel.obj");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public List<BakedQuad> getRotatingQuads() {
		List<BakedQuad> orig = super.getRotatingQuads();
		TextureAtlasSprite newTex = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(material.blockTexture.toString());
		return orig.stream().map((quad)->{
			if (quad.getSprite().getIconName().contains("steel")) {
				return new BakedQuadRetextured(quad, newTex);
			} else {
				return quad;
			}
		}).collect(Collectors.toList());
	}

	@Override
	public boolean canForm(LocalSidedWorld w) {
		BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain(-1, 1, 0);
		try {
			material = null;
			for (Material m:Material.values()) {
				if (m.matchesBlock(w, pos, "block")) {
					material = m;
					break;
				}
			}
			if (material==null) {
				return false;
			}
			for (int x = -1;x<=1;x++) {
				for (int y = -1;y<=1;y++) {
					pos.setPos(x, y, 0);
					if ((x!=0||y!=0)&&!material.matchesBlock(w, pos, "block")) {
						return false;
					}
				}
			}
			pos.setPos(0, 0, 0);
			return isValidDefaultCenter(w.getBlockState(pos));
		} finally {
			pos.release();
		}
	}

	@Override
	public IBlockState getOriginalBlock(BlockPos pos) {
		if (pos.equals(ORIGIN)) {
			return getDefaultShaft();
		}
		for (ItemStack block: OreDictionary.getOres("block"+material.oreName())) {
			if (block.getItem() instanceof ItemBlock) {
				ItemBlock ib = (ItemBlock) block.getItem();
				return ib.getBlock().getStateFromMeta(block.getMetadata());
			}
		}
		return Blocks.AIR.getDefaultState();
	}

	@Override
	public void breakOnFailure(MechEnergy energy) {
		world.setBlockState(ORIGIN, getDefaultShaft());
		IBlockState state = Blocks.AIR.getDefaultState();
		for (int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
				if (x != 0 || y != 0) {
					world.setBlockState(new BlockPos(x, y, 0), state);
				}
			}
		}
		spawnBrokenParts(8, energy, material.blockTexture);
	}

	@Override
	public short getFormPattern(int offset) {
		return 0b111_111_111;
	}

	@Override
	public MechanicalMBBlockType getType() {
		return MechanicalMBBlockType.FLYWHEEL;
	}

	@Override
	public AxisAlignedBB getBoundingBox(BlockPos offsetPart) {
		if (ORIGIN.equals(offsetPart)) {
			return Block.FULL_BLOCK_AABB;
		}
		final double small = .375;
		double xMin = offsetPart.getX()<=0?0:small;
		double xMax = offsetPart.getX()>=0?1:1-small;
		double yMin = offsetPart.getY()>=0?0:small;
		double yMax = offsetPart.getY()<=0?1:1-small;
		return new AxisAlignedBB(xMin, yMin, .0625, xMax, yMax, .9375);
	}
}

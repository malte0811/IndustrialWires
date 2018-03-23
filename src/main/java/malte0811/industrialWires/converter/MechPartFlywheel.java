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

import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.converter.MechanicalMBBlockType;
import malte0811.industrialWires.entities.EntityBrokenPart;
import malte0811.industrialWires.util.LocalSidedWorld;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.oredict.OreDictionary;

import java.util.List;
import java.util.stream.Collectors;

import static blusunrize.immersiveengineering.common.IEContent.blockMetalDecoration0;
import static blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0.LIGHT_ENGINEERING;

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
	public void disassemble(boolean failed, MechEnergy energy) {
		world.setBlockState(BlockPos.ORIGIN,
				blockMetalDecoration0.getDefaultState().withProperty(blockMetalDecoration0.property, LIGHT_ENGINEERING));
		IBlockState state = Blocks.AIR.getDefaultState();
		if (!failed) {
			for (ItemStack block: OreDictionary.getOres("block"+material.oreName())) {
				if (block.getItem() instanceof ItemBlock) {
					ItemBlock ib = (ItemBlock) block.getItem();
					state = ib.getBlock().getStateFromMeta(block.getMetadata());
				}
			}
		}
		for (int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
				if (x != 0 || y != 0) {
					world.setBlockState(new BlockPos(x, y, 0), state);
				}
			}
		}
		if (failed) {
			Matrix4 mat = new Matrix4();
			mat.rotate(Utils.RAND.nextDouble(), 0, 0, 1);
			Vec3d baseVec = new Vec3d(0, 1.5, 0);
			for (int i = 0;i<8;i++) {
				mat.rotate(Math.PI/4, 0, 0, 1);
				Vec3d pos = mat.apply(baseVec);
				EntityBrokenPart e = new EntityBrokenPart(world.getWorld(), material.blockTexture);
				e.setPosition(pos.x, pos.y, .5);
				double speed = (energy.getSpeed()/ getMaxSpeed())/1.5;
				e.motionX = pos.y*speed;
				e.motionY = -pos.x*speed;
				e.motionZ = (Utils.RAND.nextDouble()-.5)*speed/10;
				world.spawnEntity(e);
				e.breakBlocks(speed*speed*1.5*1.5);
			}
		}
	}

	@Override
	public short getFormPattern() {
		return 0b111_111_111;
	}

	@Override
	public MechanicalMBBlockType getType() {
		return MechanicalMBBlockType.FLYWHEEL;
	}
}

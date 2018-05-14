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

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.converter.MechanicalMBBlockType;
import malte0811.industrialWires.util.LocalSidedWorld;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import static blusunrize.immersiveengineering.common.IEContent.blockMetalDecoration0;
import static blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0.COIL_LV;
import static malte0811.industrialWires.converter.Waveform.Phases.get;
import static malte0811.industrialWires.util.NBTKeys.BUFFER_IN;
import static malte0811.industrialWires.util.NBTKeys.BUFFER_OUT;
import static net.minecraft.util.math.BlockPos.ORIGIN;

public class MechPartSingleCoil extends MechMBPart implements IMBPartElectric {
	{
		IBlockState lightEng = getLightEngineering();
		IBlockState coil = getCoil();
		for (int y = -1;y<=1;y++) {
			original.put(new BlockPos(-1, y, 0), lightEng);
			original.put(new BlockPos(0, y, 0), coil);
			original.put(new BlockPos(1, y, 0), lightEng);
		}
		original.put(ORIGIN, getDefaultShaft());
	}

	private double bufferToMech;
	private double bufferToE;

	@Override
	public Waveform getProduced(MechEnergy state) {
		return Waveform.forParameters(Waveform.Type.AC, get(has4Phases()), Waveform.Speed.ROTATION);
	}

	@Override
	public double getAvailableEEnergy() {
		return bufferToE;
	}

	@Override
	public void extractEEnergy(double energy) {
		bufferToE -= energy;
	}

	@Override
	public double requestEEnergy(Waveform waveform, MechEnergy energy) {
		if (has4Phases() ^ waveform.isSinglePhase()) {
			return getMaxBuffer() - bufferToMech;
		}
		return 0;
	}

	@Override
	public void insertEEnergy(double given, Waveform waveform, MechEnergy energy) {
		if (waveform.isDC()) {
			bufferToMech = 0;
		} else {
			bufferToMech += given;
		}
	}

	@Override
	public void createMEnergy(MechEnergy e) {
		e.addEnergy(bufferToMech);
		bufferToMech = 0;
	}

	@Override
	public double requestMEnergy(MechEnergy e) {
		return getMaxBuffer() - bufferToE;
	}

	@Override
	public void insertMEnergy(double added) {
		bufferToE += added;
	}

	@Override
	public double getInertia() {
		return Material.IRON.density + Material.COPPER.density;
	}

	@Override
	public double getMaxSpeed() {
		return 100;
	}

	@Override
	public void writeToNBT(NBTTagCompound out) {
		out.setDouble(BUFFER_IN, bufferToMech);
		out.setDouble(BUFFER_OUT, bufferToE);
	}

	@Override
	public void readFromNBT(NBTTagCompound in) {
		bufferToMech = in.getDouble(BUFFER_IN);
		bufferToE = in.getDouble(BUFFER_OUT);
	}

	@Override
	public ResourceLocation getRotatingBaseModel() {
		return new ResourceLocation(IndustrialWires.MODID, "block/mech_mb/single_coil.obj");
	}

	protected boolean isCoil(IBlockState state) {
		return state.getBlock() == blockMetalDecoration0 &&
				state.getValue(blockMetalDecoration0.property) == BlockTypes_MetalDecoration0.COIL_LV;
	}

	protected IBlockState getCoil() {
		return blockMetalDecoration0.getDefaultState().withProperty(blockMetalDecoration0.property, COIL_LV);
	}

	@Override
	public boolean canForm(LocalSidedWorld w) {
		BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain(0, 0, 0);
		try {
			if (!isValidDefaultCenter(w.getBlockState(pos))) {
				return false;
			}
			pos.setPos(0, 1, 0);
			if (!isCoil(w.getBlockState(pos))) {
				return false;
			}
			pos.setPos(0, -1, 0);
			if (!isCoil(w.getBlockState(pos))) {
				return false;
			}
			for (int i = -1; i <= 1; i+=2) {
				for (int y = -1; y <= 1; y++) {
					pos.setPos(i, y, 0);
					if (!isLightEngineering(w.getBlockState(pos))) {
						return false;
					}
				}
			}
			return true;
		} finally {
			pos.release();//This will run after every return
		}
	}

	@Override
	public short getFormPattern(int offset) {
		return 0b111_111_111;
	}

	protected static final ResourceLocation COIL_TEXTURE = new ResourceLocation(ImmersiveEngineering.MODID,
			"blocks/metal_decoration0_coil_lv_side");
	@Override
	public void breakOnFailure(MechEnergy energy) {
		world.setBlockState(ORIGIN, getDefaultShaft());
		for (int i = -1; i <= 1; i+=2) {
			for (int y = -1; y <= 1; y++) {
				world.setBlockState(new BlockPos(i, y, 0), getLightEngineering());
			}
		}
		spawnBrokenParts(8, energy, COIL_TEXTURE);
	}

	@Override
	public MechanicalMBBlockType getType() {
		return MechanicalMBBlockType.COIL_1_PHASE;
	}
	
	protected double getMaxBuffer() {
		return 10e3;
	}
	
	protected boolean has4Phases() {
		return false;
	}

	@Override
	public AxisAlignedBB getBoundingBox(BlockPos offsetPart) {
		if (BlockPos.ORIGIN.equals(offsetPart)) {
			return Block.FULL_BLOCK_AABB;
		} else {
			return new AxisAlignedBB(0, 0, .0625, 1, 1, .9375);
		}
	}
}

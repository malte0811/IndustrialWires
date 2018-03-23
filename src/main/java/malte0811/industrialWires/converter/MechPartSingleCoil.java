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

import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.converter.MechanicalMBBlockType;
import malte0811.industrialWires.util.LocalSidedWorld;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import static blusunrize.immersiveengineering.common.IEContent.blockMetalDecoration0;
import static blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0.COIL_LV;
import static malte0811.industrialWires.util.NBTKeys.BUFFER_IN;
import static malte0811.industrialWires.util.NBTKeys.BUFFER_OUT;

public class MechPartSingleCoil extends MechMBPart implements IMBPartElectric {
	private double bufferToMech;
	private double bufferToE;

	@Override
	public Waveform getProduced(MechEnergy state) {
		return Waveform.AC_SYNC;
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
			bufferToMech = 0;//TODO something more spectacular
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
		return Double.MAX_VALUE;//TODO I'm fine with shafts having infinite max speed. Not coils though.
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

	protected void setCoil(BlockPos p) {
		world.setBlockState(p, blockMetalDecoration0.getDefaultState().withProperty(blockMetalDecoration0.property, COIL_LV));
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
	public short getFormPattern() {
		return 0b111_111_111;
	}

	@Override
	public void disassemble(boolean failed, MechEnergy energy) {
		setDefaultShaft(BlockPos.ORIGIN);
		if (!failed) {
			for (int i = -1;i<=1;i+=2) {
				setCoil(BlockPos.ORIGIN.up(i));
			}
		}
		for (int i = -1; i <= 1; i+=2) {
			for (int y = -1; y <= 1; y++) {
				setLightEngineering(new BlockPos(i, y, 0));
			}
		}
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
}

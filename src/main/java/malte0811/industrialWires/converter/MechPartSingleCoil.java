/*
 * This file is part of Industrial Wires.
 * Copyright (C) 2016-2017 malte0811
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

import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.converter.MechanicalMBBlockType;
import malte0811.industrialWires.util.LocalSidedWorld;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.util.function.Predicate;

import static malte0811.industrialWires.util.NBTKeys.BUFFER_IN;
import static malte0811.industrialWires.util.NBTKeys.BUFFER_OUT;

public class MechPartSingleCoil extends MechMBPart implements IMBPartElectric {
	private static final double MAX_BUFFER = 10e3;
	private double bufferToMech;
	private double bufferToE;
	@Override
	public Waveform getProduced() {
		return Waveform.AC;
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
	public double requestEEnergy() {
		return MAX_BUFFER- bufferToMech;
	}

	@Override
	public void insertEEnergy(double given) {
		bufferToMech += given;
	}

	@Override
	public void createMEnergy(MechEnergy e) {
		e.addEnergy(bufferToMech);
		bufferToMech = 0;
	}

	@Override
	public double requestMEnergy(MechEnergy e) {
		return MAX_BUFFER-bufferToE;
	}

	@Override
	public void insertMEnergy(double added) {
		bufferToE += added;
	}

	@Override
	public double getInertia() {
		return Material.IRON.density+Material.COPPER.density;
	}

	@Override
	public double getMaxSpeed() {
		return Double.MAX_VALUE;//TODO
	}

	@Override
	public void writeToNBT(NBTTagCompound out) {
		out.setDouble(BUFFER_IN, bufferToMech);
		out.setDouble(BUFFER_OUT, bufferToE);
	}

	@Override
	public void readFromNBT(NBTTagCompound out) {
		bufferToMech = out.getDouble(BUFFER_IN);
		bufferToE = out.getDouble(BUFFER_OUT);
	}

	@Override
	public ResourceLocation getRotatingBaseModel() {
		return new ResourceLocation(IndustrialWires.MODID, "block/mech_mb/single_coil.obj");
	}

	private static final Predicate<IBlockState> IS_COIL = (b)->
			b.getBlock()== IEContent.blockMetalDecoration0&&
					b.getValue(IEContent.blockMetalDecoration0.property)== BlockTypes_MetalDecoration0.COIL_LV;
	@Override
	public boolean canForm(LocalSidedWorld w) {
		BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain(0, 0, 0);
		try {
			if (!isValidCenter(w.getBlockState(pos))) {
				return false;
			}
			pos.setPos(0, 1, 0);
			if (!IS_COIL.test(w.getBlockState(pos))) {
				return false;
			}
			pos.setPos(0, -1, 0);
			if (!IS_COIL.test(w.getBlockState(pos))) {
				return false;
			}
			int offset = 1;
			for (int i = 0; i < 2; i++) {
				for (int y = -1; y <= 1; y++) {
					pos.setPos(offset, y, 0);
					if (!w.isAir(pos)) {
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
	public MechanicalMBBlockType getType() {
		return MechanicalMBBlockType.COIL_1_PHASE;
	}
}

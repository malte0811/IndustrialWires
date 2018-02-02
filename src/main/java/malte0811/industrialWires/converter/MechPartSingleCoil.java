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
import malte0811.industrialWires.util.ConversionUtil;
import malte0811.industrialWires.util.LocalSidedWorld;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.util.function.Predicate;

//TODO electrical side of things. Currently a creative energy source
public class MechPartSingleCoil extends MechMBPart implements IMBPartElectric {
	double inBuffer;
	double outBuffer;
	@Override
	public Waveform getProduced() {
		return Waveform.AC;
	}
	@Override
	public double getAvailableCurrent() {
		return 0;
	}

	@Override
	public double requestCurrent(double total) {
		return 0;
	}

	@Override
	public void consumeCurrent(double given) {

	}

	@Override
	public void produceRotation(MechEnergy e) {
		double rf = 4e3;
		e.addEnergy(rf* ConversionUtil.joulesPerIf());
	}

	@Override
	public double requestEnergy(MechEnergy e) {
		return 0;
	}

	@Override
	public void consumeRotation(double added) {

	}

	@Override
	public double getWeight() {
		return Material.IRON.density+Material.COPPER.density;
	}

	@Override
	public double getMaxSpeed() {
		return Double.MAX_VALUE;
	}

	@Override
	public void writeToNBT(NBTTagCompound out) {

	}

	@Override
	public void readFromNBT(NBTTagCompound out) {

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

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
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import static net.minecraft.util.math.BlockPos.ORIGIN;

public class MechPartFourCoils extends MechPartSingleCoil {
	{
		IBlockState coil = getCoil();
		original.put(new BlockPos(-1, 0, 0), coil);
		original.put(new BlockPos(1, 0, 0), coil);
	}
	@Override
	protected double getMaxBuffer() {
		return 8*super.getMaxBuffer();
	}

	@Override
	protected boolean has4Phases() {
		return true;
	}


	@Override
	public boolean canForm(LocalSidedWorld w) {
		BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain(0, 0, 0);
		try {
			if (!isValidDefaultCenter(w.getBlockState(pos))) {
				return false;
			}
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < 2; j++) {
					pos.setPos(2*i-1, 2*j-1, 0);
					if (!isLightEngineering(w.getBlockState(pos))) {
						return false;
					}
					pos.setPos((j==0)?2*i-1:0, (j!=0)?2*i-1:0, 0);
					if (!isCoil(w.getBlockState(pos))) {
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

	@Override
	public void breakOnFailure(MechEnergy energy) {
		world.setBlockState(ORIGIN, getDefaultShaft());
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				BlockPos pos = new BlockPos(2 * i - 1, 2 * j - 1, 0);
				world.setBlockState(pos, getLightEngineering());
			}
		}
		spawnBrokenParts(8, energy, COIL_TEXTURE);
	}

	@Override
	public MechanicalMBBlockType getType() {
		return MechanicalMBBlockType.COIL_4_PHASE;
	}

	@Override
	public ResourceLocation getRotatingBaseModel() {
		return new ResourceLocation(IndustrialWires.MODID, "block/mech_mb/four_coil.obj");
	}

	@Override
	public double getMaxSpeed() {
		return 500;
	}

}

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

import com.google.common.collect.ImmutableSet;
import malte0811.industrialWires.IWConfig;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.converter.MechanicalMBBlockType;
import malte0811.industrialWires.util.LocalSidedWorld;
import net.minecraft.block.Block;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Set;

import static net.minecraft.util.EnumFacing.EAST;
import static net.minecraft.util.EnumFacing.WEST;

public class MechPartFourElectrodes extends MechPartTwoElectrodes {
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
		return super.canForm(w) && hasSupportPillars(w);

	}

	@Override
	public short getFormPattern() {
		return 0b000_111_101;
	}

	@Override
	public void disassemble(boolean failed, MechEnergy energy) {
		super.disassemble(failed, energy);
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				setLightEngineering(new BlockPos(2*i-1, j-1, 0));
			}
		}
	}

	private static final Set<Pair<BlockPos, EnumFacing>> outputs = ImmutableSet.of(
			new ImmutablePair<>(new BlockPos(1, 0, 0), EAST),
			new ImmutablePair<>(new BlockPos(1, -1, 0), EAST),
			new ImmutablePair<>(new BlockPos(-1, 0, 0), WEST),
			new ImmutablePair<>(new BlockPos(-1, -1, 0), WEST)
	);
	@Override
	public Set<Pair<BlockPos, EnumFacing>> getEnergyConnections() {
		return outputs;
	}

	@Override
	public MechanicalMBBlockType getType() {
		return MechanicalMBBlockType.SHAFT_4_PHASE;
	}

	@Override
	public ResourceLocation getRotatingBaseModel() {
		return new ResourceLocation(IndustrialWires.MODID, "block/mech_mb/shaft4.obj");
	}

	@Override
	public double getMaxSpeed() {
		return IWConfig.MechConversion.allowMBFE()?600:-1;
	}


	@Override
	public AxisAlignedBB getBoundingBox(BlockPos offsetPart) {
		if (BlockPos.ORIGIN.equals(offsetPart)) {
			return super.getBoundingBox(offsetPart);
		}
		if (offsetPart.getY()==0) {
			return Block.FULL_BLOCK_AABB;
		}
		double xMin = offsetPart.getX()<=0?.5:0;
		double xMax = offsetPart.getX()>=0?.5:1;
		return new AxisAlignedBB(xMin, 0, 0, xMax, 1, 1);
	}
}

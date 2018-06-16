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

package malte0811.industrialWires.mech_mb;

import com.google.common.collect.ImmutableList;
import malte0811.industrialWires.IWConfig;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.converter.MechanicalMBBlockType;
import malte0811.industrialWires.util.LocalSidedWorld;
import malte0811.industrialWires.util.MBSideConfig.BlockFace;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.List;

import static blusunrize.immersiveengineering.common.IEContent.blockMetalDecoration0;
import static blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0.GENERATOR;
import static net.minecraft.util.EnumFacing.UP;
import static net.minecraft.util.math.BlockPos.ORIGIN;

public class MechPartTwoElectrodes extends MechPartEnergyIO {
	{
		original.put(ORIGIN, blockMetalDecoration0.getDefaultState().withProperty(
				blockMetalDecoration0.property, GENERATOR));
	}

	@Override
	public ResourceLocation getRotatingBaseModel() {
		return new ResourceLocation(IndustrialWires.MODID, "block/mech_mb/shaft2.obj");
	}

	@Override
	public boolean canForm(LocalSidedWorld w) {
		if (!IWConfig.MechConversion.allowMBFE()) {
			return false;
		}
		IBlockState state = w.getBlockState(ORIGIN);
		return state.getBlock()== blockMetalDecoration0 &&
				state.getValue(blockMetalDecoration0.property)== GENERATOR;
	}

	@Override
	public short getFormPattern(int offset) {
		return 0b000_010_000;
	}

	@Override
	public void breakOnFailure(MechEnergy energy) {
		//NOP
	}

	@Override
	public MechanicalMBBlockType getType() {
		return MechanicalMBBlockType.SHAFT_1_PHASE;
	}

	private static final List<BlockFace> outputs = ImmutableList.of(
			new BlockFace(ORIGIN, UP)
	);
	public List<BlockFace> getEnergyConnections() {
		return outputs;
	}

	protected double getMaxBuffer() {
		return 10e3;//200kW
	}

	protected boolean has4Phases() {
		return false;
	}

	@Override
	public AxisAlignedBB getBoundingBox(BlockPos offsetPart) {
		return new AxisAlignedBB(0, .375, 0, 1, 1, 1);
	}

}

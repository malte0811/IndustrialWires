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

package malte0811.industrialwires.mech_mb;

import com.google.common.collect.ImmutableList;
import malte0811.industrialwires.IWConfig;
import malte0811.industrialwires.IndustrialWires;
import malte0811.industrialwires.blocks.converter.MechanicalMBBlockType;
import malte0811.industrialwires.util.LocalSidedWorld;
import malte0811.industrialwires.util.MBSideConfig.BlockFace;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.List;

import static net.minecraft.util.EnumFacing.EAST;
import static net.minecraft.util.EnumFacing.WEST;

public class MechPartFourElectrodes extends MechPartTwoElectrodes {
	{
		if (areBlocksRegistered()) {
			IBlockState lightEng = getLightEngineering();
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < 2; j++) {
					original.put(new BlockPos(2 * i - 1, j - 1, 0), lightEng);
				}
			}
		}
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
		return super.canForm(w) && hasSupportPillars(w);

	}

	@Override
	public short getFormPattern(int offset) {
		return 0b000_111_101;
	}


	private static final List<BlockFace> outputs = ImmutableList.of(
			new BlockFace(new BlockPos(1, 0, 0), EAST),
			new BlockFace(new BlockPos(1, -1, 0), EAST),
			new BlockFace(new BlockPos(-1, 0, 0), WEST),
			new BlockFace(new BlockPos(-1, -1, 0), WEST)
	);
	@Override
	public List<BlockFace> getEnergyConnections() {
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

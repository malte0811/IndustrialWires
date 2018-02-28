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

import blusunrize.immersiveengineering.common.util.IELogger;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.converter.MechanicalMBBlockType;
import malte0811.industrialWires.util.LocalSidedWorld;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import static blusunrize.immersiveengineering.common.IEContent.blockMetalDecoration0;
import static blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0.LIGHT_ENGINEERING;
import static malte0811.industrialWires.blocks.converter.MechanicalMBBlockType.SHAFT_BASIC;

public class MechPartShaft extends MechMBPart {
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
		return 5;//TODO
	}

	@Override
	public double getMaxSpeed() {
		return Double.MAX_VALUE;
	}

	@Override
	public void writeToNBT(NBTTagCompound out) {}

	@Override
	public void readFromNBT(NBTTagCompound in) {}

	@Override
	public ResourceLocation getRotatingBaseModel() {
		return new ResourceLocation(IndustrialWires.MODID, "block/mech_mb/shaft.obj");
	}

	@Override
	public boolean canForm(LocalSidedWorld world) {
		if (!isValidDefaultCenter(world.getBlockState(BlockPos.ORIGIN)))
			return false;
		BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain(0, 0, 0);
		for (int i = -1;i<=1;i++) {
			for (int j = -1;j<=1;j++) {
				if (j!=0||i!=0) {
					pos.setPos(i, j, 0);
					if (!world.isAir(pos)) {
						pos.release();
						return false;
					}
				}
			}
		}
		pos.release();
		return true;
	}

	@Override
	public short getFormPattern() {
		return 0b000_010_000;
	}

	@Override
	public void disassemble(boolean failed, MechEnergy energy) {
		IELogger.info(world.getOrigin());
		world.setBlockState(BlockPos.ORIGIN,
				blockMetalDecoration0.getDefaultState().withProperty(blockMetalDecoration0.property, LIGHT_ENGINEERING));
	}

	@Override
	public MechanicalMBBlockType getType() {
		return SHAFT_BASIC;
	}
}

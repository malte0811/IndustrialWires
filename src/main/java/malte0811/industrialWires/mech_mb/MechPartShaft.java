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

import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.converter.MechanicalMBBlockType;
import malte0811.industrialWires.util.LocalSidedWorld;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import static malte0811.industrialWires.blocks.converter.MechanicalMBBlockType.SHAFT_BASIC;

public class MechPartShaft extends MechMBPart {
	{
		original.put(BlockPos.ORIGIN, getDefaultShaft());
	}
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
		return 5;
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
		return isValidDefaultCenter(world.getBlockState(BlockPos.ORIGIN));
	}

	@Override
	public short getFormPattern(int offset) {
		return 0b000_010_000;
	}

	@Override
	public void breakOnFailure(MechEnergy energy) {
		disassemble();
	}

	@Override
	public MechanicalMBBlockType getType() {
		return SHAFT_BASIC;
	}

	@Override
	public AxisAlignedBB getBoundingBox(BlockPos offsetPart) {
		return new AxisAlignedBB(.375, .375, 0, .625, .625, 1);
	}
}

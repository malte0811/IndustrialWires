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

import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.converter.MechanicalMBBlockType;
import malte0811.industrialWires.util.LocalSidedWorld;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class MechPartFlywheel extends MechMBPart {
	private static final double VOLUME = 7;//~7 cubic meters
	private Material material;
	//A flywheel simply adds mass (lots of mass!), it doesn't actively change speeds/energy
	@Override
	public void produceRotation(MechEnergy e) {}

	@Override
	public double requestEnergy(MechEnergy e) {
		return 0;
	}

	@Override
	public void consumeRotation(double added) {}

	@Override
	public double getWeight() {
		return .5*material.density*VOLUME;
	}

	@Override
	public double getMaxSpeed() {
		return Double.MAX_VALUE;//material.maxSpeed;
	}

	@Override
	public void writeToNBT(NBTTagCompound out) {
		out.setInteger("material", material.ordinal());
	}

	@Override
	public void readFromNBT(NBTTagCompound out) {
		material = Material.values()[out.getInteger("material")];
	}

	@Override
	public ResourceLocation getRotatingBaseModel() {
		return new ResourceLocation(IndustrialWires.MODID, "block/mech_mb/flywheel.obj");
	}

	@Override
	public boolean canForm(LocalSidedWorld w) {
		BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain(-1, 1, 0);
		try {
			material = null;
			for (Material m:Material.values()) {
				if (m.matchesBlock(w, pos, "block")) {
					material = m;
					break;
				}
			}
			if (material==null) {
				return false;
			}
			for (int x = -1;x<=1;x++) {
				for (int y = -1;y<=1;y++) {
					pos.setPos(x, y, 0);
					if ((x!=0||y!=0)&&!material.matchesBlock(w, pos, "block")) {
						return false;
					}
				}
			}
			pos.setPos(0, 0, 0);
			if (!isValidCenter(w.getBlockState(pos))) {
				return false;
			}
			return true;
		} finally {
			pos.release();
		}
	}

	@Override
	public short getFormPattern() {
		return 0b111_111_111;
	}

	@Override
	public MechanicalMBBlockType getType() {
		return MechanicalMBBlockType.FLYWHEEL;
	}
}

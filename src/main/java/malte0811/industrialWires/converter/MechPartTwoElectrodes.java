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

import malte0811.industrialWires.blocks.converter.MechanicalMBBlockType;
import malte0811.industrialWires.util.LocalSidedWorld;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class MechPartTwoElectrodes extends MechMBPart implements IMBPartElectric {

	@Override
	public Waveform getProduced() {
		return null;
	}

	@Override
	public double getAvailableEEnergy() {
		return 0;
	}

	@Override
	public void extractEEnergy(double energy) {

	}

	@Override
	public double requestEEnergy() {
		return 0;
	}

	@Override
	public void insertEEnergy(double given) {

	}

	@Override
	public void createMEnergy(MechEnergy e) {

	}

	@Override
	public double requestMEnergy(MechEnergy e) {
		return 0;
	}

	@Override
	public void insertMEnergy(double added) {

	}

	@Override
	public double getInertia() {
		return 0;
	}

	@Override
	public double getMaxSpeed() {
		return 0;
	}

	@Override
	public void writeToNBT(NBTTagCompound out) {

	}

	@Override
	public void readFromNBT(NBTTagCompound out) {

	}

	@Override
	public ResourceLocation getRotatingBaseModel() {
		return null;
	}

	@Override
	public boolean canForm(LocalSidedWorld w) {
		return false;
	}

	@Override
	public short getFormPattern() {
		return 0;
	}

	@Override
	public MechanicalMBBlockType getType() {
		return null;
	}
}

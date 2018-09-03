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

package malte0811.industrialwires.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.energy.IEnergyStorage;

import static malte0811.industrialwires.util.NBTKeys.ENERGY;
import static malte0811.industrialwires.util.NBTKeys.VERSION;

public class JouleEnergyStorage implements IEnergyStorage {
	private double energyStored;
	private final double maxStored;
	private final double maxWattIn;
	private final double maxWattOut;

	public JouleEnergyStorage(double energyStored, double maxStored, double maxWattIn, double maxWattOut) {
		this.energyStored = energyStored;
		this.maxStored = maxStored;
		this.maxWattIn = maxWattIn;
		this.maxWattOut = maxWattOut;
	}

	public JouleEnergyStorage(double maxStored, double maxWattIn, double maxWattOut) {
		this(0, maxStored, maxWattIn, maxWattOut);
	}

	public JouleEnergyStorage(double maxStored, double maxWattIO) {
		this(maxStored, maxWattIO, maxWattIO);
	}

	public double getEnergyStoredJ() {
		return energyStored;
	}

	public void setEnergyStoredJ(double energyStored) {
		this.energyStored = energyStored;
	}

	public double getMaxInPerTick() {
		return maxWattIn/20;
	}

	public double getMaxOutPerTick() {
		return maxWattOut/20;
	}
	//conversionFactor*amount is amount in joules
	//returns amount of energy inserted/extracted
	public double insert(double amount, double conversionFactor, boolean simulate) {
		return insert(amount, conversionFactor, simulate, Double.POSITIVE_INFINITY);
	}

	public double insert(double amount, double conversionFactor, boolean simulate, double maximum) {
		double joules = amount*conversionFactor;
		joules = Math.min(joules, getMaxInPerTick());
		joules = Math.min(joules, maxStored-energyStored);
		joules = Math.min(joules, maximum);
		if (!simulate) {
			energyStored += joules;
		}
		return joules/conversionFactor;
	}

	public double extract(double amount, double conversionFactor, boolean simulate) {
		double joules = amount*conversionFactor;
		joules = Math.min(joules, getMaxOutPerTick());
		joules = Math.min(joules, energyStored);
		if (!simulate) {
			energyStored -= joules;
		}
		return joules/conversionFactor;
	}

	public double getRequested(double conversion) {
		return conversion*Math.min(getMaxInPerTick(), maxStored-energyStored);
	}

	@Override
	public int receiveEnergy(int maxReceive, boolean simulate) {
		return MathHelper.ceil(insert(maxReceive, ConversionUtil.joulesPerIf(), simulate));
	}

	@Override
	public int extractEnergy(int maxExtract, boolean simulate) {
		return MathHelper.floor(extract(maxExtract, ConversionUtil.joulesPerIf(), simulate));
	}

	@Override
	public int getEnergyStored() {
		return (int) (ConversionUtil.ifPerJoule()*energyStored);
	}

	@Override
	public int getMaxEnergyStored() {
		return (int) (ConversionUtil.ifPerJoule()*maxStored);
	}

	@Override
	public boolean canExtract() {
		return maxWattOut>0;
	}

	@Override
	public boolean canReceive() {
		return maxWattIn>0;
	}

	public void writeToNbt(NBTTagCompound nbtOuter, String key) {
		NBTTagCompound nbt = key == null ? nbtOuter : new NBTTagCompound();
		nbt.setDouble(ENERGY, energyStored);
		nbt.setByte(VERSION, (byte) 1);
		if (key != null) {
			nbtOuter.setTag(key, nbt);
		}
	}

	public void readFromNBT(NBTTagCompound nbt) {
		byte b = nbt.getByte(VERSION);
		switch (b) {
			case 0://Old EU storage
				setEnergyStoredJ(nbt.getDouble("stored"));
				break;
			case 1:
				setEnergyStoredJ(nbt.getDouble(ENERGY));
				break;
		}
	}
}

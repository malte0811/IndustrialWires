/*
 * This file is part of Industrial Wires.
 * Copyright (C) 2016-2017 malte0811
 *
 * Industrial Wires is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Industrial Wires is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Industrial Wires.  If not, see <http://www.gnu.org/licenses/>.
 */

package malte0811.industrialWires.util;

import net.minecraft.nbt.NBTTagCompound;

public class DualEnergyStorage {
	double storedEU;
	double maxEU;
	double maxOutEU;
	double maxInEU;

	public DualEnergyStorage(double maxEU, double maxInEU, double maxOutEU) {
		this(0, maxEU, maxInEU, maxOutEU);
	}
	public DualEnergyStorage(double storedEU, double maxEU, double maxInEU, double maxOutEU) {
		this.maxEU = maxEU;
		this.maxInEU = maxInEU;
		this.maxOutEU = maxOutEU;
		this.storedEU = storedEU;
	}

	public DualEnergyStorage(double maxEU, double maxIoEU) {
		this(maxEU, maxIoEU, maxIoEU);
	}

	public DualEnergyStorage(double maxEU) {
		this(maxEU, maxEU, maxEU);
	}

	public DualEnergyStorage(int maxIF, int maxInIF, int maxOutIF) {
		this(ConversionUtil.euPerIfIdeal() * maxIF, ConversionUtil.euPerIfIdeal() * maxInIF, ConversionUtil.euPerIfIdeal() * maxOutIF);
	}

	public DualEnergyStorage(int maxIF, int maxIoIF) {
		this(maxIF, maxIoIF, maxIoIF);
	}

	public DualEnergyStorage(int maxIF) {
		this(maxIF, maxIF, maxIF);
	}

	public double extractEU(double extractMax, boolean doExtract) {
		double extr = Math.min(storedEU, extractMax);
		if (doExtract) {
			storedEU -= extr;
		}
		return extr;
	}

	public double extractIF(int extractMax, boolean doExtract) {
		double eu = extractMax * ConversionUtil.euPerIfIdeal();
		return ConversionUtil.ifPerEuIdeal() * extractEU(eu, doExtract);
	}

	public double insertEU(double insertMax, boolean doInsert) {
		double ins = Math.min(insertMax, maxEU - storedEU);
		if (doInsert) {
			storedEU += ins;
		}
		return ins;
	}

	public double insertIF(int insertMax, boolean doInsert) {
		double eu = insertMax * ConversionUtil.euPerIfIdeal();
		return ConversionUtil.ifPerEuIdeal() * insertEU(eu, doInsert);
	}

	public double getEnergyStoredEU() {
		return storedEU;
	}

	public double getMaxStoredEU() {
		return maxEU;
	}

	public double getEnergyStoredIF() {
		return storedEU * ConversionUtil.ifPerEuIdeal();
	}

	public double getMaxStoredIF() {
		return maxEU * ConversionUtil.ifPerEuIdeal();
	}

	public double getEURequested() {
		return Math.min(maxInEU, maxEU - storedEU);
	}

	public void writeToNbt(NBTTagCompound nbtOuter, String key) {
		NBTTagCompound nbt = key==null?nbtOuter:new NBTTagCompound();
		nbt.setDouble("stored", storedEU);
		nbt.setDouble("maxStored", maxEU);
		nbt.setDouble("maxIn", maxInEU);
		nbt.setDouble("maxOut", maxOutEU);
		if (key!=null) {
			nbtOuter.setTag(key, nbt);
		}
	}
	public static DualEnergyStorage readFromNBT(NBTTagCompound nbt) {
		return new DualEnergyStorage(nbt.getDouble("stored"), nbt.getDouble("maxStored"), nbt.getDouble("maxIn"), nbt.getDouble("maxOut"));
	}
}

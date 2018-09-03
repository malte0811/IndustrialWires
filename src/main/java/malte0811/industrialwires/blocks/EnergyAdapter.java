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
package malte0811.industrialwires.blocks;

import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxConnection;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxProvider;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxReceiver;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.energy.IEnergyStorage;

public class EnergyAdapter implements IEnergyStorage {
	/**
	 * 2 different copies of the same thing, the TE this adapter is mirroring.
	 * rec and prov are null if the TE does not implement them
	 */
	private IFluxReceiver rec;
	private IFluxProvider prov;

	private EnumFacing dir;

	public EnergyAdapter(IFluxConnection te, EnumFacing f) {
		dir = f;
		if (te instanceof IFluxReceiver) {
			rec = (IFluxReceiver) te;
		}
		if (te instanceof IFluxProvider) {
			prov = (IFluxProvider) te;
		}
	}

	@Override
	public int receiveEnergy(int maxReceive, boolean simulate) {
		if (rec == null) {
			return 0;
		} else {
			return rec.receiveEnergy(dir, maxReceive, simulate);
		}
	}

	@Override
	public int extractEnergy(int maxExtract, boolean simulate) {
		if (prov == null) {
			return 0;
		} else {
			return prov.extractEnergy(dir, maxExtract, simulate);
		}
	}

	@Override
	public int getEnergyStored() {
		if (prov != null) {
			return prov.getEnergyStored(dir);
		} else if (rec != null) {
			return rec.getEnergyStored(dir);
		} else {
			return 0;
		}
	}

	@Override
	public int getMaxEnergyStored() {
		if (prov != null) {
			return prov.getMaxEnergyStored(dir);
		} else if (rec != null) {
			return rec.getMaxEnergyStored(dir);
		} else {
			return 0;
		}
	}

	@Override
	public boolean canExtract() {
		return prov != null;
	}

	@Override
	public boolean canReceive() {
		return rec != null;
	}
}

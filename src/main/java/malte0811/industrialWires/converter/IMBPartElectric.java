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
import malte0811.industrialWires.util.ConversionUtil;
import malte0811.industrialWires.util.LocalSidedWorld;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Set;

public interface IMBPartElectric {
	/**
	 * If a section has more than one waveform (!=NONE) the generators are shorted (or worse), so it will:
	 * 1. Heat up the sources, possibly destroying them (TODO do I want to do that?)
	 * 2. Consume a lot of mechanical energy
	 */
	Waveform getProduced(MechEnergy state);
	// All four in Joules
	double getAvailableEEnergy();
	void extractEEnergy(double energy);
	double requestEEnergy(Waveform waveform, MechEnergy energy);
	void insertEEnergy(double given, Waveform waveform, MechEnergy energy);

	default Set<Pair<BlockPos, EnumFacing>> getEnergyConnections() {
		return ImmutableSet.of();
	}
	default double outputFE(LocalSidedWorld world, int available) {
		double extracted = 0;
		for (Pair<BlockPos, EnumFacing> output : getEnergyConnections()) {
			BlockPos outTE = output.getLeft().offset(output.getRight());
			TileEntity te = world.getTileEntity(outTE);
			EnumFacing sideReal = world.transformedToReal(output.getRight()).getOpposite();
			if (te != null && te.hasCapability(CapabilityEnergy.ENERGY, sideReal)) {
				IEnergyStorage energy = te.getCapability(CapabilityEnergy.ENERGY, sideReal);
				if (energy != null && energy.canReceive()) {
					int received = energy.receiveEnergy(available, false);
					available -= received;
					extracted += ConversionUtil.joulesPerIf() * received;
				}
			}
		}
		return extracted;
	}
}

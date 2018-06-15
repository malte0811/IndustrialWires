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

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class EUCapability {
	@CapabilityInject(IC2EnergyHandler.class)
	public static Capability<IC2EnergyHandler> ENERGY_IC2 = null;

	public static void register() {
		CapabilityManager.INSTANCE.register(IC2EnergyHandler.class, new Capability.IStorage<IC2EnergyHandler>() {
					@Override
					public NBTBase writeNBT(Capability<IC2EnergyHandler> capability, IC2EnergyHandler instance, EnumFacing side) {
						throw new IllegalStateException("Can't serialize EU caps!");
					}

					@Override
					public void readNBT(Capability<IC2EnergyHandler> capability, IC2EnergyHandler instance, EnumFacing side, NBTBase nbt) {
						throw new IllegalStateException("Can't serialize EU caps!");
					}
				},
				IC2EnergyHandlerDummy::new);
	}

	public static abstract class IC2EnergyHandler {
		public int tier;

		public double getDemandedEnergy() {
			return 0;
		}

		public int getEnergyTier() {
			return tier;
		}

		public abstract double injectEnergy(EnumFacing enumFacing, double v, double v1);

		public abstract double getOfferedEnergy();

		public abstract void drawEnergy(double v);
	}

	public static class IC2EnergyHandlerDummy extends IC2EnergyHandler {

		@Override
		public double injectEnergy(EnumFacing enumFacing, double v, double v1) {
			return 0;
		}

		@Override
		public double getOfferedEnergy() {
			return 0;
		}

		@Override
		public void drawEnergy(double v) {

		}
	}
}
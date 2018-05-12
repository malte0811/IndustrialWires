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


import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class MechEnergy {
	private double speed = 0;
	public final double weight;

	public MechEnergy(double weight, double speed) {
		this.weight = weight;
		this.speed = speed;
	}

	public double getEnergy() {
		return .5 * weight * speed * speed;
	}

	public double getSpeed() {
		return speed;
	}

	public void addEnergy(double energy) {
		if (energy <= 0) {
			return;
		}
		double targetEnergy = getEnergy() + energy;
		speed = Math.sqrt(2 * targetEnergy / weight);
	}

	public void extractEnergy(double energy) {
		if (energy <= 0) {
			return;
		}
		double oldEnergy = getEnergy();
		energy = Math.min(energy, oldEnergy);
		speed = Math.sqrt(2 * (oldEnergy - energy) / weight);
	}

	public void decaySpeed(double decay) {
		speed *= decay;
		if (speed < .1)
			speed = 0;
	}

	private static final int TICKS_FOR_ADJUSTMENT = 30;
	private double targetSpeed;
	private double oldSpeed = -1;
	private int ticksTillReached;

	//ONLY USE FOR SYNCING
	@SideOnly(Side.CLIENT)
	public void setTargetSpeed(double speed) {
		targetSpeed = speed;
		oldSpeed = getSpeed();
		ticksTillReached = TICKS_FOR_ADJUSTMENT;
	}

	@SideOnly(Side.CLIENT)
	public boolean clientUpdate() {
		if (ticksTillReached >= 0) {
			speed = ((TICKS_FOR_ADJUSTMENT - ticksTillReached) * targetSpeed +
					ticksTillReached * oldSpeed) / TICKS_FOR_ADJUSTMENT;
			ticksTillReached--;
			return true;
		} else {
			oldSpeed = -1;
			return false;
		}
	}

	@SideOnly(Side.CLIENT)
	public boolean isAdjusting()
	{
		return ticksTillReached>=0;
	}
}
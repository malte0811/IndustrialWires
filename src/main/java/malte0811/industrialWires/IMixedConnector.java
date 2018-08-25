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
package malte0811.industrialWires;

import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import malte0811.industrialWires.util.ConversionUtil;
import malte0811.industrialWires.wires.EnergyType;
import net.minecraft.entity.Entity;

public interface IMixedConnector extends IImmersiveConnectable {
	/**
	 * @return leftover energy.
	 */
	double insertEnergy(double joules, boolean simulate, EnergyType type);

	@Override
	default float getDamageAmount(Entity e, ImmersiveNetHandler.Connection c)
	{
		return (float) Math.ceil(IImmersiveConnectable.super.getDamageAmount(e, c) * ConversionUtil.euPerJoule());//Same as IC2 uses
	}
}

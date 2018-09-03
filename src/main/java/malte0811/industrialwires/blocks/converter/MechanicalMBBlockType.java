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

package malte0811.industrialwires.blocks.converter;

import net.minecraft.util.IStringSerializable;

public enum MechanicalMBBlockType implements IStringSerializable {
    NO_MODEL,
    END,
    OTHER_END,
	COIL_4_PHASE,
	COIL_1_PHASE,
	SHAFT_BASIC,
    SHAFT_4_PHASE,
    SHAFT_1_PHASE,
    SHAFT_COMMUTATOR,
    FLYWHEEL,
	SPEEDOMETER,
	SHAFT_COMMUTATOR_4;
    public static final MechanicalMBBlockType[] VALUES = values();

    @Override
    public String getName() {
        return name().toLowerCase();
    }
}

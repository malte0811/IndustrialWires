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
package malte0811.industrialWires.util;

import blusunrize.immersiveengineering.common.Config;
import malte0811.industrialWires.IWConfig.MechConversion;

public class ConversionUtil {
	private ConversionUtil() {
	}

	public static double rotPerIf() {
		return 1 / Config.IEConfig.Machines.dynamo_output;
	}

	public static double ifPerRot() {
		return Config.IEConfig.Machines.dynamo_output;
	}

	public static double euPerIfIdeal() {
		return MechConversion.euPerIf;
	}

	public static double ifPerEuIdeal() {
		return 1 / MechConversion.euPerIf;
	}

	public static double euPerKin() {
		return 1 / kinPerEu();
	}

	public static double kinPerEu() {
		return MechConversion.kinPerEu;
	}

	public static double kinPerRot() {
		return kinPerEu() * euPerIfIdeal() * ifPerRot();
	}

	public static double rotPerKin() {
		return 1 / kinPerRot();
	}

	public static double joulesPerIf() {
		return MechConversion.joulesPerRF;
	}

	public static double ifPerJoule() {
		return 1/joulesPerIf();
	}

	public static double joulesPerEu() {
		return joulesPerIf()*ifPerEuIdeal();
	}

	public static double euPerJoule() {
		return euPerIfIdeal()*ifPerJoule();
	}
}
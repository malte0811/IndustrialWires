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

import net.minecraft.util.math.Vec3d;

public final class Beziers {
	private Beziers() {
	}

	public static Vec3d getPoint(double t, Vec3d[] controls) {
		if (t == 0) {
			return controls[0];
		} else if (t == 1) {
			return controls[controls.length - 1];
		}
		Vec3d ret = new Vec3d(0, 0, 0);
		int n = controls.length - 1;
		for (int i = 0; i <= n; i++) {
			double coeff = binomialCoeff(n, i) * Math.pow(t, i) * Math.pow(1 - t, n - i);
			ret = ret.addVector(coeff * controls[i].x, coeff * controls[i].y, coeff * controls[i].z);
		}
		return ret;
	}

	public static int binomialCoeff(int n, int k) {
		return factorial(n - k + 1, n) / factorial(2, k);
	}

	public static int factorial(int start, int end) {
		int ret = 1;
		for (int i = start; i <= end; i++) {
			ret *= i;
		}
		return ret;
	}
}

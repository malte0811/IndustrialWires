/*
 * This file is part of Industrial Wires.
 * Copyright (C) 2016-2017 malte0811
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

import javax.annotation.Nullable;

public interface IMBPartElectric {
	/**
	 * If a section has more than one waveform (!=NONE) the generators are shorted (or worse), so it will:
	 * 1. Heat up the sources, possibly destroying them (TODO do I want to do that?)
	 * 2. Consume a lot of mechanical energy
	 */
	Waveform getProduced();
	// This is EU
	double getAvailableCurrent();
	double requestCurrent(double total);
	void consumeCurrent(double given);

	enum Waveform {
		NONE(null, 0, 0),
		AC(true, 1, 2),
		PULSED_DC(false, 2/Math.PI, 1),
		MULTI_AC(true, 1, 4),//"4-phase" AC, magically transported by a single wire
		MULTI_AC_RECT(true, 2*Math.sqrt(2)/Math.PI, 6),
		DC(false, 1, 6),
		SQUARE(true, Math.PI/4, 5);
		@Nullable
		public Boolean isEu;
		public double efficiency;
		private int dualId;
		public Waveform dual;
		Waveform(@Nullable Boolean eu, double efficiency, int dualId) {
			isEu = eu;
			this.efficiency = efficiency;
			this.dualId = dualId;
		}

		public static void init() {
			Waveform[] values = values();
			for (Waveform f:values) {
				f.dual = values[f.dualId];
			}
		}
	}
}

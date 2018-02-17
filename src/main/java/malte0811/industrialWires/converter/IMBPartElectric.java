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
	// All four in Joules
	double getAvailableEEnergy();
	void extractEEnergy(double energy);
	double requestEEnergy(Waveform waveform);
	void insertEEnergy(double given, Waveform waveform);

	enum Waveform {
		NONE(null, 0),
		//Sync/async refers to multiblock rotation speed, not to line frequency
		AC_SYNC(true, 3),
		AC_ASYNC(true, 4) {
			@Override
			public Waveform getCommutated(double speed) {
				if (Math.abs(speed-ASYNC_SPEED)<SYNC_TOLERANCE*ASYNC_SPEED) {
					return DC;
				}
				return super.getCommutated(speed);
			}
		},
		AC_4PHASE(true, 4),//TODO what should this rectify into? If anything at all
		DC(false, 1),
		MESS(null, 4);

		public static final double ASYNC_SPEED = 10;//TODO is this a good value
		public static final double SYNC_TOLERANCE = .1;//TODO is this a good value
		public static final Waveform[] VALUES = values();
		@Nullable
		private Boolean isAC;
		private int dualId;
		public Waveform dual;
		Waveform(@Nullable Boolean ac, int dualId) {
			isAC = ac;
			this.dualId = dualId;
		}

		public static void init() {
			Waveform[] values = values();
			for (Waveform f:values) {
				f.dual = values[f.dualId];
			}
		}

		public Waveform getCommutated(double speed) {
			return dual;
		}

		public boolean isAC() {
			return isAC==Boolean.TRUE;
		}

		public boolean isDC() {
			return isAC==Boolean.FALSE;
		}
	}
}

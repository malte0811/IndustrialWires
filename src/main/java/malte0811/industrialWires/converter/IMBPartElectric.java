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

import javax.annotation.Nullable;

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

	enum Waveform {
		NONE(null, 0, true),
		//Sync/async refers to multiblock rotation speed, not to line frequency
		AC_SYNC(true, 4, true),
		AC_ASYNC(true, 5, 4, true),
		AC_4PHASE_SYNC(true, 4, false),
		AC_4PHASE_ASYNC(true, 4, false),
		DC(false, 1, true) {
			@Override
			public Waveform getCommutated(double speed, boolean fourPhase) {
				if (!fourPhase) {
					return super.getCommutated(speed, false);
				} else {
					if (isSyncSpeed(speed)) {
						return AC_4PHASE_ASYNC;
					} else {
						return AC_4PHASE_SYNC;
					}
				}
			}
		},
		MESS(null, 5, true);

		public static final double EXTERNAL_SPEED = 20;
		public static final double SYNC_TOLERANCE = .1;
		public static final double MIN_COMM_SPEED = 4;
		public static final Waveform[] VALUES = values();
		public static boolean isSyncSpeed(double speed) {
			return Math.abs(speed- EXTERNAL_SPEED)<SYNC_TOLERANCE* EXTERNAL_SPEED;
		}

		@Nullable
		private Boolean isAC;
		boolean single;
		private int dualId, syncDualId;
		public Waveform dual, syncDual;
		Waveform(@Nullable Boolean ac, int dualId, boolean singlePhase) {
			this(ac, dualId, dualId, singlePhase);
		}
		Waveform(@Nullable Boolean ac, int dualId, int syncDual, boolean singlePhase) {
			isAC = ac;
			this.dualId = dualId;
			syncDualId = syncDual;
			single = singlePhase;
		}

		public static void init() {
			for (Waveform f:VALUES) {
				f.dual = VALUES[f.dualId];
				f.syncDual = VALUES[f.syncDualId];
			}
		}

		public Waveform getCommutated(double speed, boolean fourPhase) {
			if (isSyncSpeed(speed)) {
				return syncDual;
			}
			return speed<MIN_COMM_SPEED?this:dual;
		}

		public boolean isAC() {
			return isAC==Boolean.TRUE;
		}

		public boolean isDC() {
			return isAC==Boolean.FALSE;
		}

		public boolean isEnergyWaveform() {
			return isAC!=null;
		}

		public boolean isSinglePhase() {
			return single;
		}
	}
}

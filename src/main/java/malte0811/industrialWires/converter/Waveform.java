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

import static malte0811.industrialWires.converter.Waveform.Phases.SINGLE;
import static malte0811.industrialWires.converter.Waveform.Speed.EXTERNAL;
import static malte0811.industrialWires.converter.Waveform.Speed.ROTATION;
import static malte0811.industrialWires.converter.Waveform.Type.*;

public class Waveform {


	public static final double EXTERNAL_SPEED = 20;
	public static final double SYNC_TOLERANCE = .1;
	public static final double MIN_COMM_SPEED = 4;
	public static final Waveform[] VALUES = new Waveform[Type.VALUES.length*Phases.VALUES.length* Speed.VALUES.length];

	public static boolean isSyncSpeed(double speed) {
		return Math.abs(speed - EXTERNAL_SPEED) < SYNC_TOLERANCE * EXTERNAL_SPEED;
	}

	static {
		for (Type t:Type.VALUES) {
			for (Phases p:Phases.VALUES) {
				for (Speed s: Speed.VALUES) {
					VALUES[getIndex(t, p, s)] = new Waveform(t, p, s);
				}
			}
		}
	}

	public static Waveform forParameters(Type t, Phases p, Speed s) {
		return VALUES[getIndex(t, p, s)];
	}

	private static int getIndex(Type t, Phases p, Speed s) {
		return t.ordinal()* Phases.VALUES.length* Speed.VALUES.length
				+p.ordinal()* Speed.VALUES.length
				+s.ordinal();
	}

	private Type type;
	private Phases phases;
	private Speed speed;

	private Waveform(Type type, Phases phases, Speed speed) {
		this.type = type;
		this.phases = phases;
		this.speed = speed;
	}

	public Waveform getCommutated(double speed, boolean fourPhase) {
		if (speed < MIN_COMM_SPEED) {
			return this;
		}
		Type commType = type;
		if (type==AC) {
			commType = DC;
		} else if (type==DC) {
			commType = AC;
		}
		Speed commSpeed = ROTATION;
		if (isSyncSpeed(speed)) {
			commSpeed = EXTERNAL;
		} else if (type==AC && this.speed ==EXTERNAL) {
			commType = MESS;
		}
		return forParameters(commType, phases, commSpeed);
	}

	public boolean isAC() {
		return type==AC;
	}

	public boolean isDC() {
		return type == DC;
	}

	public boolean isEnergyWaveform() {
		return type!=NONE&&type!=MESS;
	}

	public boolean isSinglePhase() {
		return phases== SINGLE;
	}

	public Waveform getForSpeed(double speed) {
		if (this.speed==ROTATION&&isSyncSpeed(speed)) {
			return forParameters(type, phases, EXTERNAL);
		}
		return this;
	}

	public int getIndex() {
		return getIndex(type, phases, speed);
	}

	public enum Type {
		NONE,
		MESS,//AC commutated at a wrong speed
		AC,
		DC;
		public static final Type[] VALUES = values();
	}

	public enum Phases {
		SINGLE,
		FOUR;
		public static final Phases[] VALUES = values();

		public static Phases get(boolean has4Phases) {
			return has4Phases?FOUR:SINGLE;
		}
	}

	public enum Speed {
		ROTATION,
		EXTERNAL;
		public static final Speed[] VALUES = values();
	}
}

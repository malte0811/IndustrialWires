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

package malte0811.industrialWires.client;

import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;

import java.util.function.Supplier;

public class IWTickableSound extends PositionedSound implements ITickableSound {
	private Supplier<Float> getVolume;
	private Supplier<Float> getPitch;

	protected IWTickableSound(ResourceLocation sound, SoundCategory category,
							  Supplier<Float> getVolume, Supplier<Float> getPitch,
							  float xPosF, float yPosF, float zPosF) {
		super(sound, category);
		this.getVolume = getVolume;
		this.getPitch = getPitch;
		this.repeat = true;
		this.repeatDelay = 0;
		this.xPosF = xPosF;
		this.yPosF = yPosF;
		this.zPosF = zPosF;
	}

	@Override
	public boolean isDonePlaying() {
		return getVolume.get()<=0;
	}

	@Override
	public void update() {
		//NOP
	}

	@Override
	public float getVolume() {
		return getVolume.get();
	}

	@Override
	public float getPitch() {
		return getPitch.get();
	}
}

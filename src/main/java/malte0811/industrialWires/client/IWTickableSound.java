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

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.items.ItemEarmuffs;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import malte0811.industrialWires.IEObjects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

import java.util.function.Supplier;

public class IWTickableSound extends PositionedSound implements ITickableSound {
	private Supplier<Float> getVolume;
	private Supplier<Float> getPitch;

	protected IWTickableSound(SoundEvent sound, SoundCategory category,
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

	//This can be static as it's the same for all sounds
	private static float mod = 1;
	private static long lastCheck = Long.MIN_VALUE;
	private static final int UPDATE_FREQU = 5;
	@Override
	public float getVolume() {
		Minecraft mc = Minecraft.getMinecraft();
		long time = mc.world.getTotalWorldTime();
		// Earmuffs don't work well for long sounds
		// so I adjust the volume manually and blacklist the sounds from the "normal" muffling
		if (time>lastCheck+UPDATE_FREQU) {
			mod = 1;
			lastCheck = time;
			ItemStack earmuffs = mc.player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
			if (ItemNBTHelper.hasKey(earmuffs, Lib.NBT_Earmuffs))
				earmuffs = ItemNBTHelper.getItemStack(earmuffs, Lib.NBT_Earmuffs);
			if (!earmuffs.isEmpty() && IEObjects.itemEarmuffs.equals(earmuffs.getItem()) &&
					!ItemNBTHelper.getBoolean(earmuffs, "IE:Earmuffs:Cat_" + category.getName())) {
				mod = ItemEarmuffs.getVolumeMod(earmuffs);
			}
		}
		return mod*getVolume.get();
	}

	@Override
	public float getPitch() {
		return getPitch.get();
	}
}

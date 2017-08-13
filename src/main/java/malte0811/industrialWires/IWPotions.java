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

package malte0811.industrialWires;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nonnull;

public class IWPotions {
	public static PotionTinnitus tinnitus;
	public static void init() {
		tinnitus = new PotionTinnitus();
	}
	static class PotionTinnitus extends Potion {
		protected PotionTinnitus() {
			super(true, 0xffff0000);
			this.setRegistryName(new ResourceLocation(IndustrialWires.MODID, "tinnitus"));
			ForgeRegistries.POTIONS.register(this);
		}

		@Override
		public boolean isReady(int duration, int amplifier) {
			return true;
		}

		@Override
		public void performEffect(@Nonnull EntityLivingBase affected, int amp) {
			if (affected.getEntityWorld().isRemote) {
				Minecraft mc = Minecraft.getMinecraft();
				if (mc.player==affected) {
					IndustrialWires.proxy.startTinnitus();
				}
			}
		}
	}
}

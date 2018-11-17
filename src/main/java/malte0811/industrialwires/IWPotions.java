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

package malte0811.industrialwires;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class IWPotions {
	public static PotionTinnitus tinnitus;
	public static void init() {
		tinnitus = new PotionTinnitus();
	}
	static class PotionTinnitus extends Potion {
		ResourceLocation tex = new ResourceLocation(IndustrialWires.MODID,"textures/gui/tinnitus.png");
		protected PotionTinnitus() {
			super(true, 0xffff0000);
			setIconIndex(0, 0);
			this.setRegistryName(new ResourceLocation(IndustrialWires.MODID, "tinnitus"));
			ForgeRegistries.POTIONS.register(this);
			this.setPotionName("potion." + IndustrialWires.MODID + "." + getRegistryName().getPath());
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

		@Override
		@SideOnly(Side.CLIENT)
		public int getStatusIconIndex()
		{
			Minecraft.getMinecraft().getTextureManager().bindTexture(tex);
			return super.getStatusIconIndex();
		}
	}
}

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

package malte0811.industrialWires;

import malte0811.industrialWires.hv.MarxOreHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber
public class IWSaveData extends WorldSavedData {
	private final static String MARX_MOD = "marxOreModifier";
	public static IWSaveData INSTANCE = new IWSaveData();

	public IWSaveData() {
		super(IndustrialWires.MODID);
	}

	public IWSaveData(String name) {
		super(name);
	}

	@Override
	public void readFromNBT(@Nonnull NBTTagCompound nbt) {
		if (nbt.hasKey(MARX_MOD)) {
			MarxOreHandler.modifier = nbt.getDouble(MARX_MOD);
		} else {
			MarxOreHandler.resetModifier();
		}
	}

	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
		compound.setDouble(MARX_MOD, MarxOreHandler.modifier);
		return compound;
	}

	@SubscribeEvent
	public static void onWorldLoad(WorldEvent.Load event) {
		World w = event.getWorld();
		if (!w.isRemote) {
			INSTANCE = (IWSaveData) w.loadData(IWSaveData.class, IndustrialWires.MODID);
			if (INSTANCE==null) {
				INSTANCE = new IWSaveData();
				MarxOreHandler.resetModifier();
				w.setData(IndustrialWires.MODID, INSTANCE);
				INSTANCE.setDirty(true);
			}
		}
	}
}

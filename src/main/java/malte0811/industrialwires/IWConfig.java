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

import blusunrize.immersiveengineering.common.Config.IEConfig;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.RequiresMcRestart;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = IndustrialWires.MODID)
@Mod.EventBusSubscriber
public class IWConfig {
	@Comment({"The maximum length of a single connection.", "Order: Tin, Copper, Gold, HV, Glass Fiber"})
	public static int[] maxLengthPerConn = {16, 16, 16, 32, 32};
	@Comment({"The maximum length of wire a coil item.", "Order: Tin, Copper, Gold, HV, Glass Fiber (as above)"})
	public static int[] maxLengthOnCoil = {1024, 1024, 1024, 2048, 2048};
	@Comment({"The factor between the IF transfer rate of the wires and the IF transfer rate corresponding to the EU transfer rate.",
				"The default value results in the same transfer rates as the standard IE wires"})
	public static double wireRatio = .5;
	@Comment({"The EU IO rates of the wires. Order is Tin, Copper, Gold, HV, Glass Fiber"})
	public static double[] ioRatesEU = {32, 128, 512, 2048, 8192};

	@Comment({"Set this to false to completely disable any conversion between IF and EU (default: true)"})
	@RequiresMcRestart
	public static boolean enableConversion = true;

	public static MechConversion mech;
	@Comment({"The highest number of keys that can be put on one key ring"})
	public static int maxKeysOnRing = 10;

	public static class MechConversion {
		@Comment({"The amount of EU that would be produced by an ideal converter from 1 IF (default: 0.25)"})
		public static double euPerIf = .25;
		@Comment({"The amount of IC2 kinetic energy that an ideal converter produces from 1 EU"})
		public static double kinPerEu = 4;

		@Comment({"The maximum amount of IF that can be converted to rotational energy", "by one motor in one tick (default: 100)"})
		@Config.RequiresWorldRestart
		public static int maxIfToMech = 100;
		@Comment({"The efficiency of the IF motor. The default value of 0.9 means that 10% of the energy are lost in the conversion."})
		public static double ifMotorEfficiency = .9;

		@Comment({"The maximum amount of IE rotational energy that can be converted into IC2 kinetic energy", "by one converter in one tick"})
		@Config.RequiresWorldRestart
		public static double maxRotToKin = 200;
		@Comment({"The efficiency of the conversion from IE rotational energy to IC2 kinetic energy"})
		public static double rotToKinEfficiency = .7;

		@Comment({"The maximum amount of IC2 kinetic energy that can be converted into IE rotational energy", "by one converter in one tick"})
		@Config.RequiresWorldRestart
		public static int maxKinToRot = 600;
		@Comment({"The efficiency of the conversion from IC2 kinetic energy to IE rotational energy"})
		public static double kinToRotEfficiency = .8;

		@Comment({"The conversion factor between Joules (the SI unit) and RF. Used for the Marx generator and the rotary converters",
				"With the default value the IE diesel generator produces 200kW"})
		public static double joulesPerRF = 200e3 / (20 * IEConfig.Machines.dieselGen_output);
		@Comment({"What energy types can be used with the mechanical multiblock. 0: None (Probably useless),",
				"1: EU (Currently useless), 2: FE, 3:EU and FE (allows conversion, default)"})
		public static int multiblockEnergyType = 3;

		public static boolean allowMBFE() {
			return (multiblockEnergyType & 2) != 0;
		}

		public static boolean allowMBEU() {
			return (multiblockEnergyType & 1) != 0 && IndustrialWires.hasIC2;
		}
	}

	public static HVStuff hv;

	public static class HVStuff {
		@Comment({"The amount of EU a Jacobs Ladder uses per tick, sorted by size of the ladder"})
		public static double[] jacobsUsageWatt = {40, 300, 2000};
		@Comment({"The damage dealt by a small Jacobs Ladder. Normal Ladders deal twice this damage, huge ones 3 times as much"})
		public static float jacobsBaseDmg = 5;
		@Comment({"The effect of standing somewhat close to a Marx generator discharge.",
				"0: Tinnitus, 1: Nausea, 2: normal damage"})
		public static int marxSoundDamage = 0;
		@Comment({"Set to false to disable shaders. They are used for rendering the Marx generator and the Jacob's ladder."})
		public static boolean enableShaders = true;
	}

	@SubscribeEvent
	public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent ev) {
		if (ev.getModID().equals(IndustrialWires.MODID)) {
			ConfigManager.sync(IndustrialWires.MODID, Config.Type.INSTANCE);
		}
	}
}
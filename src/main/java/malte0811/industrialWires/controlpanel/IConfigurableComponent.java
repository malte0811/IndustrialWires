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

package malte0811.industrialWires.controlpanel;

import blusunrize.immersiveengineering.api.tool.IConfigurableTool.ToolConfig;
import net.minecraft.nbt.NBTBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

//Based on IConfigurableTool in the IE API
public interface IConfigurableComponent {
	/**
	 * Apply and store the config option on the given stack
	 */
	void applyConfigOption(ConfigType type, int id, NBTBase value);

	/**
	 * @return a TRANSLATED name for the config option. Try to keep this short.
	 */
	@Nullable
	@SideOnly(Side.CLIENT)
	String fomatConfigName(ConfigType type, int id);

	/**
	 * @return a TRANSLATED name for the config option, displayed when hovering over it
	 */
	@Nullable
	@SideOnly(Side.CLIENT)
	String fomatConfigDescription(ConfigType type, int id);

	default StringConfig[] getStringOptions() {
		return new StringConfig[0];
	}

	default RSColorConfig[] getRSChannelOptions() {
		return new RSColorConfig[0];
	}

	default IntConfig[] getIntegerOptions() {
		return new IntConfig[0];
	}

	default BoolConfig[] getBooleanOptions() {
		return new BoolConfig[0];
	}

	default FloatConfig[] getFloatOptions() {
		return new FloatConfig[0];
	}

	class UniversalConfig<T> extends ToolConfig {
		public T value;

		protected UniversalConfig(String name, int x, int y, T value) {
			super(name, x, y);
			this.value = value;
		}
	}

	class BoolConfig extends UniversalConfig<Boolean> {
		public BoolConfig(String name, int x, int y, Boolean value) {
			super(name, x, y, value);
		}
	}

	class StringConfig extends UniversalConfig<String> {
		public StringConfig(String name, int x, int y, String value) {
			super(name, x, y, value);
		}
	}

	class RSColorConfig extends UniversalConfig<Byte> {
		public boolean small;

		public RSColorConfig(String name, int x, int y, Byte value) {
			this(name, x, y, value, false);
		}

		public RSColorConfig(String name, int x, int y, Byte value, boolean small) {
			super(name, x, y, value);
			this.small = small;
		}
	}

	class IntConfig extends UniversalConfig<Integer> {
		public int digits;
		public boolean allowNegative;

		public IntConfig(String name, int x, int y, Integer value, int digits, boolean allowNegative) {
			super(name, x, y, value);
			this.digits = digits;
			this.allowNegative = allowNegative;
		}
	}

	class FloatConfig extends UniversalConfig<Float> {
		public int width;

		public FloatConfig(String name, int x, int y, Float value, int width) {
			super(name, x, y, value);
			this.width = width;
		}
	}

	enum ConfigType {
		BOOL,
		STRING,
		RS_CHANNEL,
		INT,
		FLOAT;
	}
}
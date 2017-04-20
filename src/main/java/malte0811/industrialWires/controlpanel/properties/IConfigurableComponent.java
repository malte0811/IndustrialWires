package malte0811.industrialWires.controlpanel.properties;

import blusunrize.immersiveengineering.api.tool.IConfigurableTool.ToolConfig;
import net.minecraft.nbt.NBTBase;

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
	String fomatConfigName(ConfigType type, int id);

	/**
	 * @return a TRANSLATED name for the config option, displayed when hovering over it
	 */
	@Nullable
	String fomatConfigDescription(ConfigType type, int id);

	default StringConfig[] getStringOptions() {
		return new StringConfig[0];
	}

	default RSChannelConfig[] getRSChannelOptions() {
		return new RSChannelConfig[0];
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

	class RSChannelConfig extends UniversalConfig<Byte> {
		public RSChannelConfig(String name, int x, int y, Byte value) {
			super(name, x, y, value);
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
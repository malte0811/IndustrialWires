package malte0811.industrialWires;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;

@Config(modid=IndustrialWires.MODID)
public class IWConfig {
	@Comment({"The maximum length of a single connection.", "Order: Tin, Copper, Gold, HV, Glass Fiber"})
	public static int[] maxLengthPerConn = {16, 16, 16, 32, 32};
	@Comment({"The maximum length of wire a coil item.", "Order: Tin, Copper, Gold, HV, Glass Fiber (as above)"})
	public static int[] maxLengthOnCoil = {1024, 1024, 1024, 2048, 2048};
}

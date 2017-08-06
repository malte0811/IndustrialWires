package malte0811.industrialWires.crafting.factories;

import com.google.gson.JsonObject;
import malte0811.industrialWires.IWConfig;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;

import java.util.function.BooleanSupplier;

public class IsConversionEnabled implements IConditionFactory {

	@Override
	public BooleanSupplier parse(JsonContext context, JsonObject json) {
		return () -> IWConfig.enableConversion;
	}
}

package malte0811.industrialWires.blocks.controlpanel;

import net.minecraft.util.IStringSerializable;

import java.util.Locale;

public enum  BlockTypes_Panel implements IStringSerializable {
	TOP,
	RS_WIRE,
	DUMMY;

	@Override
	public String getName() {
		return toString().toLowerCase(Locale.ENGLISH);
	}
}

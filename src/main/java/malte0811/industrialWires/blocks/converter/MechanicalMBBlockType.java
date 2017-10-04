package malte0811.industrialWires.blocks.converter;

import net.minecraft.util.IStringSerializable;

public enum MechanicalMBBlockType implements IStringSerializable {
    NO_MODEL,
    END,
    OTHER_END,
	COIL_4_PHASE,
	COIL_1_PHASE,
    SHAFT_INSULATING,
    SHAFT_4_PHASE,
    SHAFT_1_PHASE,
    SHAFT_COMMUTATOR,
    FLYWHEEL;
    public static final MechanicalMBBlockType[] VALUES = values();

    @Override
    public String getName() {
        return name().toLowerCase();
    }
}

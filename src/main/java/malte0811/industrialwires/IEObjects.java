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

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsIE;
import blusunrize.immersiveengineering.common.blocks.metal.*;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class IEObjects {
	@GameRegistry.ObjectHolder(ImmersiveEngineering.MODID+":metal_decoration0")
	public static BlockIEBase<BlockTypes_MetalDecoration0> blockMetalDecoration0 = nullNotNull();
	@GameRegistry.ObjectHolder(ImmersiveEngineering.MODID+":metal_decoration1")
	public static BlockIEBase<BlockTypes_MetalDecoration1> blockMetalDecoration1 = nullNotNull();
	@GameRegistry.ObjectHolder(ImmersiveEngineering.MODID+":metal_decoration2")
	public static BlockIEBase<BlockTypes_MetalDecoration2> blockMetalDecoration2 = nullNotNull();
	@GameRegistry.ObjectHolder(ImmersiveEngineering.MODID+":metal_device0")
	public static BlockIEBase<BlockTypes_MetalDevice0> blockMetalDevice0 = nullNotNull();
	@GameRegistry.ObjectHolder(ImmersiveEngineering.MODID+":connector")
	public static BlockIEBase<BlockTypes_Connector> blockConnectors = nullNotNull();
	@GameRegistry.ObjectHolder(ImmersiveEngineering.MODID+":storage")
	public static BlockIEBase<BlockTypes_MetalsIE> blockStorage = nullNotNull();

	//ITEMS
	@GameRegistry.ObjectHolder(ImmersiveEngineering.MODID+":earmuffs")
	public static Item itemEarmuffs = nullNotNull();
	@GameRegistry.ObjectHolder(ImmersiveEngineering.MODID+":material")
	public static Item itemMaterial = nullNotNull();
	@GameRegistry.ObjectHolder(ImmersiveEngineering.MODID+":wirecoil")
	public static Item itemWireCoil = nullNotNull();
	@GameRegistry.ObjectHolder(ImmersiveEngineering.MODID+":tool")
	public static Item itemTool = nullNotNull();


	// Ugly hack to prevent null warnings
	private static <T> T nullNotNull() {
		//noinspection ConstantConditions
		return null;
	}
}

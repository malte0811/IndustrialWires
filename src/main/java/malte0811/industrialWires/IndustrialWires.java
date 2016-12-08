/*******************************************************************************
 * This file is part of Industrial Wires.
 * Copyright (C) 2016 malte0811
 *
 * Industrial Wires is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Industrial Wires is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Industrial Wires.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package malte0811.industrialWires;

import java.util.ArrayList;
import java.util.List;

import blusunrize.immersiveengineering.api.tool.AssemblerHandler;
import blusunrize.immersiveengineering.api.tool.AssemblerHandler.RecipeQuery;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.stone.BlockTypes_StoneDecoration;
import ic2.api.item.IC2Items;
import malte0811.industrialWires.blocks.wire.BlockIC2Connector;
import malte0811.industrialWires.blocks.wire.TileEntityIC2ConnectorCopper;
import malte0811.industrialWires.blocks.wire.TileEntityIC2ConnectorGlass;
import malte0811.industrialWires.blocks.wire.TileEntityIC2ConnectorGold;
import malte0811.industrialWires.blocks.wire.TileEntityIC2ConnectorHV;
import malte0811.industrialWires.blocks.wire.TileEntityIC2ConnectorTin;
import malte0811.industrialWires.crafting.RecipeCoilLength;
import malte0811.industrialWires.items.ItemIC2Coil;
import malte0811.industrialWires.wires.IC2Wiretype;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.RecipeSorter.Category;
import net.minecraftforge.oredict.ShapedOreRecipe;

@Mod(modid = IndustrialWires.MODID, version = IndustrialWires.VERSION, dependencies="required-after:immersiveengineering@[0.10-43,);required-after:IC2")
public class IndustrialWires {
	public static final String MODID = "industrialwires";
	public static final String VERSION = "${version}";
	public static BlockIC2Connector ic2conn;
	public static ItemIC2Coil coil;
	public static CreativeTabs creativeTab = new CreativeTabs(MODID) {
		
		@Override
		public Item getTabIconItem() {
			return null;
		}
		public ItemStack getIconItemStack() {
			return new ItemStack(coil, 1, 2);
		}
	};
	@SidedProxy(clientSide="malte0811.industrialWires.client.ClientProxy", serverSide="malte0811.industrialWires.CommonProxy")
	public static CommonProxy proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent e) {
		ic2conn = new BlockIC2Connector();
		coil = new ItemIC2Coil();
		GameRegistry.registerTileEntity(TileEntityIC2ConnectorTin.class, "ic2ConnectorTin");
		GameRegistry.registerTileEntity(TileEntityIC2ConnectorCopper.class, "ic2ConnectorCopper");
		GameRegistry.registerTileEntity(TileEntityIC2ConnectorGold.class, "ic2ConnectorGold");
		GameRegistry.registerTileEntity(TileEntityIC2ConnectorHV.class, "ic2ConnectorHV");
		GameRegistry.registerTileEntity(TileEntityIC2ConnectorGlass.class, "ic2ConnectorGlass");
		if (IC2Wiretype.IC2_TYPES==null) {
			throw new IllegalStateException("No IC2 wires registered");
		}
		proxy.preInit();
	}

	@EventHandler
	public void init(FMLInitializationEvent e) {
		ItemStack glassCable = IC2Items.getItem("cable", "type:glass,insulation:0");
		//CONNECTORS
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ic2conn, 4, 0), " t ", "rtr", "rtr", 't', "ingotTin", 'r', "itemRubber"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ic2conn, 4, 2), " c ", "rcr", "rcr", 'c', "ingotCopper", 'r', "itemRubber"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ic2conn, 4, 4), " g ", "rgr", "rgr", 'g', "ingotGold", 'r', "itemRubber"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ic2conn, 4, 6), " i ", "rir", "rir", 'i', "ingotIron", 'r', "itemRubber"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ic2conn, 4, 8), " c ", "rcr", "rcr",'c', glassCable, 'r', "itemRubber"));
		//RELAYS
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ic2conn, 4, 1), " t ", "rtr", 't', "ingotTin", 'r', "itemRubber"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ic2conn, 4, 3), " c ", "rcr", 'c', "ingotCopper", 'r', "itemRubber"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ic2conn, 4, 5), " g ", "rgr", 'g', "ingotGold", 'r', "itemRubber"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ic2conn, 4, 7), " i ", "gig", "gig", 'i', "ingotIron", 'g', new ItemStack(IEContent.blockStoneDecoration, 1, BlockTypes_StoneDecoration.INSULATING_GLASS.getMeta())));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ic2conn, 2, 9), " c ", "grg", "grg", 'r', "itemRubber", 'c', glassCable, 'g', new ItemStack(IEContent.blockStoneDecoration, 1, BlockTypes_StoneDecoration.INSULATING_GLASS.getMeta())));
		//WIRES
		RecipeSorter.register("industrialwires:coilLength", RecipeCoilLength.class, Category.SHAPELESS, "after:forge:shapelessore");
		for (int i = 0;i<IC2Wiretype.IC2_TYPES.length;i++) {
			GameRegistry.addRecipe(new RecipeCoilLength(i));
		}
		AssemblerHandler.registerRecipeAdapter(RecipeCoilLength.class, new AssemblerHandler.IRecipeAdapter<RecipeCoilLength>() {

			@Override
			public RecipeQuery[] getQueriedInputs(RecipeCoilLength recipe, ItemStack[] in) {
				List<RecipeQuery> ret = new ArrayList<>();
				for (int i = 0;i<in.length-1;i++) {
					boolean added = false;
					for (int j = 0;j<ret.size();j++) {
						if (ItemStack.areItemStacksEqual((ItemStack)ret.get(j).query, in[i])) {
							ret.get(j).querySize++;
							added = true;
							break;
						}
					}
					if (!added) {
						ret.add(new RecipeQuery(in[i], 1));
					}
				}
				return ret.toArray(new RecipeQuery[ret.size()]);
			}
			@Override
			public RecipeQuery[] getQueriedInputs(RecipeCoilLength arg0) {
				return new RecipeQuery[0];
			}
			
		});
	}
	@EventHandler
	public void postInit(FMLPostInitializationEvent	 e) {
		proxy.postInit();
	}
}

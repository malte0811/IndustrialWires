/*
 * This file is part of Industrial Wires.
 * Copyright (C) 2016-2017 malte0811
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
 */
package malte0811.industrialWires;

import blusunrize.immersiveengineering.api.tool.AssemblerHandler;
import blusunrize.immersiveengineering.api.tool.AssemblerHandler.IRecipeAdapter;
import blusunrize.immersiveengineering.api.tool.AssemblerHandler.RecipeQuery;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_Connector;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0;
import blusunrize.immersiveengineering.common.blocks.stone.BlockTypes_StoneDecoration;
import ic2.api.item.IC2Items;
import malte0811.industrialWires.blocks.BlockJacobsLadder;
import malte0811.industrialWires.blocks.TileEntityJacobsLadder;
import malte0811.industrialWires.blocks.controlpanel.*;
import malte0811.industrialWires.blocks.converter.BlockMechanicalConverter;
import malte0811.industrialWires.blocks.converter.TileEntityIEMotor;
import malte0811.industrialWires.blocks.converter.TileEntityMechICtoIE;
import malte0811.industrialWires.blocks.converter.TileEntityMechIEtoIC;
import malte0811.industrialWires.blocks.wire.*;
import malte0811.industrialWires.controlpanel.PanelUtils;
import malte0811.industrialWires.crafting.RecipeCoilLength;
import malte0811.industrialWires.items.ItemIC2Coil;
import malte0811.industrialWires.items.ItemPanelComponent;
import malte0811.industrialWires.network.MessageComponentSync;
import malte0811.industrialWires.network.MessageGUIInteract;
import malte0811.industrialWires.network.MessagePanelInteract;
import malte0811.industrialWires.network.MessageTileSyncIW;
import malte0811.industrialWires.wires.IC2Wiretype;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.RecipeSorter.Category;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.ArrayList;
import java.util.List;

@Mod(modid = IndustrialWires.MODID, version = IndustrialWires.VERSION, dependencies = "required-after:immersiveengineering@[0.10-58,);required-after:ic2")
public class IndustrialWires {
	public static final String MODID = "industrialwires";
	public static final String VERSION = "${version}";
	public static BlockIC2Connector ic2conn;
	public static BlockMechanicalConverter mechConv;
	public static BlockJacobsLadder jacobsLadder;
	public static BlockPanel panel;
	public static ItemIC2Coil coil;
	public static ItemPanelComponent panelComponent;
	public static final SimpleNetworkWrapper packetHandler = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
	@Mod.Instance(MODID)
	public static IndustrialWires instance = new IndustrialWires();
	public static CreativeTabs creativeTab = new CreativeTabs(MODID) {

		@Override
		public ItemStack getTabIconItem() {
			return new ItemStack(coil, 1, 2);
		}
	};
	@SidedProxy(clientSide="malte0811.industrialWires.client.ClientProxy", serverSide="malte0811.industrialWires.CommonProxy")
	public static CommonProxy proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent e) {
		new IWConfig();
		ic2conn = new BlockIC2Connector();
		if (IWConfig.enableConversion)
			mechConv = new BlockMechanicalConverter();
		jacobsLadder = new BlockJacobsLadder();
		coil = new ItemIC2Coil();
		panelComponent = new ItemPanelComponent();
		panel = new BlockPanel();
		GameRegistry.registerTileEntity(TileEntityIC2ConnectorTin.class, MODID + "ic2ConnectorTin");
		GameRegistry.registerTileEntity(TileEntityIC2ConnectorCopper.class, MODID + "ic2ConnectorCopper");
		GameRegistry.registerTileEntity(TileEntityIC2ConnectorGold.class, MODID + "ic2ConnectorGold");
		GameRegistry.registerTileEntity(TileEntityIC2ConnectorHV.class, MODID + "ic2ConnectorHV");
		GameRegistry.registerTileEntity(TileEntityIC2ConnectorGlass.class, MODID + "ic2ConnectorGlass");
		GameRegistry.registerTileEntity(TileEntityJacobsLadder.class, MODID+":jacobsLadder");
		GameRegistry.registerTileEntity(TileEntityPanel.class, MODID+":control_panel");
		GameRegistry.registerTileEntity(TileEntityRSPanelConn.class, MODID+":control_panel_rs");
		GameRegistry.registerTileEntity(TileEntityPanelCreator.class, MODID+":panel_creator");
		if (mechConv!=null) {
			GameRegistry.registerTileEntity(TileEntityIEMotor.class, MODID+":ieMotor");
			GameRegistry.registerTileEntity(TileEntityMechICtoIE.class, MODID+":mechIcToIe");
			GameRegistry.registerTileEntity(TileEntityMechIEtoIC.class, MODID+":mechIeToIc");
		}
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
		AssemblerHandler.registerRecipeAdapter(RecipeCoilLength.class, new CoilLengthAdapter());
		// MECH CONVERTERS
		if (mechConv!=null) {
			ItemStack shaftIron = IC2Items.getItem("crafting", "iron_shaft");
			ItemStack shaftSteel = IC2Items.getItem("crafting", "steel_shaft");
			ItemStack ironMechComponent = new ItemStack(IEContent.itemMaterial, 1, 8);
			ItemStack steelMechComponent = new ItemStack(IEContent.itemMaterial, 1, 9);
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(mechConv, 1, 0), " s ", "ici", "mum", 's', "stickIron",
					'i', "ingotIron", 'c', new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.COIL_LV.getMeta()),
					'u', "ingotCopper", 'm', ironMechComponent));
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(mechConv, 1, 2), "iIi", "sbS", "mrm", 's', "blockSheetmetalIron",
					'i', "plateIron", 'I', shaftIron,
					'b', "ingotBronze", 'm', steelMechComponent,
					'S', "blockSheetmetalSteel", 'r', "stickSteel"));
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(mechConv, 1, 1), "mrm", "sbS", "iIi", 's', "blockSheetmetalIron",
					'i', "plateSteel", 'I', shaftSteel,
					'b', "ingotBronze", 'm', ironMechComponent,
					'S', "blockSheetmetalSteel", 'r', "stickIron"));
		}
		// JACOB'S LADDERS
		ItemStack mvTransformer = IC2Items.getItem("te", "mv_transformer");
		ItemStack copperCable = IC2Items.getItem("cable", "type:copper,insulation:0");
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(jacobsLadder, 1, 0), "c c", " h ", "sts", 'c', copperCable, 'h', Blocks.HARDENED_CLAY,
				's', "ingotSteel", 't', mvTransformer));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(jacobsLadder, 1, 1), "c c", "h h", "sts", 'c', "ingotCopper", 'h', Blocks.HARDENED_CLAY,
				's', "ingotSteel", 't', new ItemStack(IEContent.blockConnectors, 1, BlockTypes_Connector.TRANSFORMER.ordinal())));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(jacobsLadder, 1, 2), "c c", "hhh", "sts", 'c', "blockCopper", 'h', Blocks.HARDENED_CLAY,
				's', "ingotSteel", 't', new ItemStack(IEContent.blockConnectors, 1, BlockTypes_Connector.TRANSFORMER_HV.ordinal())));
		// CONTROL PANELS
		ItemStack drillHeadIron = new ItemStack(IEContent.itemDrillhead, 1, 1);
		ItemStack motor = IC2Items.getItem("crafting", "electric_motor");
		ItemStack advAlloy = IC2Items.getItem("crafting", "alloy");
		ItemStack coil = IC2Items.getItem("crafting", "coil");
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(panel, 1, BlockTypes_Panel.CREATOR.ordinal()),
				"rmr", "rdr", "rar", 'r', "stickSteel", 'm', motor, 'd', drillHeadIron, 'a', advAlloy));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(panel, 8, BlockTypes_Panel.DUMMY.ordinal()),
				" r ", "rmr", " r ", 'r', "dustRedstone", 'm', PanelUtils.getPanelBase()));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(panel, 1, BlockTypes_Panel.RS_WIRE.ordinal()),
				"c", "d", 'd', new ItemStack(panel, 1, BlockTypes_Panel.DUMMY.ordinal()), 'c',
				new ItemStack(IEContent.blockConnectors, 1, BlockTypes_Connector.CONNECTOR_REDSTONE.ordinal())));
		//	PANEL COMPONENTS
		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(panelComponent, 1, 0),
				"dustGlowstone", Blocks.STONE_BUTTON, "wireCopper"));
		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(panelComponent, 4, 1),
				"paper", "plateIron"));
		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(panelComponent, 1, 2),
				"dustGlowstone", "dustRedstone", "wireCopper"));
		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(panelComponent, 1, 3),
				Blocks.STONE_BUTTON, new ItemStack(IEContent.itemWireCoil, 1, 2), "wireCopper"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(panelComponent, 1, 4),
				"r", "g", "c", 'r', "itemRubber", 'g', "ingotHOPGraphite", 'c', coil));
		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(panelComponent, 1, 5),
				"stickIron", Blocks.LEVER, "wireCopper"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(panelComponent, 1, 6),
				"aaa", "asa", 'a', "plateAluminum", 's', new ItemStack(panelComponent, 2, 5)));

		packetHandler.registerMessage(MessageTileSyncIW.HandlerClient.class, MessageTileSyncIW.class, 0, Side.CLIENT);
		packetHandler.registerMessage(MessagePanelInteract.HandlerServer.class, MessagePanelInteract.class, 1, Side.SERVER);
		packetHandler.registerMessage(MessageGUIInteract.HandlerServer.class, MessageGUIInteract.class, 2, Side.SERVER);
		packetHandler.registerMessage(MessageComponentSync.HandlerServer.class, MessageComponentSync.class, 3, Side.SERVER);

		NetworkRegistry.INSTANCE.registerGuiHandler(instance, proxy);
	}
	@EventHandler
	public void postInit(FMLPostInitializationEvent	 e) {
		proxy.postInit();
	}
	private class CoilLengthAdapter implements IRecipeAdapter<RecipeCoilLength> {
		@Override
		public RecipeQuery[] getQueriedInputs(RecipeCoilLength recipe, NonNullList<ItemStack> in) {
			List<RecipeQuery> ret = new ArrayList<>();
			for (int i = 0; i < in.size() - 1; i++) {
				boolean added = false;
				for (RecipeQuery aRet : ret) {
					if (ItemStack.areItemStacksEqual((ItemStack) aRet.query, in.get(i))) {
						aRet.querySize++;
						added = true;
						break;
					}
				}
				if (!added) {
					ret.add(new RecipeQuery(in.get(i), 1));
				}
			}
			return ret.toArray(new RecipeQuery[ret.size()]);
		}
		@Override
		public RecipeQuery[] getQueriedInputs(RecipeCoilLength arg0) {
			return new RecipeQuery[0];
		}
	}
}

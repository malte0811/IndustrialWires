package malte0811.industrialWires;

import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.stone.BlockTypes_StoneDecoration;
import ic2.api.item.IC2Items;
import malte0811.industrialWires.blocks.BlockIC2Connector;
import malte0811.industrialWires.blocks.TileEntityIC2ConnectorCopper;
import malte0811.industrialWires.blocks.TileEntityIC2ConnectorGold;
import malte0811.industrialWires.blocks.TileEntityIC2ConnectorHV;
import malte0811.industrialWires.blocks.TileEntityIC2ConnectorTin;
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
import net.minecraftforge.oredict.ShapedOreRecipe;

@Mod(modid = IndustrialWires.MODID, version = IndustrialWires.VERSION, dependencies="required-after:immersiveengineering;required-after:IC2")
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
		if (IC2Wiretype.IC2_TYPES==null) {
			throw new IllegalStateException("No IC2 wires registered");
		}
		proxy.preInit();
	}

	@EventHandler
	public void init(FMLInitializationEvent e) {
		//WIRES
		ItemStack tinCable = IC2Items.getItem("cable", "type:tin,insulation:0");
		ItemStack copperCable = IC2Items.getItem("cable", "type:copper,insulation:0");
		ItemStack goldCable = IC2Items.getItem("cable", "type:gold,insulation:0");
		ItemStack hvCable = IC2Items.getItem("cable", "type:iron,insulation:0");
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(coil, 2, 0), "ttt", "tst", "ttt", 't', tinCable, 's', "stickWood"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(coil, 2, 1), "ccc", "csc", "ccc", 'c', copperCable, 's', "stickWood"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(coil, 2, 2), "ggg", "gsg", "ggg", 'g', goldCable, 's', "stickWood"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(coil, 2, 3), "hhh", "hsh", "hhh", 'h', hvCable, 's', "stickWood"));
		//CONNECTORS
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ic2conn, 1, 0), " t ", "rtr", "rtr", 't', "ingotTin", 'r', "itemRubber"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ic2conn, 1, 2), " c ", "rcr", "rcr", 'c', "ingotCopper", 'r', "itemRubber"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ic2conn, 1, 4), " g ", "rgr", "rgr", 'g', "ingotGold", 'r', "itemRubber"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ic2conn, 1, 6), " i ", "rir", "rir", 'i', "ingotIron", 'r', "itemRubber"));
		//RELAYS
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ic2conn, 1, 1), " t ", "rtr", 't', "ingotTin", 'r', "itemRubber"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ic2conn, 1, 3), " c ", "rcr", 'c', "ingotCopper", 'r', "itemRubber"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ic2conn, 1, 5), " g ", "rgr", 'g', "ingotGold", 'r', "itemRubber"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ic2conn, 1, 7), " i ", "gig", "gig", 'i', "ingotIron", 'g', new ItemStack(IEContent.blockStoneDecoration, 1, BlockTypes_StoneDecoration.INSULATING_GLASS.getMeta())));
	}
	@EventHandler
	public void postInit(FMLPostInitializationEvent	 e) {
		proxy.postInit();
	}
}

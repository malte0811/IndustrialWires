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

import malte0811.industrialWires.blocks.BlockJacobsLadder;
import malte0811.industrialWires.blocks.TileEntityJacobsLadder;
import malte0811.industrialWires.blocks.controlpanel.*;
import malte0811.industrialWires.blocks.converter.BlockMechanicalConverter;
import malte0811.industrialWires.blocks.converter.TileEntityIEMotor;
import malte0811.industrialWires.blocks.converter.TileEntityMechICtoIE;
import malte0811.industrialWires.blocks.converter.TileEntityMechIEtoIC;
import malte0811.industrialWires.blocks.wire.*;
import malte0811.industrialWires.items.ItemIC2Coil;
import malte0811.industrialWires.items.ItemKey;
import malte0811.industrialWires.items.ItemPanelComponent;
import malte0811.industrialWires.network.MessageGUIInteract;
import malte0811.industrialWires.network.MessageItemSync;
import malte0811.industrialWires.network.MessagePanelInteract;
import malte0811.industrialWires.network.MessageTileSyncIW;
import malte0811.industrialWires.wires.IC2Wiretype;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLMissingMappingsEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

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
	public static ItemKey key;
	public static final SimpleNetworkWrapper packetHandler = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
	@Mod.Instance(MODID)
	public static IndustrialWires instance = new IndustrialWires();
	public static CreativeTabs creativeTab = new CreativeTabs(MODID) {

		@Override
		public ItemStack getTabIconItem() {
			return new ItemStack(coil, 1, 2);
		}
	};
	@SidedProxy(clientSide = "malte0811.industrialWires.client.ClientProxy", serverSide = "malte0811.industrialWires.CommonProxy")
	public static CommonProxy proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent e) {
		new IWConfig();
		ic2conn = new BlockIC2Connector();
		if (IWConfig.enableConversion)
			mechConv = new BlockMechanicalConverter();
		jacobsLadder = new BlockJacobsLadder();
		panel = new BlockPanel();

		coil = new ItemIC2Coil();
		panelComponent = new ItemPanelComponent();
		key = new ItemKey();

		GameRegistry.registerTileEntity(TileEntityIC2ConnectorTin.class, MODID + "ic2ConnectorTin");
		GameRegistry.registerTileEntity(TileEntityIC2ConnectorCopper.class, MODID + "ic2ConnectorCopper");
		GameRegistry.registerTileEntity(TileEntityIC2ConnectorGold.class, MODID + "ic2ConnectorGold");
		GameRegistry.registerTileEntity(TileEntityIC2ConnectorHV.class, MODID + "ic2ConnectorHV");
		GameRegistry.registerTileEntity(TileEntityIC2ConnectorGlass.class, MODID + "ic2ConnectorGlass");
		GameRegistry.registerTileEntity(TileEntityJacobsLadder.class, MODID + ":jacobsLadder");
		GameRegistry.registerTileEntity(TileEntityPanel.class, MODID + ":control_panel");
		GameRegistry.registerTileEntity(TileEntityRSPanelConn.class, MODID + ":control_panel_rs");
		GameRegistry.registerTileEntity(TileEntityPanelCreator.class, MODID + ":panel_creator");
		GameRegistry.registerTileEntity(TileEntityUnfinishedPanel.class, MODID + ":unfinished_panel");
		if (mechConv != null) {
			GameRegistry.registerTileEntity(TileEntityIEMotor.class, MODID + ":ieMotor");
			GameRegistry.registerTileEntity(TileEntityMechICtoIE.class, MODID + ":mechIcToIe");
			GameRegistry.registerTileEntity(TileEntityMechIEtoIC.class, MODID + ":mechIeToIc");
		}
		if (IC2Wiretype.IC2_TYPES == null) {
			throw new IllegalStateException("No IC2 wires registered");
		}
		proxy.preInit();
	}

	@EventHandler
	public void init(FMLInitializationEvent e) {
		Recipes.addRecipes();

		ExtraIC2Compat.addToolConmpat();

		packetHandler.registerMessage(MessageTileSyncIW.HandlerClient.class, MessageTileSyncIW.class, 0, Side.CLIENT);
		packetHandler.registerMessage(MessagePanelInteract.HandlerServer.class, MessagePanelInteract.class, 1, Side.SERVER);
		packetHandler.registerMessage(MessageGUIInteract.HandlerServer.class, MessageGUIInteract.class, 2, Side.SERVER);
		packetHandler.registerMessage(MessageItemSync.HandlerServer.class, MessageItemSync.class, 3, Side.SERVER);

		NetworkRegistry.INSTANCE.registerGuiHandler(instance, proxy);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent e) {
		proxy.postInit();
	}

	@EventHandler
	public void remap(FMLMissingMappingsEvent ev) {
		for (FMLMissingMappingsEvent.MissingMapping miss : ev.get()) {
			String name = miss.resourceLocation.getResourcePath();
			switch (name) {
			case "ic2connector":
				if (miss.type == GameRegistry.Type.ITEM) {
					miss.remap(Item.getItemFromBlock(IndustrialWires.ic2conn));
				} else {
					miss.remap(IndustrialWires.ic2conn);
				}
				break;
			case "ic2wirecoil":
				miss.remap(IndustrialWires.coil);
				break;
			}
		}
	}
}

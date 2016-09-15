package malte0811.industrialWires.client;

import java.util.Locale;

import com.google.common.collect.ImmutableMap;

import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.immersiveengineering.client.models.smart.ConnLoader;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.ManualPages;
import malte0811.industrialWires.CommonProxy;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.items.ItemIC2Coil;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.registry.GameData;

public class ClientProxy extends CommonProxy {
	@Override
	public void preInit() {
		super.preInit();
		ConnLoader.baseModels.put("ic2_conn_tin", new ResourceLocation("immersiveengineering:block/connector/connectorLV.obj"));
		ConnLoader.textureReplacements.put("ic2_conn_tin", ImmutableMap.of("#immersiveengineering:blocks/connector_connectorLV",
				IndustrialWires.MODID+":blocks/ic2_connTin"));
		ConnLoader.baseModels.put("ic2_relay_tin", new ResourceLocation("immersiveengineering:block/connector/connectorLV.obj"));
		ConnLoader.textureReplacements.put("ic2_relay_tin", ImmutableMap.of("#immersiveengineering:blocks/connector_connectorLV",
				IndustrialWires.MODID+":blocks/ic2_relayTin"));

		ConnLoader.baseModels.put("ic2_conn_copper", new ResourceLocation("immersiveengineering:block/connector/connectorLV.obj"));
		ConnLoader.textureReplacements.put("ic2_conn_copper", ImmutableMap.of("#immersiveengineering:blocks/connector_connectorLV",
				IndustrialWires.MODID+":blocks/ic2_connCopper"));
		ConnLoader.baseModels.put("ic2_relay_copper", new ResourceLocation("immersiveengineering:block/connector/connectorLV.obj"));
		ConnLoader.textureReplacements.put("ic2_relay_copper", ImmutableMap.of("#immersiveengineering:blocks/connector_connectorLV",
				IndustrialWires.MODID+":blocks/ic2_relayCopper"));

		ConnLoader.baseModels.put("ic2_conn_gold", new ResourceLocation("immersiveengineering:block/connector/connectorMV.obj"));
		ConnLoader.textureReplacements.put("ic2_conn_gold", ImmutableMap.of("#immersiveengineering:blocks/connector_connectorMV",
				IndustrialWires.MODID+":blocks/ic2_connGold"));
		ConnLoader.baseModels.put("ic2_relay_gold", new ResourceLocation("immersiveengineering:block/connector/connectorMV.obj"));
		ConnLoader.textureReplacements.put("ic2_relay_gold", ImmutableMap.of("#immersiveengineering:blocks/connector_connectorMV",
				IndustrialWires.MODID+":blocks/ic2_relayGold"));

		ConnLoader.baseModels.put("ic2_conn_hv", new ResourceLocation("immersiveengineering:block/connector/connectorHV.obj"));
		ConnLoader.textureReplacements.put("ic2_conn_hv", ImmutableMap.of("#immersiveengineering:blocks/connector_connectorHV",
				IndustrialWires.MODID+":blocks/ic2_connHV"));
		ConnLoader.baseModels.put("ic2_relay_hv", new ResourceLocation("immersiveengineering:block/connector/relayHV.obj"));
		ConnLoader.textureReplacements.put("ic2_relay_hv", ImmutableMap.of("#immersiveengineering:blocks/connector_connectorHV",
				IndustrialWires.MODID+":blocks/ic2_relayHV"));

		for(int meta = 0; meta < ItemIC2Coil.subNames.length; meta++) {
			ResourceLocation loc = new ResourceLocation(IndustrialWires.MODID, "ic2wireCoil/" + ItemIC2Coil.subNames[meta]);
			ModelBakery.registerItemVariants(IndustrialWires.coil, loc);
			ModelLoader.setCustomModelResourceLocation(IndustrialWires.coil, meta, new ModelResourceLocation(loc, "inventory"));
		}
		Item blockItem = Item.getItemFromBlock(IndustrialWires.ic2conn);
		final ResourceLocation loc = GameData.getBlockRegistry().getNameForObject(IndustrialWires.ic2conn);
		ModelLoader.setCustomMeshDefinition(blockItem, new ItemMeshDefinition() {
			@Override
			public ModelResourceLocation getModelLocation(ItemStack stack) {
				return new ModelResourceLocation(loc, "inventory");
			}
		});
		for(int meta = 0; meta < IndustrialWires.ic2conn.getMetaEnums().length; meta++) {
			String location = loc.toString();
			String prop = "inventory,type=" + IndustrialWires.ic2conn.getMetaEnums()[meta].toString().toLowerCase(Locale.US);
			try {
				ModelLoader.setCustomModelResourceLocation(blockItem, meta, new ModelResourceLocation(location, prop));
			} catch(NullPointerException npe) {
				throw new RuntimeException(IndustrialWires.ic2conn + " lacks an item!", npe);
			}
		}
		//		OBJLoader.INSTANCE.addDomain(IndustrialWires.MODID);
		MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
	}
	@Override
	public void postInit() {
		super.postInit();
		ManualInstance m = ManualHelper.getManual();
		m.addEntry("industrialWires.all", "industrialWires",
				new ManualPages.CraftingMulti(m, "industrialWires.all0", new ItemStack(IndustrialWires.coil, 1, 0), new ItemStack(IndustrialWires.coil, 1, 1), new ItemStack(IndustrialWires.coil, 1, 2), new ItemStack(IndustrialWires.coil, 1, 3)),
				new ManualPages.CraftingMulti(m, "industrialWires.all1", new ItemStack(IndustrialWires.ic2conn, 1, 0), new ItemStack(IndustrialWires.ic2conn, 1, 1), new ItemStack(IndustrialWires.ic2conn, 1, 2), new ItemStack(IndustrialWires.ic2conn, 1, 3),
						new ItemStack(IndustrialWires.ic2conn, 1, 4), new ItemStack(IndustrialWires.ic2conn, 1, 5), new ItemStack(IndustrialWires.ic2conn, 1, 6), new ItemStack(IndustrialWires.ic2conn, 1, 7))
				);
	}
}

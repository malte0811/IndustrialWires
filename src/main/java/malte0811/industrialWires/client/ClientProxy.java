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
package malte0811.industrialWires.client;

import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.smart.ConnLoader;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.ManualPages;
import blusunrize.lib.manual.ManualPages.PositionedItemStack;
import com.google.common.collect.ImmutableMap;
import ic2.api.item.IC2Items;
import malte0811.industrialWires.CommonProxy;
import malte0811.industrialWires.IWConfig;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.IMetaEnum;
import malte0811.industrialWires.blocks.TileEntityJacobsLadder;
import malte0811.industrialWires.blocks.controlpanel.BlockTypes_Panel;
import malte0811.industrialWires.blocks.controlpanel.TileEntityPanelCreator;
import malte0811.industrialWires.blocks.controlpanel.TileEntityRSPanelConn;
import malte0811.industrialWires.client.gui.GuiPanelComponent;
import malte0811.industrialWires.client.gui.GuiPanelCreator;
import malte0811.industrialWires.client.gui.GuiRSPanelConn;
import malte0811.industrialWires.client.gui.GuiRenameKey;
import malte0811.industrialWires.client.panelmodel.PanelModelLoader;
import malte0811.industrialWires.client.render.TileRenderJacobsLadder;
import malte0811.industrialWires.controlpanel.PanelComponent;
import malte0811.industrialWires.items.ItemIC2Coil;
import malte0811.industrialWires.items.ItemKey;
import malte0811.industrialWires.items.ItemPanelComponent;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import java.util.Locale;
import java.util.Random;
import java.util.WeakHashMap;

public class ClientProxy extends CommonProxy {
	@Override
	public void preInit() {
		super.preInit();
		ConnLoader.baseModels.put("ic2_conn_tin", new ResourceLocation("immersiveengineering:block/connector/connector_lv.obj"));
		ConnLoader.textureReplacements.put("ic2_conn_tin", ImmutableMap.of("#immersiveengineering:blocks/connector_connector_lv",
				IndustrialWires.MODID + ":blocks/ic2_conn_tin"));
		ConnLoader.baseModels.put("ic2_relay_tin", new ResourceLocation("immersiveengineering:block/connector/connector_lv.obj"));
		ConnLoader.textureReplacements.put("ic2_relay_tin", ImmutableMap.of("#immersiveengineering:blocks/connector_connector_lv",
				IndustrialWires.MODID + ":blocks/ic2_relay_tin"));

		ConnLoader.baseModels.put("ic2_conn_copper", new ResourceLocation("immersiveengineering:block/connector/connector_lv.obj"));
		ConnLoader.textureReplacements.put("ic2_conn_copper", ImmutableMap.of("#immersiveengineering:blocks/connector_connector_lv",
				IndustrialWires.MODID + ":blocks/ic2_conn_copper"));
		ConnLoader.baseModels.put("ic2_relay_copper", new ResourceLocation("immersiveengineering:block/connector/connector_lv.obj"));
		ConnLoader.textureReplacements.put("ic2_relay_copper", ImmutableMap.of("#immersiveengineering:blocks/connector_connector_lv",
				IndustrialWires.MODID + ":blocks/ic2_relay_copper"));

		ConnLoader.baseModels.put("ic2_conn_gold", new ResourceLocation("immersiveengineering:block/connector/connector_mv.obj"));
		ConnLoader.textureReplacements.put("ic2_conn_gold", ImmutableMap.of("#immersiveengineering:blocks/connector_connector_mv",
				IndustrialWires.MODID + ":blocks/ic2_conn_gold"));
		ConnLoader.baseModels.put("ic2_relay_gold", new ResourceLocation("immersiveengineering:block/connector/connector_mv.obj"));
		ConnLoader.textureReplacements.put("ic2_relay_gold", ImmutableMap.of("#immersiveengineering:blocks/connector_connector_mv",
				IndustrialWires.MODID + ":blocks/ic2_relay_gold"));

		ConnLoader.baseModels.put("ic2_conn_hv", new ResourceLocation("immersiveengineering:block/connector/connector_hv.obj"));
		ConnLoader.textureReplacements.put("ic2_conn_hv", ImmutableMap.of("#immersiveengineering:blocks/connector_connector_hv",
				IndustrialWires.MODID + ":blocks/ic2_conn_hv"));
		ConnLoader.baseModels.put("ic2_relay_hv", new ResourceLocation("immersiveengineering:block/connector/relay_hv.obj"));

		ConnLoader.baseModels.put("ic2_conn_glass", new ResourceLocation("immersiveengineering:block/connector/connector_hv.obj"));
		ConnLoader.textureReplacements.put("ic2_conn_glass", ImmutableMap.of("#immersiveengineering:blocks/connector_connector_hv",
				IndustrialWires.MODID + ":blocks/ic2_conn_glass"));
		ConnLoader.baseModels.put("ic2_relay_glass", new ResourceLocation("immersiveengineering:block/connector/relay_hv.obj"));
		ConnLoader.textureReplacements.put("ic2_relay_glass", ImmutableMap.of("#immersiveengineering:blocks/connector_relay_hv",
				IndustrialWires.MODID + ":blocks/ic2_relay_glass"));

		ConnLoader.baseModels.put("rs_panel_conn", new ResourceLocation("industrialwires:block/rs_panel_conn.obj"));
		for (int meta = 0; meta < ItemIC2Coil.subNames.length; meta++) {
			ResourceLocation loc = new ResourceLocation(IndustrialWires.MODID, "ic2_wire_coil/" + ItemIC2Coil.subNames[meta]);
			ModelBakery.registerItemVariants(IndustrialWires.coil, loc);
			ModelLoader.setCustomModelResourceLocation(IndustrialWires.coil, meta, new ModelResourceLocation(loc, "inventory"));
		}
		for (int meta = 0; meta < ItemPanelComponent.types.length; meta++) {
			ResourceLocation loc = new ResourceLocation(IndustrialWires.MODID, "panel_component/" + ItemPanelComponent.types[meta]);
			ModelBakery.registerItemVariants(IndustrialWires.panelComponent, loc);
			ModelLoader.setCustomModelResourceLocation(IndustrialWires.panelComponent, meta, new ModelResourceLocation(loc, "inventory"));
		}
		for (int meta = 0; meta < ItemKey.types.length; meta++) {
			ResourceLocation loc = new ResourceLocation(IndustrialWires.MODID, "key/" + ItemKey.types[meta]);
			ModelBakery.registerItemVariants(IndustrialWires.key, loc);
			ModelLoader.setCustomModelResourceLocation(IndustrialWires.key, meta, new ModelResourceLocation(loc, "inventory"));
		}

		Block[] blocks = {IndustrialWires.ic2conn, IndustrialWires.mechConv, IndustrialWires.jacobsLadder, IndustrialWires.panel};
		for (Block b : blocks) {
			if (b != null) {
				Item blockItem = Item.getItemFromBlock(b);
				final ResourceLocation loc = b.getRegistryName();
				assert loc != null;
				ModelLoader.setCustomMeshDefinition(blockItem, stack -> new ModelResourceLocation(loc, "inventory"));
				Object[] v = ((IMetaEnum) b).getValues();
				for (int meta = 0; meta < v.length; meta++) {
					String location = loc.toString();
					String prop = "inventory,type=" + v[meta].toString().toLowerCase(Locale.US);
					try {
						ModelLoader.setCustomModelResourceLocation(blockItem, meta, new ModelResourceLocation(location, prop));
					} catch (NullPointerException npe) {
						throw new RuntimeException(b + " lacks an item!", npe);
					}
				}
			}
		}
		OBJLoader.INSTANCE.addDomain(IndustrialWires.MODID);
		ModelLoaderRegistry.registerLoader(new PanelModelLoader());
		MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityJacobsLadder.class, new TileRenderJacobsLadder());
	}

	@Override
	public void postInit() {
		super.postInit();
		ManualInstance m = ManualHelper.getManual();
		PositionedItemStack[][] wireRecipes = new PositionedItemStack[3][10];
		int xBase = 15;
		ItemStack tinCable = IC2Items.getItem("cable", "type:tin,insulation:0");
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				wireRecipes[0][3 * i + j] = new PositionedItemStack(tinCable.copy(), 18 * i + xBase, 18 * j);
			}
		}
		ItemStack tmp = new ItemStack(IndustrialWires.coil);
		ItemIC2Coil.setLength(tmp, 9);
		wireRecipes[0][9] = new PositionedItemStack(tmp, 18 * 4 + xBase, 18);
		Random r = new Random();
		for (int i = 1; i < 3; i++) {
			int lengthSum = 0;
			for (int j1 = 0; j1 < 3; j1++) {
				for (int j2 = 0; j2 < 3; j2++) {
					if (r.nextBoolean()) {
						// cable
						lengthSum++;
						wireRecipes[i][3 * j1 + j2] = new PositionedItemStack(tinCable.copy(), 18 * j1 + xBase, 18 * j2);
					} else {
						// wire coil
						int length = r.nextInt(99) + 1;
						tmp = new ItemStack(IndustrialWires.coil);
						ItemIC2Coil.setLength(tmp, length);
						wireRecipes[i][3 * j1 + j2] = new PositionedItemStack(tmp, 18 * j1 + xBase, 18 * j2);
						lengthSum += length;
					}
				}
			}
			tmp = new ItemStack(IndustrialWires.coil);
			ItemIC2Coil.setLength(tmp, lengthSum);
			wireRecipes[i][9] = new PositionedItemStack(tmp, 18 * 4 + xBase, 18);
		}

		ClientUtils.mc().getItemColors().registerItemColorHandler((stack, pass) -> {
			if (pass == 1) {
				PanelComponent pc = ItemPanelComponent.componentFromStack(stack);
				if (pc != null) {
					return 0xff000000 | pc.getColor();
				}
			}
			return ~0;
		}, IndustrialWires.panelComponent);

		m.addEntry("industrialwires.wires", "industrialwires",
				new ManualPages.CraftingMulti(m, "industrialwires.wires0", new ItemStack(IndustrialWires.ic2conn, 1, 0), new ItemStack(IndustrialWires.ic2conn, 1, 1), new ItemStack(IndustrialWires.ic2conn, 1, 2), new ItemStack(IndustrialWires.ic2conn, 1, 3),
						new ItemStack(IndustrialWires.ic2conn, 1, 4), new ItemStack(IndustrialWires.ic2conn, 1, 5), new ItemStack(IndustrialWires.ic2conn, 1, 6), new ItemStack(IndustrialWires.ic2conn, 1, 7)),
				new ManualPages.Text(m, "industrialwires.wires1"),
				new ManualPages.CraftingMulti(m, "industrialwires.wires2", (Object[]) wireRecipes)
		);
		if (IndustrialWires.mechConv != null) {
			m.addEntry("industrialwires.mechConv", "industrialwires",
					new ManualPages.Crafting(m, "industrialwires.mechConv0", new ItemStack(IndustrialWires.mechConv, 1, 1)),
					new ManualPages.Crafting(m, "industrialwires.mechConv1", new ItemStack(IndustrialWires.mechConv, 1, 2)),
					new ManualPages.Crafting(m, "industrialwires.mechConv2", new ItemStack(IndustrialWires.mechConv, 1, 0))
			);
		}
		Config.manual_doubleA.put("iwJacobsUsage", IWConfig.HVStuff.jacobsUsageEU);
		m.addEntry("industrialwires.jacobs", "industrialwires",
				new ManualPages.CraftingMulti(m, "industrialwires.jacobs0", new ItemStack(IndustrialWires.jacobsLadder, 1, 0), new ItemStack(IndustrialWires.jacobsLadder, 1, 1), new ItemStack(IndustrialWires.jacobsLadder, 1, 2)),
				new ManualPages.Text(m, "industrialwires.jacobs1"));


		m.addEntry("industrialwires.intro", "control_panels",
				new ManualPages.Text(m, "industrialwires.intro0"),
				new ManualPages.Text(m, "industrialwires.intro1"),
				new ManualPages.Crafting(m, "industrialwires.intro2", new ItemStack(IndustrialWires.panel, 1, BlockTypes_Panel.DUMMY.ordinal())),
				new ManualPages.Text(m, "industrialwires.intro3"),
				new ManualPages.Crafting(m, "industrialwires.intro4", new ItemStack(IndustrialWires.panel, 1, BlockTypes_Panel.UNFINISHED.ordinal())),
				new ManualPages.Text(m, "industrialwires.intro5")
		);
		m.addEntry("industrialwires.panel_creator", "control_panels",
				new ManualPages.Crafting(m, "industrialwires.panel_creator0", new ItemStack(IndustrialWires.panel, 1, BlockTypes_Panel.CREATOR.ordinal())),
				new ManualPages.Text(m, "industrialwires.panel_creator1"),
				new ManualPages.Text(m, "industrialwires.panel_creator2")
		);
		m.addEntry("industrialwires.redstone", "control_panels",
				new ManualPages.Crafting(m, "industrialwires.redstone0", new ItemStack(IndustrialWires.panel, 1, BlockTypes_Panel.RS_WIRE.ordinal())),
				new ManualPages.Text(m, "industrialwires.redstone1")
		);
		m.addEntry("industrialwires.components", "control_panels",
				new ManualPages.Text(m, "industrialwires.components.general"),
				new ManualPages.Crafting(m, "industrialwires.button", new ItemStack(IndustrialWires.panelComponent, 1, 0)),
				new ManualPages.Crafting(m, "industrialwires.label", new ItemStack(IndustrialWires.panelComponent, 1, 1)),
				new ManualPages.Crafting(m, "industrialwires.indicator_light", new ItemStack(IndustrialWires.panelComponent, 1, 2)),
				new ManualPages.Crafting(m, "industrialwires.slider", new ItemStack(IndustrialWires.panelComponent, 1, 3)),
				new ManualPages.CraftingMulti(m, "industrialwires.toggle_switch", new ItemStack(IndustrialWires.panelComponent, 1, 5), new ItemStack(IndustrialWires.panelComponent, 1, 6)),
				new ManualPages.Text(m, "industrialwires.toggle_switch1"),
				new ManualPages.Crafting(m, "industrialwires.variac", new ItemStack(IndustrialWires.panelComponent, 1, 4)),
				new ManualPages.CraftingMulti(m, "industrialwires.lock", new ItemStack(IndustrialWires.panelComponent, 1, 7), new ItemStack(IndustrialWires.key)),
				new ManualPages.Crafting(m, "industrialwires.panel_meter", new ItemStack(IndustrialWires.panelComponent, 1, 8))
		);
	}

	@Override
	public World getClientWorld() {
		return Minecraft.getMinecraft().world;
	}

	private WeakHashMap<BlockPos, ISound> playingSounds = new WeakHashMap<>();
	private static ResourceLocation jacobsStart = new ResourceLocation(IndustrialWires.MODID, "jacobs_ladder_start");//~470 ms ~=9 ticks
	private static ResourceLocation jacobsMiddle = new ResourceLocation(IndustrialWires.MODID, "jacobs_ladder_middle");
	private static ResourceLocation jacobsEnd = new ResourceLocation(IndustrialWires.MODID, "jacobs_ladder_end");//~210 ms ~= 4 ticks

	@Override
	public void playJacobsLadderSound(TileEntityJacobsLadder te, int phase, Vec3d soundPos) {
		if (playingSounds.containsKey(te.getPos())) {
			Minecraft.getMinecraft().getSoundHandler().stopSound(playingSounds.get(te.getPos()));
			playingSounds.remove(te.getPos());
		}
		ResourceLocation event;
		switch (phase) {
		case 0:
			event = jacobsStart;
			break;
		case 1:
			event = jacobsMiddle;
			break;
		case 2:
			event = jacobsEnd;
			break;
		default:
			return;
		}
		PositionedSoundRecord sound = new PositionedSoundRecord(event, SoundCategory.BLOCKS, te.size.soundVolume, 1, false, 0, ISound.AttenuationType.LINEAR, (float) soundPos.x, (float) soundPos.y, (float) soundPos.z);
		ClientUtils.mc().getSoundHandler().playSound(sound);
		playingSounds.put(te.getPos(), sound);
	}

	@Override
	public Gui getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID == 0) {
			TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
			if (te instanceof TileEntityRSPanelConn) {
				return new GuiRSPanelConn((TileEntityRSPanelConn) te);
			}
			if (te instanceof TileEntityPanelCreator) {
				return new GuiPanelCreator(player.inventory, (TileEntityPanelCreator) te);
			}
		} else if (ID == 1) {
			EnumHand h = z == 1 ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
			ItemStack held = player.getHeldItem(h);
			if (!held.isEmpty()) {
				if (held.getItem() == IndustrialWires.panelComponent) {
					return new GuiPanelComponent(h, ItemPanelComponent.componentFromStack(held));
				} else if (held.getItem() == IndustrialWires.key) {
					return new GuiRenameKey(h);
				}
			}
		}
		return null;
	}
}
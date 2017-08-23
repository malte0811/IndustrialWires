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

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.BlockIWBase;
import malte0811.industrialWires.blocks.IMetaEnum;
import malte0811.industrialWires.blocks.controlpanel.TileEntityPanel;
import malte0811.industrialWires.client.panelmodel.PanelModel;
import malte0811.industrialWires.controlpanel.PanelComponent;
import malte0811.industrialWires.items.ItemIC2Coil;
import malte0811.industrialWires.items.ItemKey;
import malte0811.industrialWires.items.ItemPanelComponent;
import malte0811.industrialWires.wires.IC2Wiretype;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Locale;

@Mod.EventBusSubscriber(modid = IndustrialWires.MODID, value = Side.CLIENT)
public class ClientEventHandler {
	@SubscribeEvent
	public static void renderOverlayPost(RenderGameOverlayEvent.Post e) {
		if (ClientUtils.mc().player != null && e.getType() == RenderGameOverlayEvent.ElementType.TEXT) {
			EntityPlayer player = ClientUtils.mc().player;

			for (EnumHand hand : EnumHand.values()) {
				if (!player.getHeldItem(hand).isEmpty()) {
					ItemStack equipped = player.getHeldItem(hand);
					if (OreDictionary.itemMatches(new ItemStack(IndustrialWires.coil, 1, OreDictionary.WILDCARD_VALUE), equipped, false)) {
						IC2Wiretype type = IC2Wiretype.IC2_TYPES[equipped.getItemDamage()];
						int color = type.getColour(null);
						String s = I18n.format(IndustrialWires.MODID + ".desc.wireLength", ItemIC2Coil.getLength(equipped));
						ClientUtils.font().drawString(s, e.getResolution().getScaledWidth() / 2 - ClientUtils.font().getStringWidth(s) / 2, e.getResolution().getScaledHeight() - GuiIngameForge.left_height - 40, color, true);
						if (ItemNBTHelper.hasKey(equipped, "linkingPos")) {
							int[] link = ItemNBTHelper.getIntArray(equipped, "linkingPos");
							if (link != null && link.length > 3) {
								s = I18n.format(Lib.DESC_INFO + "attachedTo", link[1], link[2], link[3]);
								RayTraceResult focussedBlock = ClientUtils.mc().objectMouseOver;
								double distSquared;
								if (focussedBlock != null && focussedBlock.typeOfHit == RayTraceResult.Type.BLOCK) {
									distSquared = focussedBlock.getBlockPos().distanceSq(link[1], link[2], link[3]);
								} else {
									distSquared = player.getDistanceSq(link[1], link[2], link[3]);
								}
								int length = Math.min(ItemIC2Coil.getLength(equipped), type.getMaxLength());
								if (length * length < distSquared) {
									color = 0xdd3333;
								}
								ClientUtils.font().drawString(s, e.getResolution().getScaledWidth() / 2 - ClientUtils.font().getStringWidth(s) / 2, e.getResolution().getScaledHeight() - GuiIngameForge.left_height - 20, color, true);
							}
						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void renderBoundingBoxes(DrawBlockHighlightEvent event) {
		if (!event.isCanceled() && event.getSubID() == 0 && event.getTarget().typeOfHit == RayTraceResult.Type.BLOCK) {
			TileEntity tile = event.getPlayer().world.getTileEntity(event.getTarget().getBlockPos());
			if (tile instanceof TileEntityPanel) {
				TileEntityPanel panel = (TileEntityPanel) tile;
				Pair<PanelComponent, RayTraceResult> pc = panel.getSelectedComponent(Minecraft.getMinecraft().player, event.getTarget().hitVec, true);
				if (pc != null) {
					pc.getLeft().renderBox(panel);
					event.setCanceled(true);
				}
			}
		}
	}

	@SubscribeEvent
	public static void bakeModel(ModelBakeEvent event) {
		event.getModelRegistry().putObject(new ModelResourceLocation(IndustrialWires.MODID + ":control_panel", "inventory,type=top"), new PanelModel());
		event.getModelRegistry().putObject(new ModelResourceLocation(IndustrialWires.MODID + ":control_panel", "inventory,type=unfinished"), new PanelModel());
	}
	@SubscribeEvent
	public static void registerModels(ModelRegistryEvent evt) {
		if (IndustrialWires.coil!=null) {
			for (int meta = 0; meta < ItemIC2Coil.subNames.length; meta++) {
				ResourceLocation loc = new ResourceLocation(IndustrialWires.MODID, "ic2_wire_coil/" + ItemIC2Coil.subNames[meta]);
				ModelBakery.registerItemVariants(IndustrialWires.coil, loc);
				ModelLoader.setCustomModelResourceLocation(IndustrialWires.coil, meta, new ModelResourceLocation(loc, "inventory"));
			}
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
		for (BlockIWBase b : IndustrialWires.blocks) {
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
}

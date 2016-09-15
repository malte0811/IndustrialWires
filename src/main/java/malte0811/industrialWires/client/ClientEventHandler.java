package malte0811.industrialWires.client;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.wires.IC2Wiretype;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;

public class ClientEventHandler {
	@SubscribeEvent
	public void renderOverlayPost(RenderGameOverlayEvent.Post e) {
		if(ClientUtils.mc().thePlayer!=null && e.getType() == RenderGameOverlayEvent.ElementType.TEXT) {
			EntityPlayer player = ClientUtils.mc().thePlayer;

			for(EnumHand hand : EnumHand.values())
				if(player.getHeldItem(hand)!=null) {
					ItemStack equipped = player.getHeldItem(hand);
					if(OreDictionary.itemMatches(new ItemStack(IndustrialWires.coil, 1, OreDictionary.WILDCARD_VALUE), equipped, false)) {
						if(ItemNBTHelper.hasKey(equipped, "linkingPos")) {
							int[] link = ItemNBTHelper.getIntArray(equipped, "linkingPos");
							if(link!=null&&link.length>3) {
								String s = I18n.format(Lib.DESC_INFO+"attachedTo", link[1],link[2],link[3]);
								ClientUtils.font().drawString(s, e.getResolution().getScaledWidth()/2 - ClientUtils.font().getStringWidth(s)/2, e.getResolution().getScaledHeight()-GuiIngameForge.left_height-20, IC2Wiretype.IC2_TYPES[2].getColour(null), true);
							}
						}
					}
				}
		}
	}
}

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
package malte0811.industrialWires.client;

import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.immersiveengineering.api.ManualPageMultiblock;
import blusunrize.immersiveengineering.api.energy.wires.WireApi;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.ManualPages;
import blusunrize.lib.manual.ManualPages.PositionedItemStack;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import malte0811.industrialWires.CommonProxy;
import malte0811.industrialWires.IWConfig;
import malte0811.industrialWires.IWPotions;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.controlpanel.BlockTypes_Panel;
import malte0811.industrialWires.blocks.controlpanel.TileEntityPanelCreator;
import malte0811.industrialWires.blocks.controlpanel.TileEntityRSPanelConn;
import malte0811.industrialWires.blocks.converter.TileEntityMechMB;
import malte0811.industrialWires.blocks.hv.TileEntityJacobsLadder;
import malte0811.industrialWires.blocks.hv.TileEntityMarx;
import malte0811.industrialWires.client.gui.GuiPanelComponent;
import malte0811.industrialWires.client.gui.GuiPanelCreator;
import malte0811.industrialWires.client.gui.GuiRSPanelConn;
import malte0811.industrialWires.client.gui.GuiRenameKey;
import malte0811.industrialWires.client.manual.TextSplitter;
import malte0811.industrialWires.client.panelmodel.PanelModelLoader;
import malte0811.industrialWires.client.render.*;
import malte0811.industrialWires.controlpanel.PanelComponent;
import malte0811.industrialWires.converter.MechEnergy;
import malte0811.industrialWires.crafting.IC2TRHelper;
import malte0811.industrialWires.entities.EntityBrokenPart;
import malte0811.industrialWires.hv.MarxOreHandler;
import malte0811.industrialWires.hv.MultiblockMarx;
import malte0811.industrialWires.items.ItemIC2Coil;
import malte0811.industrialWires.items.ItemPanelComponent;
import malte0811.industrialWires.util.CommandIWClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {
	@Override
	public void preInit() {
		super.preInit();
		if (IndustrialWires.hasIC2) {
			WireApi.registerConnectorForRender("ic2_conn_tin", new ResourceLocation("immersiveengineering:block/connector/connector_lv.obj"),
					ImmutableMap.of("#immersiveengineering:blocks/connector_connector_lv",
					IndustrialWires.MODID + ":blocks/ic2_conn_tin"));
			WireApi.registerConnectorForRender("ic2_relay_tin", new ResourceLocation("immersiveengineering:block/connector/connector_lv.obj"),
					ImmutableMap.of("#immersiveengineering:blocks/connector_connector_lv",
					IndustrialWires.MODID + ":blocks/ic2_relay_tin"));

			WireApi.registerConnectorForRender("ic2_conn_copper", new ResourceLocation("immersiveengineering:block/connector/connector_lv.obj"),
					ImmutableMap.of("#immersiveengineering:blocks/connector_connector_lv",
					IndustrialWires.MODID + ":blocks/ic2_conn_copper"));
			WireApi.registerConnectorForRender("ic2_relay_copper", new ResourceLocation("immersiveengineering:block/connector/connector_lv.obj"),
					ImmutableMap.of("#immersiveengineering:blocks/connector_connector_lv",
					IndustrialWires.MODID + ":blocks/ic2_relay_copper"));

			WireApi.registerConnectorForRender("ic2_conn_gold", new ResourceLocation("immersiveengineering:block/connector/connector_mv.obj"),
					ImmutableMap.of("#immersiveengineering:blocks/connector_connector_mv",
					IndustrialWires.MODID + ":blocks/ic2_conn_gold"));
			WireApi.registerConnectorForRender("ic2_relay_gold", new ResourceLocation("immersiveengineering:block/connector/connector_mv.obj"),
					ImmutableMap.of("#immersiveengineering:blocks/connector_connector_mv",
					IndustrialWires.MODID + ":blocks/ic2_relay_gold"));

			WireApi.registerConnectorForRender("ic2_conn_hv", new ResourceLocation("immersiveengineering:block/connector/connector_hv.obj"),
					ImmutableMap.of("#immersiveengineering:blocks/connector_connector_hv",
					IndustrialWires.MODID + ":blocks/ic2_conn_hv"));
			WireApi.registerConnectorForRender("ic2_relay_hv", new ResourceLocation("immersiveengineering:block/connector/relay_hv.obj"), null);

			WireApi.registerConnectorForRender("ic2_conn_glass", new ResourceLocation("immersiveengineering:block/connector/connector_hv.obj"),
					ImmutableMap.of("#immersiveengineering:blocks/connector_connector_hv",
					IndustrialWires.MODID + ":blocks/ic2_conn_glass"));
			WireApi.registerConnectorForRender("ic2_relay_glass", new ResourceLocation("immersiveengineering:block/connector/relay_hv.obj"),
					ImmutableMap.of("#immersiveengineering:blocks/connector_relay_hv",
					IndustrialWires.MODID + ":blocks/ic2_relay_glass"));
		}
		WireApi.registerConnectorForRender("rs_panel_conn", new ResourceLocation("industrialwires:block/rs_panel_conn.obj"), null);

		WireApi.registerConnectorForRender("empty", new ResourceLocation("builtin/generated"), null);

		OBJLoader.INSTANCE.addDomain(IndustrialWires.MODID);
		ModelLoaderRegistry.registerLoader(new PanelModelLoader());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityJacobsLadder.class, new TileRenderJacobsLadder());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMarx.class, new TileRenderMarx());
		TileRenderMechMB tesr = new TileRenderMechMB();
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMechMB.class, tesr);
		((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(tesr);
		RenderingRegistry.registerEntityRenderingHandler(EntityBrokenPart.class, EntityRenderBrokenPart::new);

		Shaders.initShaders(true);
	}

	@Override
	public void postInit() {
		super.postInit();
		ManualInstance m = ManualHelper.getManual();
		if (IndustrialWires.hasIC2) {
			PositionedItemStack[][] wireRecipes = new PositionedItemStack[3][10];
			int xBase = 15;
			Ingredient tinCable = IC2TRHelper.getStack("cable", "type:tin,insulation:0");
			List<ItemStack> tinCableList = Arrays.asList(tinCable.getMatchingStacks());
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					wireRecipes[0][3 * i + j] = new PositionedItemStack(tinCableList, 18 * i + xBase, 18 * j);
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
							wireRecipes[i][3 * j1 + j2] = new PositionedItemStack(tinCableList, 18 * j1 + xBase, 18 * j2);
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
		}
		int oldLength = Config.IEConfig.Tools.earDefenders_SoundBlacklist.length;
		Config.IEConfig.Tools.earDefenders_SoundBlacklist =
				Arrays.copyOf(Config.IEConfig.Tools.earDefenders_SoundBlacklist, oldLength + 1);
		Config.IEConfig.Tools.earDefenders_SoundBlacklist[oldLength] = TINNITUS_LOC.toString();

		ClientUtils.mc().getItemColors().registerItemColorHandler((stack, pass) -> {
			if (pass == 1) {
				PanelComponent pc = ItemPanelComponent.componentFromStack(stack);
				if (pc != null) {
					return 0xff000000 | pc.getColor();
				}
			}
			return ~0;
		}, IndustrialWires.panelComponent);

		Config.manual_doubleA.put("iwJacobsUsage", IWConfig.HVStuff.jacobsUsageWatt);
		Config.manual_int.put("iwKeysOnRing", IWConfig.maxKeysOnRing);
		m.addEntry("industrialwires.jacobs", IndustrialWires.MODID,
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
				new ManualPages.Crafting(m, "industrialwires.lock1", new ItemStack(IndustrialWires.key, 1, 2)),
				new ManualPages.Crafting(m, "industrialwires.panel_meter", new ItemStack(IndustrialWires.panelComponent, 1, 8)),
				new ManualPages.Crafting(m, "industrialwires.7seg", new ItemStack(IndustrialWires.panelComponent, 1, 9))
		);
		List<MarxOreHandler.OreInfo> ores = MarxOreHandler.getRecipes();
		String text = I18n.format("ie.manual.entry.industrialwires.marx");
		for (int i = 0; i < ores.size(); i++) {
			MarxOreHandler.OreInfo curr = ores.get(i);
			if (!curr.exampleInput.isEmpty()) {
				text += I18n.format(IndustrialWires.MODID + ".desc.input") + ": §l" + curr.exampleInput.get(0).getDisplayName() + "§r<br>";
				text += I18n.format(IndustrialWires.MODID + ".desc.output") + ": " + Utils.formatDouble(curr.maxYield, "0.#") + "x" + curr.output.get().getDisplayName() + "<br>";
				if (curr.outputSmall != null && !curr.outputSmall.get().isEmpty()) {
					text += I18n.format(IndustrialWires.MODID + ".desc.alt") + ": " + curr.smallMax + "x" + curr.outputSmall.get().getDisplayName() + "<br>";
				}
				text += I18n.format(IndustrialWires.MODID + ".desc.ideal_e") + ": " + Utils.formatDouble(curr.avgEnergy * MarxOreHandler.defaultEnergy / 1000, "0.#") + " kJ<br><br>";
			}
		}
		boolean uni = m.fontRenderer.getUnicodeFlag();
		m.fontRenderer.setUnicodeFlag(true);
		m.entryRenderPre();
		TextSplitter splitter = new TextSplitter(m.fontRenderer::getStringWidth, 16, 120,
				(s) -> new ManualPages.Text(m, s));
		splitter.addSpecialPage(0, 0, 6,
				(s) -> new ManualPageMultiblock(m, s,
						MultiblockMarx.INSTANCE));
		splitter.split(text);
		m.entryRenderPost();
		m.fontRenderer.setUnicodeFlag(uni);
		List<ManualPages> marxEntry = splitter.toManualEntry();
		m.addEntry("industrialwires.marx", IndustrialWires.MODID, marxEntry.toArray(new ManualPages[0]));
		ClientCommandHandler.instance.registerCommand(new CommandIWClient());
	}

	private static final ResourceLocation TINNITUS_LOC = new ResourceLocation(IndustrialWires.MODID, "tinnitus");
	private static ISound playingTinnitus = null;
	@Override
	public void startTinnitus() {
		final Minecraft mc = Minecraft.getMinecraft();
		if (playingTinnitus==null||!mc.getSoundHandler().isSoundPlaying(playingTinnitus)) {
			playingTinnitus = getTinnitus();
			mc.getSoundHandler().playSound(playingTinnitus);
		}
	}

	private ISound getTinnitus() {
		final Minecraft mc = Minecraft.getMinecraft();
		return  new MovingSound(new SoundEvent(TINNITUS_LOC), SoundCategory.PLAYERS) {
			@Override
			public void update() {
				if (mc.player.getActivePotionEffect(IWPotions.tinnitus)==null) {
					donePlaying = true;
					playingTinnitus = null;
				}
			}

			@Override
			public float getVolume() {
				return .5F;
			}

			@Override
			public float getXPosF() {
				return (float) mc.player.posX;
			}

			@Override
			public float getYPosF() {
				return (float) mc.player.posY;
			}

			@Override
			public float getZPosF() {
				return (float) mc.player.posZ;
			}

			@Override
			public boolean canRepeat() {
				return true;
			}
		};
	}

	@Override
	public World getClientWorld() {
		return Minecraft.getMinecraft().world;
	}

	private Map<BlockPos, List<ISound>> playingSounds = new HashMap<>();
	private static ResourceLocation jacobsStart = new ResourceLocation(IndustrialWires.MODID, "jacobs_ladder_start");//~470 ms ~=9 ticks
	private static ResourceLocation jacobsMiddle = new ResourceLocation(IndustrialWires.MODID, "jacobs_ladder_middle");
	private static ResourceLocation jacobsEnd = new ResourceLocation(IndustrialWires.MODID, "jacobs_ladder_end");//~210 ms ~= 4 ticks
	private static ResourceLocation marxBang = new ResourceLocation(IndustrialWires.MODID, "marx_bang");
	private static ResourceLocation marxPop = new ResourceLocation(IndustrialWires.MODID, "marx_pop");
	private static ResourceLocation turnFast = new ResourceLocation(IndustrialWires.MODID, "mech_mb_fast");
	private static ResourceLocation turnSlow = new ResourceLocation(IndustrialWires.MODID, "mech_mb_slow");

	@Override
	public void playJacobsLadderSound(TileEntityJacobsLadder te, int phase, Vec3d soundPos) {
		stopAllSoundsExcept(te.getPos(), ImmutableSet.of());
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
		PositionedSoundRecord sound = new PositionedSoundRecord(event, SoundCategory.BLOCKS, te.size.soundVolume, 1,
				false, 0, ISound.AttenuationType.LINEAR, (float) soundPos.x, (float) soundPos.y, (float) soundPos.z);
		ClientUtils.mc().getSoundHandler().playSound(sound);
		addSound(te.getPos(), sound);
	}

	@Override
	public void updateMechMBTurningSound(TileEntityMechMB te, MechEnergy energy) {
		final double MIN_SPEED = 5;
		Set<ISound> added = new HashSet<>();
		if (energy.getSpeed() > MIN_SPEED) {
			boolean adjusting = energy.isAdjusting();
			double speedToUse = energy.getSpeed()-MIN_SPEED;//Volume should be zero by the time the sound stops
			float lambda = MathHelper.clamp((float) speedToUse / 50 - .5F, 0, 1);
			float totalVolume = (float) (energy.weight / 20e3 * Math.tanh(speedToUse/30));
			totalVolume = Math.min(totalVolume, 1.5F);
			float pitch = (float) Math.min(Math.sqrt(speedToUse), 3);
			if (lambda > 0) {
				PositionedSoundRecord sound = new PositionedSoundRecord(turnFast, SoundCategory.BLOCKS, lambda * totalVolume, pitch,
						!adjusting, 0, ISound.AttenuationType.LINEAR, te.getPos().getX(), te.getPos().getY(),
						te.getPos().getZ());
				ClientUtils.mc().getSoundHandler().playSound(sound);
				addSound(te.getPos(), sound);
				added.add(sound);
			}
			if (lambda < 1) {
				PositionedSoundRecord sound = new PositionedSoundRecord(turnSlow, SoundCategory.BLOCKS, (1 - lambda) * totalVolume, pitch,
						!adjusting, 0, ISound.AttenuationType.LINEAR, te.getPos().getX(), te.getPos().getY(),
						te.getPos().getZ());
				ClientUtils.mc().getSoundHandler().playSound(sound);
				addSound(te.getPos(), sound);
				added.add(sound);
			}
		}
		stopAllSoundsExcept(te.getPos(), added);
	}

	@Override
	public void playMarxBang(TileEntityMarx te, Vec3d pos, float energy) {
		ResourceLocation soundLoc = marxBang;
		if (energy<0) {
			energy = -energy;
			soundLoc = marxPop;
		}
		PositionedSoundRecord sound = new PositionedSoundRecord(soundLoc, SoundCategory.BLOCKS, 5*energy, 1,
				false, 0, ISound.AttenuationType.LINEAR, (float) pos.x, (float) pos.y, (float) pos.z);
		ClientUtils.mc().getSoundHandler().playSound(sound);
		addSound(te.getPos(), sound);
	}

	private void addSound(BlockPos pos, ISound sound) {
		List<ISound> allForPos = playingSounds.get(pos);
		if (allForPos==null) {
			allForPos = new ArrayList<>();
		}
		allForPos.add(sound);
		if (allForPos.size()==1) {
			playingSounds.put(pos, allForPos);
		}
	}

	@Override
	public void stopAllSoundsExcept(BlockPos pos, Set<ISound> excluded) {
		if (playingSounds.containsKey(pos)) {
			SoundHandler manager = Minecraft.getMinecraft().getSoundHandler();
			List<ISound> sounds = playingSounds.get(pos);
			List<ISound> toRemove = new ArrayList<>(sounds.size()-excluded.size());
			for (ISound sound:sounds) {
				if (!excluded.contains(sound)) {
					manager.stopSound(sound);
					toRemove.add(sound);
				}
			}
			sounds.removeAll(toRemove);
			if (sounds.isEmpty()) {
				playingSounds.remove(pos);
			}
		}
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

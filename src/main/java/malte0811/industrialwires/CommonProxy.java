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

import malte0811.industrialwires.blocks.controlpanel.TileEntityPanelCreator;
import malte0811.industrialwires.blocks.controlpanel.TileEntityRSPanel;
import malte0811.industrialwires.blocks.converter.TileEntityMechMB;
import malte0811.industrialwires.blocks.hv.TileEntityJacobsLadder;
import malte0811.industrialwires.blocks.hv.TileEntityMarx;
import malte0811.industrialwires.containers.ContainerPanelComponent;
import malte0811.industrialwires.containers.ContainerPanelCreator;
import malte0811.industrialwires.containers.ContainerRSPanelConn;
import malte0811.industrialwires.containers.ContainerRenameKey;
import malte0811.industrialwires.mech_mb.MechEnergy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import java.util.Set;

public class CommonProxy implements IGuiHandler {
	public void preInit() {
	}

	public void postInit() {
	}

	public World getClientWorld() {
		return null;
	}

	public void playJacobsLadderSound(TileEntityJacobsLadder te, int phase, Vec3d soundPos) {
	}
	public void startTinnitus() {
	}

	@Override
	public Container getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID == 0) {//TILE GUI
			TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
			if (te instanceof TileEntityPanelCreator) {
				return new ContainerPanelCreator(player.inventory, (TileEntityPanelCreator) te);
			}
			if (te instanceof TileEntityRSPanel) {
				return new ContainerRSPanelConn((TileEntityRSPanel) te);
			}
		} else if (ID == 1) {//ITEM GUI
			EnumHand h = z == 1 ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
			ItemStack held = player.getHeldItem(h);
			if (!held.isEmpty()) {
				if (held.getItem() == IndustrialWires.panelComponent) {
					return new ContainerPanelComponent(h);
				} else if (held.getItem() == IndustrialWires.key) {
					return new ContainerRenameKey(h);
				}
			}
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return null;
	}

	public void playMarxBang(TileEntityMarx tileEntityMarx, Vec3d vec3d, float energy) {}

	public void updateMechMBTurningSound(TileEntityMechMB te, MechEnergy energy) {}

	public void stopAllSoundsExcept(BlockPos pos, Set<?> excluded) {}

	public boolean isSingleplayer() {
		return false;
	}

	public boolean isValidTextureSource(ItemStack stack) {
		return stack.getItem() instanceof ItemBlock;
	}
}

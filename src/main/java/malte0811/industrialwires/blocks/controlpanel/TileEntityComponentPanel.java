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

package malte0811.industrialwires.blocks.controlpanel;

import malte0811.industrialwires.controlpanel.ControlPanelNetwork;
import malte0811.industrialwires.controlpanel.PanelComponent;
import malte0811.industrialwires.controlpanel.PropertyComponents;
import malte0811.industrialwires.items.ItemPanelComponent;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

import static malte0811.industrialwires.util.MiscUtils.apply;

public class TileEntityComponentPanel extends TileEntityPanel {
	private byte rsOut = 0;
	public TileEntityComponentPanel() {
		components = new PropertyComponents.AABBPanelProperties();
		panelNetwork = new SingleCompNetwork();
	}

	@Override
	public void onLoad() {
		super.onLoad();
		if (!world.isRemote) {
			updateRSInput();
		}
	}

	public void updateRSInput() {
		int value = world.isBlockIndirectlyGettingPowered(pos);
		if (value == 0) {
			for (EnumFacing f : EnumFacing.HORIZONTALS) {
				IBlockState state = world.getBlockState(pos.offset(f));
				if (state.getBlock() == Blocks.REDSTONE_WIRE && state.getValue(BlockRedstoneWire.POWER) > value)
					value = state.getValue(BlockRedstoneWire.POWER);
			}
		}
		((SingleCompNetwork)panelNetwork).setGlobalInput((byte) value);
	}

	public void markBlockForUpdate(BlockPos pos)
	{
		if (world!=null) {
			IBlockState state = world.getBlockState(getBlockPos());
			world.notifyBlockUpdate(pos, state, state, 3);
			world.notifyNeighborsOfStateChange(pos, state.getBlock(), true);
		}
	}

	@Override
	public AxisAlignedBB getBoundingBox() {
		if (defAABB == null) {
			AxisAlignedBB base = ((PropertyComponents.AABBPanelProperties)components).getPanelBoundingBox();
			defAABB = apply(components.getPanelBaseTransform(), base.setMaxY(components.getMaxHeight()));
		}
		return defAABB;
	}

	public int getRSOutput() {
		return rsOut;
	}

	@Nonnull
	@Override
	public ItemStack getTileDrop(EntityPlayer player, @Nonnull IBlockState state) {
		if (components.size()<1) {
			return ItemStack.EMPTY;
		}
		return ItemPanelComponent.stackFromComponent(components.get(0));
	}

	@Override
	public boolean canJoinNetwork() {
		return false;
	}

	public void setComponent(PanelComponent comp) {
		components.clear();
		components.add(comp);
		comp.setPanel(this);
		comp.setNetwork(panelNetwork);
	}

	private class SingleCompNetwork extends ControlPanelNetwork {
		@Override
		public void setOutputs(IOwner owner, RSChannelState... out) {
			super.setOutputs(owner, out);
			byte oldOut = rsOut;
			rsOut = 0;
			for (OutputValue s:activeOutputs.values()) {
				rsOut = (byte) Math.max(rsOut, s.getTargetState().getStrength());
			}
			if (oldOut!=rsOut) {
				markBlockForUpdate(pos);
			}
		}

		public void setGlobalInput(byte value) {
			for (RSChannel channel: listeners.keySet()) {
				RSChannelState state = new RSChannelState(channel, value);
				for (ChangeListener l:listeners.get(channel)) {
					l.onChange(state);
				}
			}
		}
	}
}

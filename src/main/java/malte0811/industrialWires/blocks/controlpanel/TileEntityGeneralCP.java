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

package malte0811.industrialWires.blocks.controlpanel;

import malte0811.industrialWires.blocks.TileEntityIWBase;
import malte0811.industrialWires.controlpanel.ControlPanelNetwork;
import malte0811.industrialWires.controlpanel.ControlPanelNetwork.IOwner;
import malte0811.industrialWires.util.MiscUtils;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

public abstract class TileEntityGeneralCP extends TileEntityIWBase implements IOwner {
	@Nonnull
	protected ControlPanelNetwork panelNetwork = new ControlPanelNetwork();

	public void setNetworkAndInit(ControlPanelNetwork newNet) {
		panelNetwork = newNet;
	}

	@Override
	public void onLoad() {
		super.onLoad();
		if (!world.isRemote) {
			boolean isFinalNet = false;
			for (EnumFacing side : EnumFacing.VALUES) {
				BlockPos posSide = pos.offset(side);
				TileEntityGeneralCP neighbour = MiscUtils.getExistingTE(world, posSide, TileEntityGeneralCP.class);
				if (neighbour != null) {
					if (!isFinalNet) {
						panelNetwork = neighbour.panelNetwork;
						panelNetwork.addMember(this);
						isFinalNet = true;
					} else {
						neighbour.panelNetwork.replaceWith(panelNetwork, world);
					}
				}
			}
			if (!isFinalNet) {
				panelNetwork.addMember(this);
			}
		}
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		panelNetwork.removeMember(pos, world);
	}

	@Override
	public void invalidate() {
		super.invalidate();
		panelNetwork.removeMember(pos, world);
	}

	@Override
	public BlockPos getBlockPos() {
		return pos;
	}
}

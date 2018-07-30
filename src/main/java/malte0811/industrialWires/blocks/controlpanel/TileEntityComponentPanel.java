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

public class TileEntityComponentPanel extends TileEntityPanel {
	/*private int rsOut = 0; todo
	private Consumer<byte[]> rsIn;
	public TileEntityComponentPanel() {
		components = new PropertyComponents.AABBPanelProperties();
	}

	@Override
	public void update() {
		for (PanelComponent pc : components) {
			pc.update();
		}
		if (!world.isRemote) {
			if (firstTick&&components.size()>0) {
				PanelComponent pc = components.get(0);
				pc.registerRSOutput(-1, (channel, value, pcTmp)->{
					rsOut = value;
					if (!isInvalid()) {
						markBlockForUpdate(pos);
						markBlockForUpdate(pos.offset(components.getTop(), -1));
					}
				});
				rsIn = pc.getRSInputHandler(-1, this);
				updateRS();
				firstTick = false;
			}
		}
	}

	public void updateRS() {
		if (rsIn != null) {
			int value = world.isBlockIndirectlyGettingPowered(pos);
			if (value == 0) {
				for (EnumFacing f : EnumFacing.HORIZONTALS) {
					IBlockState state = world.getBlockState(pos.offset(f));
					if (state.getBlock() == Blocks.REDSTONE_WIRE && state.getValue(BlockRedstoneWire.POWER) > value)
						value = state.getValue(BlockRedstoneWire.POWER);
				}
			}
			byte[] tmp = new byte[16];
			for (int i = 0; i < tmp.length; i++) {
				tmp[i] = (byte) value;
			}
			rsIn.accept(tmp);
		}
	}

	public void markBlockForUpdate(BlockPos pos)
	{
		IBlockState state = world.getBlockState(getBlockPos());
		world.notifyBlockUpdate(pos,state,state,3);
		world.notifyNeighborsOfStateChange(pos, state.getBlock(), true);
	}

	@Override
	public AxisAlignedBB getBoundingBox() {
		if (defAABB == null) {
			AxisAlignedBB base = ((PropertyComponents.AABBPanelProperties)components).getPanelBoundingBox();
			defAABB = apply(components.getPanelBaseTransform(), base.setMaxY(components.getMaxHeight()));
		}
		return defAABB;
	}

	@Override
	public void registerRS(TileEntityRSPanelConn te) {
		//NO-OP
	}

	@Override
	public void unregisterRS(TileEntityRSPanelConn te) {
		//NO-OP
	}

	@Override
	public boolean interactsWithRSWires() {
		return false;
	}

	public int getRSOutput() {
		return rsOut;
	}

	@Nonnull
	@Override
	public ItemStack getTileDrop(@Nonnull EntityPlayer player, @Nonnull IBlockState state) {
		if (components.size()<1) {
			return ItemStack.EMPTY;
		}
		return ItemPanelComponent.stackFromComponent(components.get(0));
	}
*/}

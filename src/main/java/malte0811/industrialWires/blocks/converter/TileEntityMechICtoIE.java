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
package malte0811.industrialWires.blocks.converter;

import blusunrize.immersiveengineering.api.energy.IRotationAcceptor;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import ic2.api.energy.tile.IKineticSource;
import malte0811.industrialWires.IWConfig.MechConversion;
import malte0811.industrialWires.blocks.TileEntityIWBase;
import malte0811.industrialWires.util.ConversionUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

import static malte0811.industrialWires.util.NBTKeys.BUFFER;
import static malte0811.industrialWires.util.NBTKeys.DIRECTION;

public class TileEntityMechICtoIE extends TileEntityIWBase implements IDirectionalTile, ITickable {
	private EnumFacing dir = EnumFacing.DOWN;
	private int kinBuffer = 0;
	private BlockPos to;
	private BlockPos from;

	@Override
	public void update() {
		if (!world.isRemote) {
			if (to == null) {
				to = pos.offset(dir);
			}
			if (from == null) {
				from = pos.offset(dir, -1);
			}
			TileEntity teFrom = world.getTileEntity(from);
			if (teFrom instanceof IKineticSource) {
				int sourceMax = ((IKineticSource) teFrom).maxrequestkineticenergyTick(dir);
				int draw = Math.min(2 * MechConversion.maxKinToRot - kinBuffer, sourceMax);
				if (draw > 0) {
					kinBuffer += ((IKineticSource) teFrom).requestkineticenergy(dir, draw) * MechConversion.kinToRotEfficiency;
				}
			}
			TileEntity teTo = world.getTileEntity(to);
			if (kinBuffer > 0 && teTo instanceof IRotationAcceptor) {
				double out = Math.min(ConversionUtil.rotPerKin() * MechConversion.maxKinToRot,
						ConversionUtil.rotPerKin() * kinBuffer);
				((IRotationAcceptor) teTo).inputRotation(out, dir);
				kinBuffer -= out * ConversionUtil.kinPerRot();
			}
		}
	}

	@Override
	public void writeNBT(NBTTagCompound out, boolean updatePacket) {
		out.setByte(DIRECTION, (byte) dir.getIndex());
		out.setInteger(BUFFER, kinBuffer);
	}

	@Override
	public void readNBT(NBTTagCompound in, boolean updatePacket) {
		dir = EnumFacing.VALUES[in.getByte(DIRECTION)];
		kinBuffer = in.getInteger(BUFFER);
		to = null;
		from = null;
	}

	// Directional
	@Nonnull
	@Override
	public EnumFacing getFacing() {
		return dir;
	}

	@Override
	public void setFacing(@Nonnull EnumFacing facing) {
		dir = facing;
		to = null;
		from = null;
		markDirty();
	}

	@Override
	public int getFacingLimitation() {
		return 1;
	}

	@Override
	public boolean mirrorFacingOnPlacement(@Nonnull EntityLivingBase placer) {
		return false;
	}

	@Override
	public boolean canHammerRotate(@Nonnull EnumFacing side, float hitX, float hitY, float hitZ, @Nonnull EntityLivingBase entity) {
		return true;
	}

	@Override
	public boolean canRotate(@Nonnull EnumFacing axis) {
		return true;
	}
}

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
package malte0811.industrialwires.blocks.converter;

import blusunrize.immersiveengineering.api.energy.IRotationAcceptor;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import ic2.api.energy.tile.IKineticSource;
import malte0811.industrialwires.IWConfig.MechConversion;
import malte0811.industrialwires.blocks.TileEntityIWBase;
import malte0811.industrialwires.util.ConversionUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;

import static malte0811.industrialwires.IWConfig.MechConversion.maxRotToKin;
import static malte0811.industrialwires.util.NBTKeys.BUFFER;
import static malte0811.industrialwires.util.NBTKeys.DIRECTION;

public class TileEntityMechIEtoIC extends TileEntityIWBase implements IDirectionalTile, IRotationAcceptor, IKineticSource {
	private EnumFacing dir = EnumFacing.DOWN;
	private double rotBuffer = 0;

	@Override
	public void writeNBT(NBTTagCompound out, boolean updatePacket) {
		out.setByte(DIRECTION, (byte) dir.getIndex());
		out.setDouble(BUFFER, rotBuffer);
	}

	@Override
	public void readNBT(NBTTagCompound in, boolean updatePacket) {
		dir = EnumFacing.VALUES[in.getByte(DIRECTION)];
		rotBuffer = in.getDouble(BUFFER);
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
	@Deprecated
	public int maxrequestkineticenergyTick(EnumFacing enumFacing) {
		return 0;
	}

	//IC2 kinetic
	@Override
	public int getConnectionBandwidth(EnumFacing f) {
		if (f == dir) {
			return (int) (ConversionUtil.kinPerRot() * rotBuffer);
		} else {
			return 0;
		}
	}

	@Override
	@Deprecated
	public int requestkineticenergy(EnumFacing enumFacing, int i) {
		return 0;
	}

	@Override
	public int drawKineticEnergy(EnumFacing f, int requested, boolean simulate) {
		if (f == dir) {
			int stored = (int) (ConversionUtil.kinPerRot() * rotBuffer);
			int out = Math.min(requested, stored);
			if (!simulate) {
				rotBuffer -= out * ConversionUtil.rotPerKin();
			}
			return (int) (out * MechConversion.rotToKinEfficiency);
		} else {
			return 0;
		}
	}

	//IE rotation
	@Override
	public void inputRotation(double rotation, @Nonnull EnumFacing side) {
		if (side == dir) {
			rotBuffer = Math.min(Math.max(rotBuffer, rotation), maxRotToKin);
		}
	}

	@Override
	public boolean canRotate(@Nonnull EnumFacing axis) {
		return true;
	}
}

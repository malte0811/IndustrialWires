/*******************************************************************************
 * This file is part of Industrial Wires.
 * Copyright (C) 2016 malte0811
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
 *******************************************************************************/
package malte0811.industrialWires.blocks.converter;

import blusunrize.immersiveengineering.api.energy.IRotationAcceptor;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import ic2.api.energy.tile.IKineticSource;
import malte0811.industrialWires.IWConfig.MechConversion;
import malte0811.industrialWires.blocks.TileEntityIWBase;
import malte0811.industrialWires.util.ConversionUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public class TileEntityMechIEtoIC extends TileEntityIWBase implements IDirectionalTile, IRotationAcceptor, IKineticSource {
	EnumFacing dir = EnumFacing.DOWN;
	double rotBuffer = 0;
	private static final double rotBufMax = 2*MechConversion.maxRotToKin;
	private static final int maxOutput = (int)(ConversionUtil.kinPerRot()*MechConversion.maxRotToKin);
	
	@Override
	public void writeNBT(NBTTagCompound out, boolean updatePacket) {
		out.setByte(DIR_TAG, (byte) dir.getIndex());
		out.setDouble(BUFFER_TAG, rotBuffer);
	}

	@Override
	public void readNBT(NBTTagCompound in, boolean updatePacket) {
		dir = EnumFacing.VALUES[in.getByte(DIR_TAG)];
		rotBuffer = in.getDouble(BUFFER_TAG);
	}
	// Directional
	@Override
	public EnumFacing getFacing() {
		return dir;
	}
	@Override
	public void setFacing(EnumFacing facing) {
		dir = facing;
		markDirty();
	}
	@Override
	public int getFacingLimitation() {
		return 0;
	}
	@Override
	public boolean mirrorFacingOnPlacement(EntityLivingBase placer) {
		return false;
	}
	@Override
	public boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity) {
		return true;
	}
	//IC2 kinetic
	@Override
	public int maxrequestkineticenergyTick(EnumFacing f) {
		if (f==dir) {
			return maxOutput;
		} else {
			return 0;
		}
	}

	@Override
	public int requestkineticenergy(EnumFacing f, int requested) {
		if (f==dir) {
			int stored = (int) (ConversionUtil.kinPerRot()*rotBuffer);
			int out = Math.min(maxOutput, stored);
			out = Math.min(requested, out);
			rotBuffer -= out*ConversionUtil.rotPerKin();
			return (int)(out*MechConversion.rotToKinEfficiency);
		} else {
			return 0;
		}
	}
	
	//IE rotation
	@Override
	public void inputRotation(double rotation, EnumFacing side) {
		if (side==dir) {
			rotBuffer = Math.min(rotBufMax, rotBuffer+rotation);
		}
	}
}

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
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxReceiver;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import malte0811.industrialWires.IWConfig.MechConversion;
import malte0811.industrialWires.blocks.EnergyAdapter;
import malte0811.industrialWires.blocks.TileEntityIWBase;
import malte0811.industrialWires.util.ConversionUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;

public class TileEntityIEMotor extends TileEntityIWBase implements ITickable, IFluxReceiver, IDirectionalTile {
	public final double bufferMax = 2*MechConversion.maxIfToMech*ConversionUtil.rotPerIf();

	private double rotBuffer = 0;
	private FluxStorage energy = new FluxStorage(20*MechConversion.maxIfToMech, 2*MechConversion.maxIfToMech);
	private EnumFacing dir = EnumFacing.DOWN;
	private BlockPos receiver;
	@Override
	public void update() {
		if (!worldObj.isRemote) {
			if (receiver==null) {
				receiver = pos.offset(dir);
			}
			int max = MechConversion.maxIfToMech;
			boolean dirty = false;
			if (rotBuffer<bufferMax&&energy.extractEnergy(max, true)>0) {
				int extracted = energy.extractEnergy(max, false);
				rotBuffer += extracted*ConversionUtil.rotPerIf()*MechConversion.ifMotorEfficiency;
				dirty = true;
			}
			TileEntity te = worldObj.getTileEntity(receiver);
			if (te instanceof IRotationAcceptor) {
				((IRotationAcceptor)te).inputRotation(rotBuffer, dir);
				rotBuffer = 0;
				dirty = true;
			}
			if (dirty) {
				markDirty();
			}
		}
	}
	@Override
	public void readNBT(NBTTagCompound in, boolean updatePacket) {
		dir = EnumFacing.VALUES[in.getByte(DIR_TAG)];
		energy.readFromNBT(in.getCompoundTag(ENERGY_TAG));
		receiver = null;
		rotBuffer = in.getDouble(BUFFER_TAG);
	}
	@Override
	public void writeNBT(NBTTagCompound out, boolean updatePacket) {
		out.setByte(DIR_TAG, (byte) dir.getIndex());
		NBTTagCompound nbt = new NBTTagCompound();
		energy.writeToNBT(nbt);
		out.setTag(ENERGY_TAG, nbt);
		out.setDouble(BUFFER_TAG, rotBuffer);
	}

	// Flux energy
	@Override
	public boolean canConnectEnergy(EnumFacing from) {
		return from==dir.getOpposite()||from==null;
	}
	@Override
	public int receiveEnergy(EnumFacing from, int energyIn, boolean simulate) {
		if (canConnectEnergy(from)) {
			int ret = energy.receiveEnergy(energyIn, simulate);
			markDirty();
			return ret;
		} else {
			return 0;
		}
	}
	@Override
	public int getEnergyStored(EnumFacing from) {
		return energy.getEnergyStored();
	}
	@Override
	public int getMaxEnergyStored(EnumFacing from) {
		return energy.getMaxEnergyStored();
	}

	// Directional
	@Override
	public EnumFacing getFacing() {
		return dir;
	}
	@Override
	public void setFacing(EnumFacing facing) {
		dir = facing;
		receiver = null;
		markDirty();
	}
	@Override
	public int getFacingLimitation() {
		return 1;
	}
	@Override
	public boolean mirrorFacingOnPlacement(EntityLivingBase placer) {
		return false;
	}
	@Override
	public boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity) {
		return true;
	}
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if (capability==CapabilityEnergy.ENERGY&&canConnectEnergy(facing)) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability==CapabilityEnergy.ENERGY&&canConnectEnergy(facing)) {
			return (T) new EnergyAdapter(this, facing);
		}
		return super.getCapability(capability, facing);
	}
}

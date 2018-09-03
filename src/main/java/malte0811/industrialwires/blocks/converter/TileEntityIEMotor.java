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
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxReceiver;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import malte0811.industrialwires.IWConfig.MechConversion;
import malte0811.industrialwires.blocks.EnergyAdapter;
import malte0811.industrialwires.blocks.TileEntityIWBase;
import malte0811.industrialwires.util.ConversionUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

import static malte0811.industrialwires.util.NBTKeys.*;

public class TileEntityIEMotor extends TileEntityIWBase implements ITickable, IFluxReceiver, IDirectionalTile {
	private double rotBuffer = 0;
	private FluxStorage energy = new FluxStorage(20 * MechConversion.maxIfToMech, 2 * MechConversion.maxIfToMech);
	private EnumFacing dir = EnumFacing.DOWN;
	private BlockPos receiver;

	@Override
	public void update() {
		if (!world.isRemote) {
			if (receiver == null) {
				receiver = pos.offset(dir);
			}
			int max = MechConversion.maxIfToMech;
			boolean dirty = false;
			if (rotBuffer < 2 * MechConversion.maxIfToMech * ConversionUtil.rotPerIf()
					&& energy.extractEnergy(max, true) > 0) {
				int extracted = energy.extractEnergy(max, false);
				rotBuffer += extracted * ConversionUtil.rotPerIf() * MechConversion.ifMotorEfficiency;
				dirty = true;
			}
			TileEntity te = world.getTileEntity(receiver);
			if (te instanceof IRotationAcceptor) {
				((IRotationAcceptor) te).inputRotation(rotBuffer, dir);
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
		dir = EnumFacing.VALUES[in.getByte(DIRECTION)];
		energy.readFromNBT(in.getCompoundTag(ENERGY));
		receiver = null;
		rotBuffer = in.getDouble(BUFFER);
	}

	@Override
	public void writeNBT(NBTTagCompound out, boolean updatePacket) {
		out.setByte(DIRECTION, (byte) dir.getIndex());
		NBTTagCompound nbt = new NBTTagCompound();
		energy.writeToNBT(nbt);
		out.setTag(ENERGY, nbt);
		out.setDouble(BUFFER, rotBuffer);
	}

	// Flux energy
	@Override
	public boolean canConnectEnergy(EnumFacing from) {
		return from == dir.getOpposite() || from == null;
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
	@Nonnull
	@Override
	public EnumFacing getFacing() {
		return dir;
	}

	@Override
	public void setFacing(@Nonnull EnumFacing facing) {
		dir = facing;
		receiver = null;
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
	public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY && canConnectEnergy(facing)) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	private Map<EnumFacing, IEnergyStorage> energies = new HashMap<>();
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY && canConnectEnergy(facing)) {
			if (!energies.containsKey(facing))
				energies.put(facing, new EnergyAdapter(this, facing));
			return CapabilityEnergy.ENERGY.cast(energies.get(facing));
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public boolean canRotate(@Nonnull EnumFacing axis) {
		return true;
	}
}

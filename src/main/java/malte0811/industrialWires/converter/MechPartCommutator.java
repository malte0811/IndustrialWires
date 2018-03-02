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

package malte0811.industrialWires.converter;

import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.converter.MechanicalMBBlockType;
import malte0811.industrialWires.converter.EUCapability.IC2EnergyHandler;
import malte0811.industrialWires.util.ConversionUtil;
import malte0811.industrialWires.util.LocalSidedWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.IEnergyStorage;

import static malte0811.industrialWires.util.NBTKeys.*;
import static net.minecraftforge.energy.CapabilityEnergy.ENERGY;

public class MechPartCommutator extends MechMBPart implements IMBPartElectric {
	private double bufferToMB;
	private Waveform wfToMB = Waveform.NONE;
	private double bufferToWorld;
	private Waveform wfToWorld = Waveform.NONE;
	@Override
	public Waveform getProduced(MechEnergy state) {

		return wfToMB.getCommutated(state.getSpeed());
	}

	@Override
	public double getAvailableEEnergy() {
		return bufferToMB;
	}

	@Override
	public void extractEEnergy(double energy) {
		bufferToMB -= energy;
	}

	@Override
	public double requestEEnergy(Waveform waveform, MechEnergy energy) {
		if (waveform == wfToWorld.getCommutated(energy.getSpeed())) {
			return getMaxBuffer() - bufferToWorld;
		}
		else {
			return getMaxBuffer();
		}
	}

	@Override
	public void insertEEnergy(double given, Waveform waveform, MechEnergy energy) {
		waveform = waveform.getCommutated(energy.getSpeed());
		if (waveform!=wfToWorld) {
			wfToWorld = waveform;
			bufferToWorld = 0;
		}
		bufferToWorld += given;
	}

	private final IC2EnergyHandler capIc2 = new IC2EnergyHandler() {
		{
			tier = 3;//TODO does this mean everything blows up?
		}
		@Override
		public boolean acceptsEnergyFrom(EnumFacing side) {
			return side==EnumFacing.UP&&bufferToMB<getMaxBuffer();
		}

		@Override
		public boolean emitsEnergyTo(EnumFacing side) {
			return side==EnumFacing.UP&&bufferToWorld>0;
		}

		@Override
		public double injectEnergy(EnumFacing side, double amount, double voltage) {
			double buffer = bufferToMB;
			double input = amount * ConversionUtil.joulesPerEu();
			if (!wfToMB.isDC()) {
				buffer = 0;
			}
			input = Math.min(input, getMaxBuffer()-buffer);
			buffer += input;
			bufferToMB = buffer;
			wfToMB = Waveform.AC_ASYNC;
			return amount-ConversionUtil.euPerJoule()*input;
		}

		@Override
		public double getOfferedEnergy() {
			if (wfToWorld.isDC()) {
				return ConversionUtil.euPerJoule()*bufferToWorld;
			}
			return 0;
		}

		@Override
		public void drawEnergy(double amount) {
			bufferToWorld -= ConversionUtil.joulesPerEu()*amount;
		}
	};

	private IEnergyStorage capForge = new IEnergyStorage() {
		@Override
		public int receiveEnergy(int maxReceive, boolean simulate) {
			double buffer = bufferToMB;
			double input = maxReceive* ConversionUtil.joulesPerIf();
			if (!wfToMB.isAC()) {
				buffer = 0;
			}
			input = Math.min(input, getMaxBuffer()-buffer);
			buffer += input;
			if (!simulate) {
				bufferToMB = buffer;
				wfToMB = Waveform.AC_ASYNC;
			}
			return (int) (ConversionUtil.ifPerJoule()*input);
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate) {
			if (!wfToWorld.isAC()) {
				return 0;
			}
			double buffer = bufferToWorld;
			double output = maxExtract* ConversionUtil.joulesPerIf();
			output = Math.min(output, getMaxBuffer()-buffer);
			buffer += output;
			if (!simulate) {
				bufferToWorld = buffer;
			}
			return (int) (ConversionUtil.ifPerJoule()*output);
		}

		@Override
		public int getEnergyStored() {
			return (int) (ConversionUtil.ifPerJoule()*(bufferToWorld+bufferToMB));
		}

		@Override
		public int getMaxEnergyStored() {
			return (int)(2*ConversionUtil.ifPerJoule()*getMaxBuffer());
		}

		@Override
		public boolean canExtract() {
			return true;
		}

		@Override
		public boolean canReceive() {
			return true;
		}
	};

	@Override
	public <T> T getCapability(Capability<T> cap, EnumFacing side, Vec3i pos) {
		if (cap==EUCapability.ENERGY_IC2) {
			return EUCapability.ENERGY_IC2.cast(capIc2);
		}
		if (side==EnumFacing.UP&&cap== ENERGY) {
			return ENERGY.cast(capForge);
		}
		return super.getCapability(cap, side, pos);
	}

	@Override
	public <T> boolean hasCapability(Capability<T> cap, EnumFacing side, Vec3i pos) {
		if (cap==EUCapability.ENERGY_IC2) {
			return true;
		}
		if (side==EnumFacing.UP&&cap== ENERGY) {
			return true;
		}
		return super.hasCapability(cap, side, pos);
	}

	@Override
	public void createMEnergy(MechEnergy e) {}

	@Override
	public double requestMEnergy(MechEnergy e) {
		return 0;
	}

	@Override
	public void insertMEnergy(double added) {}

	@Override
	public double getInertia() {
		return 50;
	}

	@Override
	public double getSpeedFor15RS() {
		return Double.MAX_VALUE;
	}

	@Override
	public void writeToNBT(NBTTagCompound out) {
		out.setDouble(BUFFER_IN, bufferToMB);
		out.setDouble(BUFFER_OUT, bufferToWorld);
		out.setInteger(BUFFER_IN+WAVEFORM, wfToMB.ordinal());
		out.setInteger(BUFFER_OUT+WAVEFORM, wfToWorld.ordinal());
	}

	@Override
	public void readFromNBT(NBTTagCompound in) {
		bufferToMB = in.getDouble(BUFFER_IN);
		bufferToWorld = in.getDouble(BUFFER_OUT);
		wfToMB = Waveform.VALUES[in.getInteger(BUFFER_IN+WAVEFORM)];
		wfToWorld = Waveform.VALUES[in.getInteger(BUFFER_OUT+WAVEFORM)];
	}

	@Override
	public ResourceLocation getRotatingBaseModel() {
		return new ResourceLocation(IndustrialWires.MODID, "block/mech_mb/shaft_comm.obj");
	}

	private static final ResourceLocation KINETIC_GEN_KEY =
			new ResourceLocation("ic2", "kinetic_generator");
	@Override
	public boolean canForm(LocalSidedWorld w) {
		Minecraft.getMinecraft().mouseHelper.ungrabMouseCursor();
		//Center is an IC2 kinetic generator
		TileEntity te = w.getTileEntity(BlockPos.ORIGIN);
		if (te!=null) {
			ResourceLocation loc = TileEntity.getKey(te.getClass());
			if (loc!=null&&loc.equals(KINETIC_GEN_KEY)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public short getFormPattern() {
		return 0b000_010_000;
	}

	@Override
	public void disassemble(boolean failed, MechEnergy energy) {
		if (IndustrialWires.ic2TeBlock!=null) {
			NBTTagCompound dummyNbt = new NBTTagCompound();
			dummyNbt.setString("id", KINETIC_GEN_KEY.toString());
			world.setBlockState(BlockPos.ORIGIN, IndustrialWires.ic2TeBlock.getDefaultState());
			world.setTileEntity(BlockPos.ORIGIN, TileEntity.create(world.getWorld(), dummyNbt));
		}
	}

	@Override
	public MechanicalMBBlockType getType() {
		return MechanicalMBBlockType.SHAFT_COMMUTATOR;
	}

	protected double getMaxBuffer() {
		return 2.5e3;
	}
}

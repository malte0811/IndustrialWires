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

package malte0811.industrialWires.mech_mb;

import malte0811.industrialWires.IWConfig;
import malte0811.industrialWires.mech_mb.EUCapability.IC2EnergyHandler;
import malte0811.industrialWires.util.ConversionUtil;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Set;

import static malte0811.industrialWires.mech_mb.EUCapability.ENERGY_IC2;
import static malte0811.industrialWires.mech_mb.MechPartEnergyIO.IOState.*;
import static malte0811.industrialWires.mech_mb.Waveform.Phases.get;
import static malte0811.industrialWires.mech_mb.Waveform.Speed.EXTERNAL;
import static malte0811.industrialWires.mech_mb.Waveform.Speed.ROTATION;
import static malte0811.industrialWires.mech_mb.Waveform.Type.DC;
import static malte0811.industrialWires.mech_mb.Waveform.Type.NONE;
import static malte0811.industrialWires.util.NBTKeys.*;
import static net.minecraftforge.energy.CapabilityEnergy.ENERGY;

public abstract class MechPartEnergyIO extends MechMBPart implements IMBPartElectric {
	private double bufferToMB;
	private Waveform wfToMB = Waveform.forParameters(NONE, get(has4Phases()), ROTATION);
	private double bufferToWorld;
	private Waveform wfToWorld = Waveform.forParameters(NONE, get(has4Phases()), ROTATION);
	private final IEnergyStorage capForge = new EnergyStorageMMB();
	private final IC2EnergyHandler capIc2 = new IC2EHandlerMB();
	private IOState lastIOState = NO_TRANSFER;
	private long lastStateChange = Long.MIN_VALUE;

	@Override
	public Waveform getProduced(MechEnergy state) {
		return transform(wfToMB, state);
	}

	protected Waveform transform(Waveform wf, MechEnergy e) {
		return wf;
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
		if (!has4Phases()==waveform.isSinglePhase()) {
			return getMaxBuffer() - bufferToWorld;
		}
		return 0;
	}

	@Override
	public void insertEEnergy(double given, Waveform waveform, MechEnergy mechEnergy) {
		waveform = transform(waveform, mechEnergy);
		wfToWorld = waveform;
		bufferToWorld += given;
	}

	public void setLastIOState(IOState state) {
		if (lastIOState.canSwitchTo(state)) {
			lastIOState = state;
			lastStateChange = world.getWorld().getTotalWorldTime();
		}
	}

	public IOState getLastIOState() {
		return lastIOState;
	}

	@Override
	public <T> T getCapability(Capability<T> cap, EnumFacing side, BlockPos pos) {
		if (getEnergyConnections().contains(new ImmutablePair<>(pos, side))) {
			if (cap == ENERGY_IC2) {
				return ENERGY_IC2.cast(capIc2);
			}
			if (cap == ENERGY) {
				return ENERGY.cast(capForge);
			}
		}
		return super.getCapability(cap, side, pos);
	}

	@Override
	public <T> boolean hasCapability(Capability<T> cap, EnumFacing side, BlockPos pos) {
		if (getEnergyConnections().contains(new ImmutablePair<>(pos, side))) {
			if (cap == ENERGY_IC2) {
				return true;
			}
			if (cap == ENERGY) {
				return true;
			}
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
	public void insertMEnergy(double added) {
		if (world.getWorld().getTotalWorldTime()>lastStateChange+1) {
			setLastIOState(NO_TRANSFER);
		}
		int available = (int) (Math.min(ConversionUtil.ifPerJoule() * bufferToWorld,
				getMaxBuffer()/getEnergyConnections().size()));
		if (available > 0 && wfToWorld.isAC()) {//The IC2 net will deal with DC by itself
			bufferToWorld -= outputFE(available);
		}
	}

	@Override
	public double getInertia() {
		return 50;
	}

	@Override
	public double getMaxSpeed() {
		return IWConfig.MechConversion.allowMBEU()?100:-1;
	}

	@Override
	public void writeToNBT(NBTTagCompound out) {
		out.setDouble(BUFFER_IN, bufferToMB);
		out.setDouble(BUFFER_OUT, bufferToWorld);
		out.setString(BUFFER_IN+WAVEFORM, wfToMB.serializeToString());
		out.setString(BUFFER_OUT+WAVEFORM, wfToWorld.serializeToString());
	}

	@Override
	public void readFromNBT(NBTTagCompound in) {
		bufferToMB = in.getDouble(BUFFER_IN);
		bufferToWorld = in.getDouble(BUFFER_OUT);
		wfToMB = Waveform.fromString(in.getString(BUFFER_IN+WAVEFORM));
		wfToWorld = Waveform.fromString(in.getString(BUFFER_OUT+WAVEFORM));
	}

	protected abstract double getMaxBuffer();

	protected abstract boolean has4Phases();

	public abstract Set<Pair<BlockPos, EnumFacing>> getEnergyConnections();

	private double outputFE(int available) {
		if (!getLastIOState().canSwitchToOutput())
			return 0;
		double extracted = 0;
		for (Pair<BlockPos, EnumFacing> output : getEnergyConnections()) {
			if (output.getRight()==null)
				continue;
			BlockPos outTE = output.getLeft().offset(output.getRight());
			TileEntity te = world.getTileEntity(outTE);
			EnumFacing sideReal = world.transformedToReal(output.getRight()).getOpposite();
			if (te != null && te.hasCapability(CapabilityEnergy.ENERGY, sideReal)) {
				IEnergyStorage energy = te.getCapability(CapabilityEnergy.ENERGY, sideReal);
				if (energy != null && energy.canReceive()) {
					int received = energy.receiveEnergy(available, false);
					available -= received;
					extracted += ConversionUtil.joulesPerIf() * received;
				}
			}
		}
		if (extracted>0) {
			setLastIOState(IOState.OUTPUT);
		}
		return extracted;
	}

	class IC2EHandlerMB extends IC2EnergyHandler {
		{
			tier = 3;//TODO does this mean everything blows up?
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
			wfToMB = Waveform.forParameters(DC, get(has4Phases()), EXTERNAL);
			setLastIOState(INPUT);
			return amount-ConversionUtil.euPerJoule()*input;
		}

		@Override
		public double getOfferedEnergy() {
			if (wfToWorld.isDC() && getLastIOState().canSwitchToOutput()) {
				return Math.min(ConversionUtil.euPerJoule()*bufferToWorld,
						ConversionUtil.euPerJoule()*getMaxBuffer()/getEnergyConnections().size()*2);
			}
			return 0;
		}

		@Override
		public double getDemandedEnergy() {
			if (getLastIOState().canSwitchToInput()) {
				return Math.min(ConversionUtil.euPerJoule()*(getMaxBuffer()-bufferToMB),
						ConversionUtil.euPerJoule()*getMaxBuffer()/getEnergyConnections().size()*2);
			}
			return 0;
		}

		@Override
		public void drawEnergy(double amount) {
			bufferToWorld -= ConversionUtil.joulesPerEu()*amount;
			setLastIOState(OUTPUT);
		}
	};

	class EnergyStorageMMB implements IEnergyStorage {
		@Override
		public int receiveEnergy(int maxReceive, boolean simulate) {
			if (!getLastIOState().canSwitchToInput()) {
				return 0;
			}
			double buffer = bufferToMB;
			double input = maxReceive * ConversionUtil.joulesPerIf();
			if (!wfToMB.isAC()) {
				buffer = 0;
			}
			input = Math.min(input, getMaxBuffer() - buffer);
			buffer += input;
			if (!simulate) {
				bufferToMB = buffer;
				wfToMB = Waveform.forParameters(Waveform.Type.AC, get(has4Phases()), EXTERNAL);
				if (input > 0) {
					setLastIOState(INPUT);
				}
			}
			return (int) (ConversionUtil.ifPerJoule() * input);
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate) {
			if (!wfToWorld.isAC() || !getLastIOState().canSwitchToOutput()) {
				return 0;
			}
			double buffer = bufferToWorld;
			double output = maxExtract * ConversionUtil.joulesPerIf();
			output = Math.min(output, buffer);
			buffer -= output;
			if (!simulate) {
				bufferToWorld = buffer;
				if (output > 0) {
					setLastIOState(OUTPUT);
				}
			}
			return (int) (ConversionUtil.ifPerJoule() * output);
		}

		@Override
		public int getEnergyStored() {
			return (int) (ConversionUtil.ifPerJoule() * (bufferToWorld + bufferToMB));
		}

		@Override
		public int getMaxEnergyStored() {
			return (int) (2 * ConversionUtil.ifPerJoule() * getMaxBuffer());
		}

		@Override
		public boolean canExtract() {
			return true;
		}

		@Override
		public boolean canReceive() {
			return true;
		}
	}

	enum IOState {
		NO_TRANSFER,
		OUTPUT,
		INPUT;

		boolean canSwitchToOutput() {
			return NO_TRANSFER ==this||OUTPUT==this;
		}

		boolean canSwitchToInput() {
			return NO_TRANSFER ==this||INPUT==this;
		}

		public boolean canSwitchTo(IOState state) {
			switch (state) {
				case NO_TRANSFER:
					return true;
				case OUTPUT:
					return canSwitchToOutput();
				case INPUT:
					return canSwitchToInput();
			}
			return true;
		}
	}
}

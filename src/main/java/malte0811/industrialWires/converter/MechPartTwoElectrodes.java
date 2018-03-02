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
import malte0811.industrialWires.util.ConversionUtil;
import malte0811.industrialWires.util.LocalSidedWorld;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import static blusunrize.immersiveengineering.common.IEContent.blockMetalDecoration0;
import static blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0.GENERATOR;
import static malte0811.industrialWires.converter.IMBPartElectric.Waveform.*;
import static malte0811.industrialWires.util.ConversionUtil.ifPerJoule;
import static malte0811.industrialWires.util.ConversionUtil.joulesPerIf;
import static malte0811.industrialWires.util.NBTKeys.*;

public class MechPartTwoElectrodes extends MechMBPart implements IMBPartElectric {
	private final static double MAX_BUFFER = 10e3;//200kW
	private double bufferToMB;
	private boolean isACInMBBuffer;
	private double bufferToWorld;
	private boolean isACInWBuffer;
	@Override
	public Waveform getProduced(MechEnergy state) {
		return bufferToMB>0?(isACInMBBuffer? AC_ASYNC: DC): NONE;
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
		return MAX_BUFFER-bufferToWorld;
	}

	@Override
	public void insertEEnergy(double given, Waveform waveform, MechEnergy energy) {
		if (bufferToWorld > 0 && (isACInWBuffer ^ waveform.isAC())) {
			bufferToWorld = 0;
		}
		if (waveform.isAC() || waveform.isAC()) {
			bufferToWorld += given;
			isACInWBuffer = waveform.isAC();
		}
	}

	@Override
	public void createMEnergy(MechEnergy e) {}

	@Override
	public double requestMEnergy(MechEnergy e) {
		return 0;
	}

	@Override
	public void insertMEnergy(double added) {
		int available = (int) (ConversionUtil.ifPerJoule()*bufferToWorld);
		if (available>0&&isACInWBuffer) {//The IC2 net will deal with DC by itself
			BlockPos up = BlockPos.ORIGIN.up();
			TileEntity te = world.getTileEntity(up);
			if (te != null && te.hasCapability(CapabilityEnergy.ENERGY, EnumFacing.DOWN)) {
				IEnergyStorage energy = te.getCapability(CapabilityEnergy.ENERGY, EnumFacing.DOWN);
				if (energy != null && energy.canReceive()) {
					int received = energy.receiveEnergy(available, false);
					bufferToWorld -= ConversionUtil.joulesPerIf() * received;
				}
			}
		}
	}

	@Override
	public double getInertia() {
		return 50;//Random value. Does this work reasonably well?
	}

	@Override
	public double getSpeedFor15RS() {
		return Double.MAX_VALUE;//TODO
	}

	@Override
	public void writeToNBT(NBTTagCompound out) {
		out.setDouble(BUFFER_IN, bufferToMB);
		out.setBoolean(BUFFER_IN+AC, isACInMBBuffer);
		out.setDouble(BUFFER_OUT, bufferToWorld);
		out.setBoolean(BUFFER_OUT+AC, isACInWBuffer);
	}

	@Override
	public void readFromNBT(NBTTagCompound in) {
		bufferToMB = in.getDouble(BUFFER_IN);
		isACInMBBuffer = in.getBoolean(BUFFER_IN+AC);
		bufferToWorld = in.getDouble(BUFFER_OUT);
		isACInWBuffer = in.getBoolean(BUFFER_OUT+AC);
	}

	@Override
	public ResourceLocation getRotatingBaseModel() {
		return new ResourceLocation(IndustrialWires.MODID, "block/mech_mb/shaft2.obj");
	}


	@Override
	public boolean canForm(LocalSidedWorld w) {
		IBlockState state = w.getBlockState(BlockPos.ORIGIN);
		return state.getBlock()== blockMetalDecoration0 &&
				state.getValue(blockMetalDecoration0.property)== GENERATOR;
	}

	@Override
	public short getFormPattern() {
		return 0b000_010_000;
	}

	@Override
	public void disassemble(boolean failed, MechEnergy energy) {
		world.setBlockState(BlockPos.ORIGIN,
				blockMetalDecoration0.getDefaultState().withProperty(blockMetalDecoration0.property, GENERATOR));
	}

	@Override
	public MechanicalMBBlockType getType() {
		return MechanicalMBBlockType.SHAFT_1_PHASE;
	}


	private IEnergyStorage energy = new IEnergyStorage() {
		@Override
		public int receiveEnergy(int maxReceive, boolean simulate) {
			double joules = joulesPerIf()*maxReceive;
			double insert = Math.min(joules, MAX_BUFFER-bufferToMB);
			if (!simulate) {
				if (!isACInMBBuffer) {
					bufferToMB = 0;
					isACInMBBuffer = true;
				}
				bufferToMB += insert;
			}
			return (int) Math.ceil(insert* ifPerJoule());
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate) {
			if (isACInWBuffer) {
				double joules = joulesPerIf() * maxExtract;
				double extract = Math.min(joules, bufferToWorld);
				if (!simulate)
					bufferToWorld -= extract;
				return (int) Math.floor(extract * ifPerJoule());
			} else {
				return 0;
			}
		}

		@Override
		public int getEnergyStored() {
			return (int) Math.round((bufferToMB+bufferToWorld)* ifPerJoule());
		}

		@Override
		public int getMaxEnergyStored() {
			return (int) Math.round(MAX_BUFFER*2* ifPerJoule());
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
	public <T> boolean hasCapability(Capability<T> cap, EnumFacing side, Vec3i pos) {
		if (pos.equals(BlockPos.ORIGIN)&&side==EnumFacing.UP) {
			if (cap==CapabilityEnergy.ENERGY)
				return true;
			//TODO return true for internal IC2 cap that doesn't exist yet
		}
		return super.hasCapability(cap, side, pos);
	}

	@Override
	public <T> T getCapability(Capability<T> cap, EnumFacing side, Vec3i pos) {
		if (pos.equals(BlockPos.ORIGIN)&&side==EnumFacing.UP&&cap== CapabilityEnergy.ENERGY)
			return CapabilityEnergy.ENERGY.cast(energy);
		return super.getCapability(cap, side, pos);
	}
}

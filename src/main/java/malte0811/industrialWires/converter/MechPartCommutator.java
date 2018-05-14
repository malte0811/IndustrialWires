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

import com.google.common.collect.ImmutableSet;
import malte0811.industrialWires.IWConfig;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.converter.MechanicalMBBlockType;
import malte0811.industrialWires.converter.EUCapability.IC2EnergyHandler;
import malte0811.industrialWires.util.ConversionUtil;
import malte0811.industrialWires.util.LocalSidedWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.IEnergyStorage;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Set;

import static malte0811.industrialWires.converter.EUCapability.ENERGY_IC2;
import static malte0811.industrialWires.converter.Waveform.Phases.get;
import static malte0811.industrialWires.converter.Waveform.Speed.EXTERNAL;
import static malte0811.industrialWires.converter.Waveform.Speed.ROTATION;
import static malte0811.industrialWires.converter.Waveform.Type.DC;
import static malte0811.industrialWires.converter.Waveform.Type.NONE;
import static malte0811.industrialWires.util.NBTKeys.*;
import static net.minecraft.util.EnumFacing.UP;
import static net.minecraft.util.math.BlockPos.ORIGIN;
import static net.minecraftforge.energy.CapabilityEnergy.ENERGY;

public class MechPartCommutator extends MechMBPart implements IMBPartElectric {
	public static ItemStack originalStack = ItemStack.EMPTY;
	private double bufferToMB;
	private Waveform wfToMB = Waveform.forParameters(NONE, get(has4Phases()), ROTATION);
	private double bufferToWorld;
	private Waveform wfToWorld = Waveform.forParameters(NONE, get(has4Phases()), ROTATION);
	@Override
	public Waveform getProduced(MechEnergy state) {
		return wfToMB.getCommutated(state.getSpeed(), has4Phases());
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
		waveform = waveform.getCommutated(mechEnergy.getSpeed(), has4Phases());
		wfToWorld = waveform;
		bufferToWorld += given;
	}


	private final IC2EnergyHandler capIc2 = new IC2EnergyHandler() {
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
			return amount-ConversionUtil.euPerJoule()*input;
		}

		@Override
		public double getOfferedEnergy() {
			if (wfToWorld.isDC()) {
				return Math.min(ConversionUtil.euPerJoule()*bufferToWorld,
						ConversionUtil.euPerJoule()*getMaxBuffer())/getEnergyConnections().size()*2;
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
				wfToMB = Waveform.forParameters(Waveform.Type.AC, get(has4Phases()), EXTERNAL);
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
	public void insertMEnergy(double added) {int available = (int) (Math.min(ConversionUtil.ifPerJoule() * bufferToWorld,
			getMaxBuffer()/getEnergyConnections().size()));
		if (available > 0 && wfToWorld.isAC()) {//The IC2 net will deal with DC by itself
			bufferToWorld -= outputFE(world, available);
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

	@Override
	public ResourceLocation getRotatingBaseModel() {
		return new ResourceLocation(IndustrialWires.MODID, "block/mech_mb/shaft_comm.obj");
	}

	private static final ResourceLocation KINETIC_GEN_KEY =
			new ResourceLocation("ic2", "kinetic_generator");
	@Override
	public boolean canForm(LocalSidedWorld w) {
		if (!IWConfig.MechConversion.allowMBEU()) {
			return false;
		}
		//Center is an IC2 kinetic generator
		TileEntity te = w.getTileEntity(BlockPos.ORIGIN);
		if (te!=null) {
			ResourceLocation loc = TileEntity.getKey(te.getClass());
			return loc != null && loc.equals(KINETIC_GEN_KEY);
		}
		return false;
	}

	@Override
	public short getFormPattern(int offset) {
		return 0b000_010_000;
	}

	@Override
	public void breakOnFailure(MechEnergy energy) {
		//NOP
	}

	@Override
	public ItemStack getOriginalItem(BlockPos pos) {
		return pos.equals(ORIGIN)?originalStack:super.getOriginalItem(pos);
	}

	@Override
	public void disassemble() {
		super.disassemble();
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

	protected boolean has4Phases() {
		return false;
	}

	private static final ImmutableSet<Pair<BlockPos, EnumFacing>> outputs = ImmutableSet.of(
			new ImmutablePair<>(ORIGIN, UP), new ImmutablePair<>(ORIGIN, null)
	);
	public Set<Pair<BlockPos, EnumFacing>> getEnergyConnections() {
		return outputs;
	}

	@Override
	public AxisAlignedBB getBoundingBox(BlockPos offsetPart) {
		return new AxisAlignedBB(0, .375-1/32D, 0, 1, 1, 1);
	}
}

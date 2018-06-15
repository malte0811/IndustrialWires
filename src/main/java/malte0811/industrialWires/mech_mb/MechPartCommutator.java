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

import com.google.common.collect.ImmutableSet;
import malte0811.industrialWires.IWConfig;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.converter.MechanicalMBBlockType;
import malte0811.industrialWires.util.LocalSidedWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Set;

import static net.minecraft.util.EnumFacing.UP;
import static net.minecraft.util.math.BlockPos.ORIGIN;

public class MechPartCommutator extends MechPartEnergyIO {
	public static ItemStack originalStack = ItemStack.EMPTY;

	@Override
	protected Waveform transform(Waveform wf, MechEnergy e) {
		return wf.getCommutated(e.getSpeed(), has4Phases());
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

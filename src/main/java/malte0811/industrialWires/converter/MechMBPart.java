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

import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.converter.MechanicalMBBlockType;
import malte0811.industrialWires.blocks.converter.TileEntityMultiblockConverter;
import malte0811.industrialWires.client.render.TileRenderMBConverter;
import malte0811.industrialWires.util.LocalSidedWorld;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static malte0811.industrialWires.blocks.converter.MechanicalMBBlockType.NO_MODEL;
import static malte0811.industrialWires.util.NBTKeys.TYPE;

public abstract class MechMBPart {
	public static final Map<String, MechMBPart> INSTANCES = new HashMap<>();
	public LocalSidedWorld world;

	// These 3 are called once per tick in bulk in this order
	public abstract void createMEnergy(MechEnergy e);
	public abstract double requestMEnergy(MechEnergy e);
	// This should do any misc ticking as well
	public abstract void insertMEnergy(double added);

	public abstract double getInertia();
	public abstract double getSpeedFor15RS();
	public abstract void writeToNBT(NBTTagCompound out);
	public abstract void readFromNBT(NBTTagCompound in);

	@SideOnly(Side.CLIENT)
	public List<BakedQuad> getRotatingQuads() {
		return TileRenderMBConverter.BASE_MODELS.get(getRotatingBaseModel())
				.getQuads(null, null, 123);

	}
	@SideOnly(Side.CLIENT)
	public abstract ResourceLocation getRotatingBaseModel();

	public abstract boolean canForm(LocalSidedWorld w);

	public abstract short getFormPattern();

	/**
	 * @param failed whether the MB is being disassembled because this part failed
	 * @param energy
	 */
	public abstract void disassemble(boolean failed, MechEnergy energy);

	public abstract MechanicalMBBlockType getType();

	public <T> boolean hasCapability(Capability<T> cap, EnumFacing side, Vec3i pos) {
		return false;
	}

	public <T> T getCapability(Capability<T> cap, EnumFacing side, Vec3i pos) {
		return null;
	}

	private static final BiMap<String, Class<? extends MechMBPart>> REGISTRY = HashBiMap.create();
	public static void preInit() {
		IMBPartElectric.Waveform.init();

		REGISTRY.put("flywheel", MechPartFlywheel.class);
		REGISTRY.put("singleCoil", MechPartSingleCoil.class);
		REGISTRY.put("twoElectrodes", MechPartTwoElectrodes.class);
		REGISTRY.put("commutator", MechPartCommutator.class);
		REGISTRY.put("shaft", MechPartShaft.class);
		REGISTRY.put("speedometer", MechPartSpeedometer.class);

		for (String key : REGISTRY.keySet()) {
			cacheNewInstance(key);
		}
	}

	public static void cacheNewInstance(String key) {
		try {
			MechMBPart instance = REGISTRY.get(key).newInstance();
			INSTANCES.put(key, instance);
		} catch (IllegalAccessException | InstantiationException e) {
			throw new RuntimeException(e);
		}
	}

	public static MechMBPart fromNBT(NBTTagCompound nbt, LocalSidedWorld w) {
		String name = nbt.getString(TYPE);
		Class<? extends MechMBPart> clazz = REGISTRY.get(name);
		try {
			MechMBPart ret = clazz.newInstance();
			ret.readFromNBT(nbt);
			if (w==null)
				throw new NullPointerException();
			ret.world = w;
			return ret;
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("While creating mechanical MB part", e);
		}
	}
	public static NBTTagCompound toNBT(MechMBPart part) {
		Class<? extends MechMBPart> clazz = part.getClass();
		String name = REGISTRY.inverse().get(clazz);
		NBTTagCompound nbt = new NBTTagCompound();
		part.writeToNBT(nbt);
		nbt.setString(TYPE, name);
		return nbt;
	}

	public static boolean isValidDefaultCenter(IBlockState state) {
		return state.getBlock()== IEContent.blockMetalDecoration0&&
				state.getValue(IEContent.blockMetalDecoration0.property)==BlockTypes_MetalDecoration0.LIGHT_ENGINEERING;
	}


	public void form(LocalSidedWorld w, Consumer<TileEntityMultiblockConverter> initializer) {
		world = w;
		BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain();
		short pattern = getFormPattern();
		int i = 0;
		for (int y = -1; y <= 1; y++) {
			for (int x = -1; x <= 1; x++) {
				if ((pattern & (1 << i)) != 0) {
					pos.setPos(x, y, 0);
					w.setBlockState(pos, IndustrialWires.mechanicalMB.getStateFromMeta((i==4?getType():NO_MODEL).ordinal()));
					TileEntity te = w.getTileEntity(pos);
					if (te instanceof TileEntityMultiblockConverter) {
						initializer.accept((TileEntityMultiblockConverter) te);
					}
				}
				i++;
			}
		}
		pos.release();
	}

	public int getLength() {
		return 1;
	}
}

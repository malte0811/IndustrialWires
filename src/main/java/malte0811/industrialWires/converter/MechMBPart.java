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
import malte0811.industrialWires.blocks.converter.TileEntityMechMB;
import malte0811.industrialWires.client.render.TileRenderMBConverter;
import malte0811.industrialWires.util.LocalSidedWorld;
import malte0811.industrialWires.util.MiscUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static blusunrize.immersiveengineering.common.IEContent.blockMetalDecoration0;
import static blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0.HEAVY_ENGINEERING;
import static blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0.LIGHT_ENGINEERING;
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
	public abstract double getMaxSpeed();
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

	protected boolean hasSupportPillars(LocalSidedWorld w) {
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				IBlockState state = w.getBlockState(new BlockPos(2*i-1, j-1, 0));
				if (!isLightEngineering(state)) {
					return false;
				}
			}
		}
		return true;
	}

	public abstract short getFormPattern();

	/**
	 * @param failed whether the MB is being disassembled because this part failed
	 */
	public abstract void disassemble(boolean failed, MechEnergy energy);

	public abstract MechanicalMBBlockType getType();

	public <T> boolean hasCapability(Capability<T> cap, EnumFacing side, BlockPos pos) {
		return false;
	}

	public <T> T getCapability(Capability<T> cap, EnumFacing side, BlockPos pos) {
		return null;
	}

	public static final BiMap<String, Class<? extends MechMBPart>> REGISTRY = HashBiMap.create();

	public static final String SHAFT_KEY = "shaft";

	public static final Comparator<MechMBPart> SORT_BY_COUNT = Comparator.comparingInt(
			(c)->-MiscUtils.count1Bits(c.getFormPattern())
	);
	public static void preInit() {
		REGISTRY.put("flywheel", MechPartFlywheel.class);
		REGISTRY.put("singleCoil", MechPartSingleCoil.class);
		REGISTRY.put("twoElectrodes", MechPartTwoElectrodes.class);
		REGISTRY.put("commutator", MechPartCommutator.class);
		REGISTRY.put(SHAFT_KEY, MechPartShaft.class);
		REGISTRY.put("speedometer", MechPartSpeedometer.class);
		REGISTRY.put("commFour", MechPartCommutator4Phase.class);
		REGISTRY.put("fourCoils", MechPartFourCoils.class);
		REGISTRY.put("fourElectrodes", MechPartFourElectrodes.class);

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
				state.getValue(IEContent.blockMetalDecoration0.property)==BlockTypes_MetalDecoration0.HEAVY_ENGINEERING;
	}

	public static boolean isHeavyEngineering(IBlockState state) {
		return isValidDefaultCenter(state);
	}

	public static boolean isLightEngineering(IBlockState state) {
		return state.getBlock()== IEContent.blockMetalDecoration0&&
				state.getValue(IEContent.blockMetalDecoration0.property)==BlockTypes_MetalDecoration0.LIGHT_ENGINEERING;
	}

	public void setDefaultShaft(BlockPos pos) {
		world.setBlockState(pos, blockMetalDecoration0.getDefaultState().withProperty(blockMetalDecoration0.property,
				HEAVY_ENGINEERING));
	}

	public void setLightEngineering(BlockPos pos) {
		world.setBlockState(pos, blockMetalDecoration0.getDefaultState().withProperty(blockMetalDecoration0.property,
				LIGHT_ENGINEERING));
	}


	public void form(LocalSidedWorld w, Consumer<TileEntityMechMB> initializer) {
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
					if (te instanceof TileEntityMechMB) {
						initializer.accept((TileEntityMechMB) te);
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

	public abstract AxisAlignedBB getBoundingBox(BlockPos offsetPart);
}

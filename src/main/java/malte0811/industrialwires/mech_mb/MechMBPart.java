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

package malte0811.industrialwires.mech_mb;

import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import malte0811.industrialwires.IndustrialWires;
import malte0811.industrialwires.blocks.converter.MechanicalMBBlockType;
import malte0811.industrialwires.blocks.converter.TileEntityMechMB;
import malte0811.industrialwires.client.render.TileRenderMechMB;
import malte0811.industrialwires.entities.EntityBrokenPart;
import malte0811.industrialwires.util.LocalSidedWorld;
import malte0811.industrialwires.util.MiscUtils;
import malte0811.industrialwires.util.MultiblockTemplateManual;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0.HEAVY_ENGINEERING;
import static blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0.LIGHT_ENGINEERING;
import static malte0811.industrialwires.IEObjects.blockMetalDecoration0;
import static malte0811.industrialwires.IndustrialWires.MODID;
import static malte0811.industrialwires.blocks.converter.MechanicalMBBlockType.NO_MODEL;
import static malte0811.industrialwires.util.NBTKeys.TYPE;

public abstract class MechMBPart {
	public static final Map<String, MechMBPart> INSTANCES = new HashMap<>();
	public LocalSidedWorld world;
	protected Map<BlockPos, IBlockState> original = new HashMap<>();

	// These 3 are called once per tick in bulk in this order
	public abstract void createMEnergy(MechEnergy e);
	public abstract double requestMEnergy(MechEnergy e);
	// This should do any misc ticking as well
	public abstract void insertMEnergy(double added);

	public abstract double getInertia();
	public abstract double getMaxSpeed();
	public abstract void writeToNBT(NBTTagCompound out);
	public abstract void readFromNBT(NBTTagCompound in);

	public IBlockState getExtState(IBlockState in) {
		return in;
	}

	@SideOnly(Side.CLIENT)
	public List<BakedQuad> getRotatingQuads() {
		return TileRenderMechMB.BASE_MODELS.get(getRotatingBaseModel())
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

	public abstract short getFormPattern(int offset);

	public abstract void breakOnFailure(MechEnergy energy);

	public ItemStack getOriginalItem(BlockPos pos) {
		IBlockState state = getOriginalBlock(pos);
		return new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
	}

	public IBlockState getOriginalBlock(BlockPos pos) {
		return original.getOrDefault(pos, Blocks.AIR.getDefaultState());
	}

	public abstract MechanicalMBBlockType getType();

	public <T> boolean hasCapability(Capability<T> cap, EnumFacing side, BlockPos pos) {
		return false;
	}

	public <T> T getCapability(Capability<T> cap, EnumFacing side, BlockPos pos) {
		return null;
	}

	public int interact(@Nonnull EnumFacing side, @Nonnull Vec3i offset, @Nonnull EntityPlayer player,
						@Nonnull EnumHand hand, @Nonnull ItemStack heldItem) {
		return -1;
	}

	public static final BiMap<String, Class<? extends MechMBPart>> REGISTRY = HashBiMap.create();

	public static final String SHAFT_KEY = "shaft";
	public static final ResourceLocation EXAMPLE_MECHMB_LOC = new ResourceLocation(MODID, "example_mech_mb");

	public static final Comparator<MechMBPart> SORT_BY_COUNT = (a, b)-> {
		if (a.getLength()!=b.getLength()) {
			return Integer.compare(a.getLength(), b.getLength());
		}
		for (int i = 0;i<a.getLength();i++) {
			int aBits = MiscUtils.count1Bits(a.getFormPattern(i));
			int bBits = MiscUtils.count1Bits(b.getFormPattern(i));
			if (aBits!=bBits) {
				return Integer.compare(aBits, bBits);
			}
		}
		return 0;
	};
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
			MultiblockHandler.registerMultiblock(
					new MultiblockTemplateManual(getSchematicLocationForPart(key)));
		}
	}

	public static void init() {
		//The old instances don't have block/blockstate references yet
		for (String key : REGISTRY.keySet()) {
			cacheNewInstance(key);
		}
	}

	public static ResourceLocation getSchematicLocationForPart(Class<? extends MechMBPart> cl) {
		String name = REGISTRY.inverse().get(cl);
		return getSchematicLocationForPart(name);
	}

	public static ResourceLocation getSchematicLocationForPart(String name) {
		if (name==null)
			return null;
		name = MiscUtils.toSnakeCase(name);
		return new ResourceLocation(MODID, name);
	}

	public static MultiblockHandler.IMultiblock getManualMBForPart(Class<? extends MechMBPart> cl) {
		return MiscUtils.getMBFromName(getSchematicLocationForPart(cl).toString());
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
		return state.getBlock()== blockMetalDecoration0&&
				state.getValue(blockMetalDecoration0.property)==BlockTypes_MetalDecoration0.HEAVY_ENGINEERING;
	}

	public static boolean isHeavyEngineering(IBlockState state) {
		return isValidDefaultCenter(state);
	}

	public static boolean isLightEngineering(IBlockState state) {
		return state.getBlock()== blockMetalDecoration0&&
				state.getValue(blockMetalDecoration0.property)==BlockTypes_MetalDecoration0.LIGHT_ENGINEERING;
	}

	public IBlockState getDefaultShaft() {
		return blockMetalDecoration0.getDefaultState().withProperty(blockMetalDecoration0.property,
				HEAVY_ENGINEERING);
	}

	public IBlockState getLightEngineering() {
		return blockMetalDecoration0.getDefaultState().withProperty(blockMetalDecoration0.property,
				LIGHT_ENGINEERING);
	}

	protected boolean areBlocksRegistered() {
		return Loader.instance().getLoaderState().ordinal()>LoaderState.PREINITIALIZATION.ordinal();
	}

	public void form(LocalSidedWorld w, Consumer<TileEntityMechMB> initializer) {
		world = w;
		BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain();
		for (int z = 0;z<getLength();z++) {
			short pattern = getFormPattern(z);
			int i = 0;
			for (int y = -1; y <= 1; y++) {
				for (int x = -1; x <= 1; x++) {
					if ((pattern & (1 << i)) != 0) {
						pos.setPos(x, y, -z);
						w.setBlockState(pos, IndustrialWires.mechanicalMB.getStateFromMeta((i == 4 ? getType() : NO_MODEL).ordinal()));
						TileEntity te = w.getTileEntity(pos);
						if (te instanceof TileEntityMechMB) {
							initializer.accept((TileEntityMechMB) te);
						}
					}
					i++;
				}
			}
		}
		pos.release();
	}

	public void disassemble() {
		BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain();
		for (int z = 0;z<getLength();z++) {
			for (int x = -1; x <= 1; x++) {
				for (int y = -1; y <= 1; y++) {
					pos.setPos(x, y, -z);
					IBlockState state = getOriginalBlock(pos);
					if (state.getBlock()!=Blocks.AIR) {
						world.setBlockState(pos, state);
					}
				}
			}
		}
		pos.release();
	}

	protected void spawnBrokenParts(int count, MechEnergy energy, ResourceLocation texture) {
		Matrix4 mat = new Matrix4();
		mat.rotate(Utils.RAND.nextDouble(), 0, 0, 1);
		Vec3d baseVec = new Vec3d(0, 1.5, 0);
		for (int i = 0;i<count;i++) {
			mat.rotate(2*Math.PI / count, 0, 0, 1);
			Vec3d pos = mat.apply(baseVec);
			EntityBrokenPart e = new EntityBrokenPart(world.getWorld(), texture);
			e.setPosition(pos.x, pos.y, -.5);
			double speed = (energy.getSpeed() / getMaxSpeed()) / 1.5;
			e.motionX = pos.y * speed;
			e.motionY = -pos.x * speed;
			e.motionZ = (Utils.RAND.nextDouble() - .5) * speed / 10;
			world.spawnEntity(e);
			e.breakBlocks(speed * speed * 1.5 * 1.5);
		}
	}

	public int getLength() {
		return 1;
	}

	public abstract AxisAlignedBB getBoundingBox(BlockPos offsetPart);
}

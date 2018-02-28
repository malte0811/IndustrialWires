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

package malte0811.industrialWires.blocks;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGeneralMultiblock;
import malte0811.industrialWires.util.MiscUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiConsumer;

public abstract class TileEntityIWMultiblock extends TileEntityIWBase implements IGeneralMultiblock,
		IDirectionalTile {
	protected final static String OFFSET = "offset";
	protected final static String FORMED = "formed";
	protected final static String MIRRORED = "mirrored";
	protected final static String FACING = "facing";
	//HFR
	protected Vec3i size;
	public Vec3i offset = new Vec3i(0, 1, 0);
	public boolean formed;
	public boolean mirrored;
	public long onlyLocalDissassembly;
	public EnumFacing facing = EnumFacing.NORTH;
	@Nonnull
	protected abstract BlockPos getOrigin();
	public abstract IBlockState getOriginalBlock();
	public BiConsumer<World, BlockPos> getOriginalBlockPlacer() {
		return (w, p)->w.setBlockState(p, getOriginalBlock());
	}
	@Nullable
	public <T extends TileEntityIWMultiblock> T master(T here) {
		if (!isLogicDummy()) {
			return here;
		}
		TileEntity m = world.getTileEntity(pos.subtract(offset));
		if (m!=null&&m.getClass().equals(this.getClass())) {
			return (T) m;
		}
		return null;
	}

	@Override
	public boolean isLogicDummy() {
		return offset.getX()!=0||offset.getY()!=0||offset.getZ()!=0;
	}

	@Nonnull
	public <T extends TileEntityIWMultiblock> T masterOr(T here, @Nonnull T def) {
		T master = master(here);
		return master!=null?master:def;
	}
	public void disassemble() {
		if (formed && !world.isRemote) {
			BlockPos startPos = getOrigin();
			BlockPos masterPos = getPos().subtract(offset);
			long time = world.getTotalWorldTime();
			Vec3i size = getSize();
			for (int up = 0; up < size.getX(); up++) {
				for (int forward = 0; forward < size.getY(); forward++) {
					for (int right = 0; right < size.getZ(); right++) {
						BlockPos pos = MiscUtils.offset(startPos, facing, mirrored, right, forward, up);
						TileEntity te = world.getTileEntity(pos);
						if (te instanceof TileEntityIWMultiblock) {
							TileEntityIWMultiblock part = (TileEntityIWMultiblock) te;
							Vec3i diff = pos.subtract(masterPos);
							if (part.offset.equals(diff) && time != part.onlyLocalDissassembly) {
								part.formed = false;
								if (!pos.equals(this.pos)) {
									part.getOriginalBlockPlacer().accept(world, pos);
								} else if (part.getOriginalBlock()!=null) {
									ItemStack drop = MiscUtils.getItemStack(part.getOriginalBlock(), world, pos);
									world.spawnEntity(new EntityItem(world, pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5, drop));
								}
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void writeNBT(NBTTagCompound out, boolean updatePacket) {
		out.setInteger(FACING, facing.getHorizontalIndex());
		out.setIntArray(OFFSET, new int[]{offset.getX(), offset.getY(), offset.getZ()});
		out.setBoolean(MIRRORED, mirrored);
		out.setBoolean(FORMED, formed);
	}

	@Override
	public void readNBT(NBTTagCompound in, boolean updatePacket) {
		formed = in.getBoolean(FORMED);
		mirrored = in.getBoolean(MIRRORED);
		int[] offset = in.getIntArray(OFFSET);
		this.offset = new Vec3i(offset[0], offset[1], offset[2]);
		facing = EnumFacing.getHorizontal(in.getInteger(FACING));
	}

	public Vec3i getSize() {
		return size;
	}

	public int getRight() {
		return dot(offset, facing.rotateY().getDirectionVec())*(mirrored?-1:1);
	}

	public int getForward() {
		return dot(offset, facing.getDirectionVec());
	}

	protected int dot(Vec3i a, Vec3i b) {
		return a.getX()*b.getX()+a.getY()*b.getY()+a.getZ()*b.getZ();
	}


	@Nonnull
	@Override
	public EnumFacing getFacing() {
		return facing;
	}

	@Override
	public void setFacing(@Nonnull EnumFacing facing) {
		this.facing = facing;
	}

	@Override
	public int getFacingLimitation() {
		return 2;
	}

	@Override
	public boolean mirrorFacingOnPlacement(@Nonnull EntityLivingBase placer) {
		return false;
	}

	@Override
	public boolean canHammerRotate(@Nonnull EnumFacing side, float hitX, float hitY, float hitZ, @Nonnull EntityLivingBase entity) {
		return false;
	}

	@Override
	public boolean canRotate(@Nonnull EnumFacing axis) {
		return false;
	}
}

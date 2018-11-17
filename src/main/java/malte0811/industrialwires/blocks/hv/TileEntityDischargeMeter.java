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

package malte0811.industrialwires.blocks.hv;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import malte0811.industrialwires.IndustrialWires;
import malte0811.industrialwires.blocks.IBlockBoundsIW.IBlockBoundsDirectional;
import malte0811.industrialwires.blocks.TileEntityIWBase;
import malte0811.industrialwires.hv.IMarxTarget;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;

public class TileEntityDischargeMeter extends TileEntityIWBase implements IPlayerInteraction, IMarxTarget,
		IBlockBoundsDirectional, IDirectionalTile {
	private static final String HAS_WIRE = "hasWire";
	private static final String FACING = "facing";
	private static final String LAST_DISCHARGE = "last";
	boolean hasWire;
	EnumFacing facing = EnumFacing.NORTH;
	double lastDischarge = -1;

	@Override
	public void writeNBT(NBTTagCompound out, boolean updatePacket) {
		out.setByte(FACING, (byte) facing.getHorizontalIndex());
		out.setBoolean(HAS_WIRE, hasWire);
		out.setDouble(LAST_DISCHARGE, lastDischarge);
	}

	@Override
	public void readNBT(NBTTagCompound in, boolean updatePacket) {
		hasWire = in.getBoolean(HAS_WIRE);
		facing = EnumFacing.byHorizontalIndex(in.getByte(FACING));
		lastDischarge = in.getDouble(LAST_DISCHARGE);
		aabb = null;
	}

	@Override
	public boolean interact(@Nonnull EnumFacing side, @Nonnull EntityPlayer player, @Nonnull EnumHand hand,
							@Nonnull ItemStack heldItem, float hitX, float hitY, float hitZ) {
		if (!world.isRemote) {
			if (hasWire)
				return false;
			if (ApiUtils.compareToOreName(heldItem, "wireAluminum")) {
				hasWire = true;
				heldItem.shrink(1);
				triggerRenderUpdate();
			} else if (lastDischarge > 0) {
				player.sendMessage(new TextComponentTranslation(IndustrialWires.MODID + ".chat.marxEnergy",
						String.format("%.1f", lastDischarge/1e3)));
				lastDischarge = -1;
			}
			markDirty();
		}
		return true;
	}

	@Override
	public boolean onHit(double energy, TileEntityMarx master) {
		if (hasWire) {
			hasWire = false;
			lastDischarge = energy;
			triggerRenderUpdate();
			markDirty();
			return true;
		}
		return false;
	}

	AxisAlignedBB aabb = null;
	@Override
	public AxisAlignedBB getBoundingBoxNoRot() {
		return new AxisAlignedBB(1F/16, 0, 5F/16,
					10F/16, (hasWire?15F:14F)/16, 11F/16);
	}

	@Override
	public AxisAlignedBB getBoundingBox() {
		if (aabb==null) {
			aabb = IBlockBoundsDirectional.super.getBoundingBox();
		}
		return aabb;
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

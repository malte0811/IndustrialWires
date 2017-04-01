/*
 * This file is part of Industrial Wires.
 * Copyright (C) 2016-2017 malte0811
 *
 * Industrial Wires is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Industrial Wires is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Industrial Wires.  If not, see <http://www.gnu.org/licenses/>.
 */

package malte0811.industrialWires.blocks.controlpanel;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import malte0811.industrialWires.blocks.IBlockBoundsIW;
import malte0811.industrialWires.blocks.TileEntityIWBase;
import malte0811.industrialWires.util.MiscUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TileEntityPanel extends TileEntityIWBase implements IDirectionalTile, IBlockBoundsIW, IPlayerInteraction, ITickable {
	private PropertyComponents.PanelRenderProperties components = new PropertyComponents.PanelRenderProperties();
	boolean firstTick = true;
	// non-rendered properties
	//TODO does the lambda stuff cause GC issues?
	{
		for (int i = 0;i<16;i++) {
			int color = EnumDyeColor.byMetadata(i).getMapColor().colorValue;
			IndicatorLight ind = new IndicatorLight(0, i, color);
			LightedButton btn = new LightedButton(color, false, true, 1, i);
			Label lbl = new Label("->", color);
			ind.setX(0);
			ind.setY(i/16F);
			ind.setPanelHeight(.5F);
			lbl.setX(2/16F);
			lbl.setY(i/16F);
			lbl.setPanelHeight(.5F);
			btn.setX(5/16F);
			btn.setY(i/16F);
			btn.setPanelHeight(.5F);
			components.add(ind);
			components.add(lbl);
			components.add(btn);
		}
	}

	@Override
	public void update() {
		for (PanelComponent pc:components) {
			pc.update(this);
		}
		if (!worldObj.isRemote) {
			if (firstTick) {
				List<BlockPos> parts = MiscUtils.discoverPanelParts(worldObj, pos);
				for (BlockPos bp:parts) {
					TileEntity te = worldObj.getTileEntity(bp);
					if (te instanceof TileEntityRSPanelConn) {
						//TODO deal with people adding 2 RS ports with the same ID!
						((TileEntityRSPanelConn) te).requestRSConn(this);
					}
				}
				firstTick = false;
			}
		}
	}

	@Override
	public void writeNBT(NBTTagCompound out, boolean updatePacket) {
		NBTTagList comps = new NBTTagList();
		for (PanelComponent p:components) {
			NBTTagCompound nbt = new NBTTagCompound();
			p.writeToNBT(nbt);
			comps.appendTag(nbt);
		}
		out.setTag("components", comps);
		out.setInteger("facing", components.facing.getHorizontalIndex());
		out.setFloat("height", components.height);
		out.setInteger("top", components.top.getIndex());
	}

	@Override
	public void readNBT(NBTTagCompound in, boolean updatePacket) {
		NBTTagList l = in.getTagList("components", 10);
		components.clear();
		for (int i = 0;i<l.tagCount();i++) {
			PanelComponent pc = PanelComponent.read(l.getCompoundTagAt(i));
			if (pc!=null) {
				components.add(pc);
			}
		}
		components.facing = EnumFacing.getHorizontal(in.getInteger("facing"));
		components.height = in.getFloat("height");
		components.top = EnumFacing.getFront(in.getInteger("top"));
		defAABB = null;
	}

	@Override
	public EnumFacing getFacing() {
		return components.facing;
	}

	@Override
	public void setFacing(EnumFacing facing) {
		this.components.facing = facing;
	}

	@Override
	public int getFacingLimitation() {
		return 2;
	}

	@Override
	public EnumFacing getFacingForPlacement(EntityLivingBase placer, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
		switch (side) {
		case UP:
			components.top = EnumFacing.UP;
			return IDirectionalTile.super.getFacingForPlacement(placer, pos, side, hitX, hitY, hitZ);
		case DOWN:
			components.top = EnumFacing.DOWN;
			return IDirectionalTile.super.getFacingForPlacement(placer, pos, side, hitX, hitY, hitZ);
		case NORTH:
		case SOUTH:
		case WEST:
		case EAST:
			components.top = side;
			return EnumFacing.SOUTH;//Should not matter
		}
		return components.facing;
	}

	@Override
	public boolean mirrorFacingOnPlacement(EntityLivingBase placer) {
		return false;
	}

	@Override
	public boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity) {
		return false;
	}

	@Override
	public boolean canRotate(EnumFacing axis) {
		return false;
	}

	private AxisAlignedBB defAABB;
	@Override
	public AxisAlignedBB getBoundingBox() {
		if (defAABB==null) {
			defAABB = apply(components.getPanelBaseTransform(), new AxisAlignedBB(0, 0, 0, 1, components.height, 1));
		}
		return defAABB;
	}

	public PropertyComponents.PanelRenderProperties getComponents() {
		return components;
	}
	public AxisAlignedBB apply(Matrix4 mat, AxisAlignedBB in) {
		Vec3d min = new Vec3d(in.minX, in.minY, in.minZ);
		Vec3d max = new Vec3d(in.maxX, in.maxY, in.maxZ);
		min = mat.apply(min);
		max = mat.apply(max);
		return new AxisAlignedBB(min, max);
	}
	@Nullable
	public Pair<PanelComponent, RayTraceResult> getSelectedComponent(EntityPlayer player, Vec3d hit, boolean hitAbs) {
		Matrix4 mat = components.getPanelTopTransform();
		for (PanelComponent pc : components) {
			AxisAlignedBB box = pc.getBlockRelativeAABB();
			if (box!=null) {
				box = apply(mat, box.expandXyz(.002));
				Vec3d hitVec = hitAbs ? hit.addVector(-pos.getX(), -pos.getY(), -pos.getZ()) : hit;
				Vec3d playerPos = Minecraft.getMinecraft().thePlayer.getPositionVector().addVector(-pos.getX(), player.getEyeHeight() - pos.getY(), -pos.getZ());
				RayTraceResult ray = box.calculateIntercept(playerPos, hitVec);
				if (ray != null) {
					return new ImmutablePair<>(pc, ray);
				}
			}
		}
		return null;
	}

	@Override
	public boolean interact(EnumFacing side, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ) {
		Pair<PanelComponent, RayTraceResult> pc = getSelectedComponent(player, new Vec3d(hitX, hitY, hitZ), false);
		return pc != null && pc.getLeft().interactWith(pc.getRight().hitVec, this);
	}

	public void triggerRenderUpdate() {
		IBlockState state = worldObj.getBlockState(pos);
		worldObj.notifyBlockUpdate(pos,state,state,3);
		worldObj.addBlockEvent(pos, state.getBlock(), 255, 0);
	}
}

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

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.IBlockBoundsIW;
import malte0811.industrialWires.blocks.TileEntityIWBase;
import malte0811.industrialWires.controlpanel.*;
import malte0811.industrialWires.network.MessagePanelInteract;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TileEntityPanel extends TileEntityIWBase implements IDirectionalTile, IBlockBoundsIW, IPlayerInteraction, ITickable, IEBlockInterfaces.ITileDrop {
	private PropertyComponents.PanelRenderProperties components = new PropertyComponents.PanelRenderProperties();
	public boolean firstTick = true;
	// non-rendered properties
	private Set<TileEntityRSPanelConn> rsPorts = new HashSet<>();

	{
		for (int i = 2; i < 14; i++) {
			int color = EnumDyeColor.byMetadata(i - 2).getMapColor().colorValue;
			IndicatorLight ind = new IndicatorLight(0, (byte) (i - 2), color);
			LightedButton btn = new LightedButton(color, false, true, 1, i - 2);
			Label lbl = new Label("->", color);
			ind.setX(0);
			ind.setY(i / 16F);
			ind.setPanelHeight(components.height);
			lbl.setX(2 / 16F);
			lbl.setY(i / 16F);
			lbl.setPanelHeight(components.height);
			btn.setX(5 / 16F);
			btn.setY(i / 16F);
			btn.setPanelHeight(components.height);
			components.add(ind);
			components.add(lbl);
			components.add(btn);
		}
	}

	@Override
	public void update() {
		for (PanelComponent pc : components) {
			pc.update(this);
		}
		if (!world.isRemote) {
			if (firstTick) {
				List<BlockPos> parts = PanelUtils.discoverPanelParts(world, pos, 100);
				for (BlockPos bp : parts) {
					TileEntity te = world.getTileEntity(bp);
					if (te instanceof TileEntityRSPanelConn && !rsPorts.contains(te)) {
						((TileEntityRSPanelConn) te).registerPanel(this);
					}
				}
				firstTick = false;
			}
		}
	}

	@Override
	public void writeNBT(NBTTagCompound out, boolean updatePacket) {
		writeToItemNBT(out, false);
		out.setInteger("facing", components.facing.getHorizontalIndex());
		out.setInteger("top", components.top.getIndex());
	}

	@Override
	public void readNBT(NBTTagCompound in, boolean updatePacket) {
		readFromItemNBT(in);
		components.facing = EnumFacing.getHorizontal(in.getInteger("facing"));
		components.top = EnumFacing.getFront(in.getInteger("top"));
	}

	@Override
	@Nonnull
	public ItemStack getTileDrop(@Nonnull EntityPlayer player, @Nonnull IBlockState state) {
		NBTTagCompound ret = new NBTTagCompound();
		writeToItemNBT(ret, true);
		ItemStack retStack = new ItemStack(IndustrialWires.panel, 1, BlockTypes_Panel.TOP.ordinal());
		retStack.setTagCompound(ret);
		return retStack;
	}

	@Override
	public void readOnPlacement(@Nullable EntityLivingBase placer, @Nonnull ItemStack stack) {
		if (!stack.isEmpty() && stack.hasTagCompound()) {
			readFromItemNBT(stack.getTagCompound());
		}
	}

	public void readFromItemNBT(@Nullable NBTTagCompound nbt) {
		if (nbt != null) {
			NBTTagList l = nbt.getTagList("components", 10);
			PanelUtils.readListFromNBT(l, components);
			components.height = nbt.getFloat("height");
			components.angle = nbt.getFloat("angle");
		}
		defAABB = null;
	}

	public void writeToItemNBT(NBTTagCompound nbt, boolean toItem) {
		NBTTagList comps = new NBTTagList();
		for (PanelComponent p : components) {
			NBTTagCompound nbtInner = new NBTTagCompound();
			p.writeToNBT(nbtInner, toItem);
			comps.appendTag(nbtInner);
		}
		nbt.setTag("components", comps);
		nbt.setFloat("height", components.height);
		nbt.setFloat("angle", components.angle);
	}

	@Nonnull
	@Override
	public EnumFacing getFacing() {
		return components.facing;
	}

	@Override
	public void setFacing(@Nonnull EnumFacing facing) {
		this.components.facing = facing;
	}

	@Override
	public int getFacingLimitation() {
		return 0;
	}

	@Nonnull
	@Override
	public EnumFacing getFacingForPlacement(@Nonnull EntityLivingBase placer, @Nonnull BlockPos pos, @Nonnull EnumFacing side, float hitX, float hitY, float hitZ) {
		switch (side) {
		case UP:
			components.top = EnumFacing.UP;
			return EnumFacing.fromAngle(placer.rotationYaw);
		case DOWN:
			components.top = EnumFacing.DOWN;
			return EnumFacing.fromAngle(placer.rotationYaw);
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

	private AxisAlignedBB defAABB;

	@Override
	public AxisAlignedBB getBoundingBox() {
		if (defAABB == null) {
			defAABB = apply(components.getPanelBaseTransform(), new AxisAlignedBB(0, 0, 0, 1, components.getMaxHeight(), 1));
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
		return new AxisAlignedBB(min.x, min.y, min.z, max.x, max.y, max.z);
	}

	@Nullable
	public Pair<PanelComponent, RayTraceResult> getSelectedComponent(EntityPlayer player, Vec3d hit, boolean hitAbs) {
		//TODO prevent clicking through the back of the panel
		Matrix4 mat = components.getPanelTopTransform();
		mat.invert();
		PanelComponent retPc = null;
		RayTraceResult retRay = null;
		Vec3d playerPosRelative = player.getPositionVector().addVector(-pos.getX(), player.getEyeHeight() - pos.getY(), -pos.getZ());
		Vec3d playerPosTransformed = mat.apply(playerPosRelative);
		for (PanelComponent pc : components) {
			AxisAlignedBB box = pc.getBlockRelativeAABB();
			if (box.maxY > box.minY) {
				box = box.grow(.002);
				Vec3d hitVec = hitAbs ? hit.addVector(-pos.getX(), -pos.getY(), -pos.getZ()) : hit;
				hitVec = hitVec.subtract(playerPosRelative.subtract(hitVec).scale(10));
				RayTraceResult ray = box.calculateIntercept(playerPosTransformed, mat.apply(hitVec));
				if (ray != null) {
					if (retPc == null) {
						ray.hitVec = ray.hitVec.subtract(pc.getX(), 0, pc.getY());
						retPc = pc;
						retRay = ray;
					} else {
						double oldDist = retRay.hitVec.subtract(playerPosRelative).lengthSquared();
						double newDist = ray.hitVec.subtract(playerPosRelative).lengthSquared();
						if (newDist < oldDist) {
							ray.hitVec = ray.hitVec.subtract(pc.getX(), 0, pc.getY());
							retPc = pc;
							retRay = ray;
						}
					}
				}
			}
		}
		return retPc != null ? new ImmutablePair<>(retPc, retRay) : null;
	}

	@Override
	public boolean interact(@Nonnull EnumFacing side, @Nonnull EntityPlayer player, @Nonnull EnumHand hand, @Nonnull ItemStack heldItem, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			Pair<PanelComponent, RayTraceResult> pc = getSelectedComponent(player, new Vec3d(hitX, hitY, hitZ), false);
			if (pc != null) {
				IndustrialWires.packetHandler.sendToServer(new MessagePanelInteract(this, components.indexOf(pc.getKey()), pc.getRight().hitVec));
			}
		}
		return true;
	}

	public void interactServer(Vec3d hitRelative, int pcId, EntityPlayerMP player) {
		if (pcId >= 0 && pcId < components.size()) {
			components.get(pcId).interactWith(hitRelative, this, player);
		}
	}

	public void triggerRenderUpdate() {
		IBlockState state = world.getBlockState(pos);
		world.notifyBlockUpdate(pos, state, state, 3);
		world.addBlockEvent(pos, state.getBlock(), 255, 0);
	}

	public void registerRS(TileEntityRSPanelConn te) {
		rsPorts.add(te);
	}

	public void unregisterRS(TileEntityRSPanelConn te) {
		if (!tileEntityInvalid) {
			rsPorts.remove(te);
		}
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		for (PanelComponent pc : components) {
			pc.invalidate(this);
		}
		removeAllRSCons();
	}

	public void removeAllRSCons() {
		for (TileEntityRSPanelConn rs : rsPorts) {
			rs.unregisterPanel(this, true);
		}
		rsPorts.clear();
		firstTick = true;
	}

	@Override
	public void invalidate() {
		super.invalidate();
		for (PanelComponent pc : components) {
			pc.invalidate(this);
		}
		removeAllRSCons();
	}
}

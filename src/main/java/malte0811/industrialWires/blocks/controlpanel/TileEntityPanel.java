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
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.IBlockBoundsIW;
import malte0811.industrialWires.blocks.TileEntityIWBase;
import malte0811.industrialWires.network.MessagePanelInteract;
import malte0811.industrialWires.util.MiscUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
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

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TileEntityPanel extends TileEntityIWBase implements IDirectionalTile, IBlockBoundsIW, IPlayerInteraction, ITickable {
	private PropertyComponents.PanelRenderProperties components = new PropertyComponents.PanelRenderProperties();
	private boolean firstTick = true;
	// non-rendered properties
	private Set<TileEntityRSPanelConn> rsPorts = new HashSet<>();
	{
		for (int i = 2;i<14;i++) {
			int color = EnumDyeColor.byMetadata(i-2).getMapColor().colorValue;
			IndicatorLight ind = new IndicatorLight(0, i-2, color);
			LightedButton btn = new LightedButton(color, false, true, 1, i-2);
			Label lbl = new Label("->", color);
			ind.setX(0);
			ind.setY(i/16F);
			ind.setPanelHeight(components.height);
			lbl.setX(2/16F);
			lbl.setY(i/16F);
			lbl.setPanelHeight(components.height);
			btn.setX(5/16F);
			btn.setY(i/16F);
			btn.setPanelHeight(components.height);
			components.add(ind);
			components.add(lbl);
			components.add(btn);
		}
		Slider slid = new Slider(.5F, 0x00ff00, true, 1, (byte)1);
		slid.setX(.4F);
		slid.setY(.25F);
		slid.setPanelHeight(components.height);
		components.add(slid);
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
						((TileEntityRSPanelConn) te).registerPanel(this);
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
		return new AxisAlignedBB(min, max);
	}

	@Nullable
	public Pair<PanelComponent, RayTraceResult> getSelectedComponent(EntityPlayer player, Vec3d hit, boolean hitAbs) {
		Matrix4 mat = components.getPanelTopTransform();
		PanelComponent retPc = null;
		RayTraceResult retRay = null;
		Vec3d playerPos = Minecraft.getMinecraft().thePlayer.getPositionVector().addVector(-pos.getX(), player.getEyeHeight() - pos.getY(), -pos.getZ());
		for (PanelComponent pc : components) {
			AxisAlignedBB box = pc.getBlockRelativeAABB();
			if (box!=null) {
				box = apply(mat, box.expandXyz(.002));
				Vec3d hitVec = hitAbs ? hit.addVector(-pos.getX(), -pos.getY(), -pos.getZ()) : hit;
				hitVec = hitVec.scale(2).subtract(playerPos);
				RayTraceResult ray = box.calculateIntercept(playerPos, hitVec);
				if (ray != null) {
					if (retPc==null) {
						retPc = pc;
						retRay = ray;
					} else {
						double oldDist = retRay.hitVec.subtract(playerPos).lengthSquared();
						double newDist = ray.hitVec.subtract(playerPos).lengthSquared();
						if (newDist<oldDist) {
							retPc = pc;
							retRay = ray;
						}
					}
				}
			}
		}
		return retPc!=null?new ImmutablePair<>(retPc, retRay):null;
	}

	@Override
	public boolean interact(EnumFacing side, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ) {
		if (worldObj.isRemote) {
			Pair<PanelComponent, RayTraceResult> pc = getSelectedComponent(player, new Vec3d(hitX, hitY, hitZ), false);
			if (pc != null) {
				Matrix4 inv = components.getPanelTopTransform();
				inv.translate(pc.getLeft().getX(), 0, pc.getLeft().getY());
				inv.invert();
				Vec3d hitVec = inv.apply(pc.getRight().hitVec);
				hitVec.subtract(pc.getLeft().getX(), 0, -pc.getLeft().getY());
				IndustrialWires.packetHandler.sendToServer(new MessagePanelInteract(this, components.indexOf(pc.getKey()), hitVec));
			}
		}
		return true;
	}

	public void interactServer(Vec3d hitRelative, int pcId) {
		if (pcId>=0&&pcId<components.size()) {
			components.get(pcId).interactWith(hitRelative, this);
		}
	}

	public void triggerRenderUpdate() {
		IBlockState state = worldObj.getBlockState(pos);
		worldObj.notifyBlockUpdate(pos,state,state,3);
		worldObj.addBlockEvent(pos, state.getBlock(), 255, 0);
	}

	public void registerRS(TileEntityRSPanelConn te) {
		rsPorts.add(te);
	}
	public void unregisterRS(TileEntityRSPanelConn te) {
		rsPorts.remove(te);
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		for (TileEntityRSPanelConn rs:rsPorts) {
			rs.unregisterPanel(this, true);
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		for (TileEntityRSPanelConn rs:rsPorts) {
			rs.unregisterPanel(this, true);
		}
	}
}

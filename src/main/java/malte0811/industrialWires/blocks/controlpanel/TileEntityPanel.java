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
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class TileEntityPanel extends TileEntityIWBase implements IDirectionalTile, IBlockBoundsIW, IPlayerInteraction, ITickable {
	PropertyComponents.PanelRenderProperties components = new PropertyComponents.PanelRenderProperties();
	// non-rendered properties
	//relative positions!
	private List<BlockPos> rsWireConns = new ArrayList<>();
	{
		Random r = new Random();
		PanelComponent b = new LightedButton(0xff<<(8*r.nextInt(3)), false, false, 0, 0);
		b.setX(3/16F);
		b.setY(.75F);
		b.setPanelHeight(components.height);
		components.add(b);
		b = new LightedButton(0xff<<(8*r.nextInt(3)), false, true, 0, 1);
		b.setX(8/16F);
		b.setY(.75F);
		b.setPanelHeight(components.height);
		components.add(b);
		b = new LightedButton(0xff<<(8*r.nextInt(3)), false, true, 0, 2);
		b.setX(13/16F);
		b.setY(.75F);
		b.setPanelHeight(components.height);
		components.add(b);
		b = new Label("TESTtextIII");
		b.setX(3/16F);
		b.setY(.25F);
		b.setPanelHeight(components.height);
		components.add(b);

		rsWireConns.add(new BlockPos(0, -1, 0));//one RS output 1 block below the panel
	}

	@Override
	public void update() {
		for (PanelComponent pc:components) {
			pc.update(this);
		}
		if (!worldObj.isRemote) {
			for (int i = 0; i < rsWireConns.size(); i++) {
				TileEntityRSPanelConn rs = getRSConn(i);
				if (rs != null)
					rs.flushRS();
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
		NBTTagList rsConns = new NBTTagList();
		for (BlockPos pos:rsWireConns) {
			rsConns.appendTag(new NBTTagLong(pos.toLong()));
		}
		out.setTag("rsConns", rsConns);
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
		l = in.getTagList("rsConns", 4);
		rsWireConns.clear();
		for (int i = 0;i<l.tagCount();i++) {
			rsWireConns.add(BlockPos.fromLong(((NBTTagLong)l.get(i)).getLong()));
		}
		components.facing = EnumFacing.getHorizontal(in.getInteger("facing"));
		components.height = in.getFloat("height");
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
			defAABB = new AxisAlignedBB(0, 0, 0, 1, components.height, 1);
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

	public TileEntityRSPanelConn getRSConn(int id) {
		if (id < 0 || id >= rsWireConns.size()) {
			return null;
		}
		TileEntity te = worldObj.getTileEntity(pos.add(rsWireConns.get(id)));
		if (te instanceof TileEntityRSPanelConn) {
			return (TileEntityRSPanelConn) te;
		}
		return null;
	}
}

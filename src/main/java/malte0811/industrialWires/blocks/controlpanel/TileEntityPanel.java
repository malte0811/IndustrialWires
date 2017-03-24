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
import malte0811.industrialWires.blocks.IBlockBoundsIW;
import malte0811.industrialWires.blocks.TileEntityIWBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.Random;

public class TileEntityPanel extends TileEntityIWBase implements IDirectionalTile, IBlockBoundsIW, IPlayerInteraction, ITickable {
	PropertyComponents.ComponentList components = new PropertyComponents.ComponentList();
	EnumFacing facing = EnumFacing.NORTH;
	public float height = .5F;

	{
		Random r = new Random();
		LightedButton b = new LightedButton(0xff<<(8*r.nextInt(3)), false, false);
		IELogger.info(Integer.toHexString(b.color));
		b.setX(3/16F);
		b.setY(.5F);
		b.setPanelHeight(height);
		components.add(b);
		b = new LightedButton(0xff<<(8*r.nextInt(3)), false, true);
		IELogger.info(Integer.toHexString(b.color));
		b.setX(8/16F);
		b.setY(.5F);
		b.setPanelHeight(height);
		components.add(b);
		b = new LightedButton(0xff<<(8*r.nextInt(3)), false, true);
		IELogger.info(Integer.toHexString(b.color));
		b.setX(13/16F);
		b.setY(.5F);
		b.setPanelHeight(height);
		components.add(b);
	}

	@Override
	public void update() {
		for (PanelComponent pc:components) {
			pc.update(this);
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
		out.setInteger("facing", facing.getHorizontalIndex());
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
		facing = EnumFacing.getHorizontal(in.getInteger("facing"));
	}

	@Override
	public EnumFacing getFacing() {
		return facing;
	}

	@Override
	public void setFacing(EnumFacing facing) {
		this.facing = facing;
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

	private static final AxisAlignedBB defAABB = new AxisAlignedBB(0, 0, 0, 1, .5, 1);
	@Override
	public AxisAlignedBB getBoundingBox() {
		return defAABB;
	}

	public PropertyComponents.ComponentList getComponents() {
		return components;
	}
	@Nullable
	public PanelComponent getSelectedComponent(EntityPlayer player, Vec3d hit, boolean hitAbs) {
		for (PanelComponent pc : components) {
			AxisAlignedBB box = pc.getBlockRelativeAABB().expandXyz(.002);
			Vec3d hitVec = hitAbs?hit.addVector(-pos.getX(), -pos.getY(), -pos.getZ()):hit;
			Vec3d playerPos = Minecraft.getMinecraft().thePlayer.getPositionVector().addVector(-pos.getX(), player.getEyeHeight()-pos.getY(), -pos.getZ());
			if (box.calculateIntercept(playerPos, hitVec)!=null) {
				return pc;
			}
		}
		return null;
	}

	@Override
	public boolean interact(EnumFacing side, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ) {
		PanelComponent pc = getSelectedComponent(player, new Vec3d(hitX, hitY, hitZ), false);
		if (pc!=null) {
			Vec3d hitRel = new Vec3d(hitX-pc.getX(), hitY-height, hitZ-pc.getY());
			return pc.interactWith(hitRel, this);
		}
		return false;
	}

	public void triggerRenderUpdate() {
		IBlockState state = worldObj.getBlockState(pos);
		worldObj.notifyBlockUpdate(pos,state,state,3);
		worldObj.addBlockEvent(pos, state.getBlock(), 255, 0);
	}
}

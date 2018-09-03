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

package malte0811.industrialwires.blocks.controlpanel;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import malte0811.industrialwires.IndustrialWires;
import malte0811.industrialwires.blocks.IBlockBoundsIW;
import malte0811.industrialwires.controlpanel.*;
import malte0811.industrialwires.controlpanel.ControlPanelNetwork.RSChannel;
import malte0811.industrialwires.network.MessagePanelInteract;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
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

import static malte0811.industrialwires.util.MiscUtils.apply;

public class TileEntityPanel extends TileEntityGeneralCP implements IDirectionalTile, IBlockBoundsIW, IPlayerInteraction,
		ITickable, IEBlockInterfaces.ITileDrop {
	protected PropertyComponents.PanelRenderProperties components = new PropertyComponents.PanelRenderProperties();

	{
		int[] colors = {
				16383998, 16351261, 13061821, 3847130, 16701501, 8439583, 15961002,
				4673362, 10329495, 1481884, 8991416, 3949738, 8606770, 6192150
		};
		for (int i = 2; i < 14; i++) {
			int color = colors[i-2];
			IndicatorLight ind = new IndicatorLight(new RSChannel(0, (byte) (i - 2)), color);
			LightedButton btn = new LightedButton(color, false, true,
					new RSChannel(0, (byte)(i-2)));
			Label lbl = new Label("->", color);
			ind.setX(0);
			ind.setY(i / 16F);
			ind.setPanelHeight(components.getHeight());
			lbl.setX(2 / 16F);
			lbl.setY(i / 16F);
			lbl.setPanelHeight(components.getHeight());
			btn.setX(5 / 16F);
			btn.setY(i / 16F);
			btn.setPanelHeight(components.getHeight());
			components.add(ind);
			components.add(lbl);
			components.add(btn);
		}
		for (PanelComponent pc:components) {
			pc.setPanel(this);
		}
	}

	@Override
	public void update() {
		if (!world.isRemote) {
			for (PanelComponent pc : components) {
				pc.update();
			}
		}
	}

	@Override
	public void setNetworkAndInit(ControlPanelNetwork newNet) {
		super.setNetworkAndInit(newNet);
		for (PanelComponent pc : components) {
			pc.setNetwork(newNet);
		}
	}

	@Override
	public void writeNBT(NBTTagCompound out, boolean updatePacket) {
		writeToItemNBT(out, false);
		out.setInteger("facing", components.getFacing().getHorizontalIndex());
		out.setInteger("top", components.getTop().getIndex());
	}

	@Override
	public void readNBT(NBTTagCompound in, boolean updatePacket) {
		readFromItemNBT(in);
		components.setFacing(EnumFacing.getHorizontal(in.getInteger("facing")));
		components.setTop(EnumFacing.getFront(in.getInteger("top")));
	}

	@Override
	@Nonnull
	public ItemStack getTileDrop(@Nullable EntityPlayer player, @Nonnull IBlockState state) {
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
			panelNetwork.removeIOFor(this);
			for (PanelComponent pc : components) {
				pc.setPanel(this);
				if (world == null || !world.isRemote) {
					pc.setNetwork(panelNetwork);
				}
			}
			components.setHeight(nbt.getFloat("height"));
			components.setAngle(nbt.getFloat("angle"));
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
		nbt.setFloat("height", components.getHeight());
		nbt.setFloat("angle", components.getAngle());
	}

	@Nonnull
	@Override
	public EnumFacing getFacing() {
		return components.getFacing();
	}

	@Override
	public void setFacing(@Nonnull EnumFacing facing) {
		this.components.setFacing(facing);
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
			components.setTop(EnumFacing.UP);
			return EnumFacing.fromAngle(placer.rotationYaw);
		case DOWN:
			components.setTop(EnumFacing.DOWN);
			return EnumFacing.fromAngle(-placer.rotationYaw);
		case NORTH:
		case SOUTH:
		case WEST:
		case EAST:
			components.setTop(side);
			return EnumFacing.SOUTH;//Should not matter
		}
		return components.getFacing();
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

	protected AxisAlignedBB defAABB;

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

	@Nullable
	public Pair<PanelComponent, RayTraceResult> getSelectedComponent(EntityPlayer player, Vec3d hitVec, boolean hitAbs) {
		Matrix4 mat = components.getPanelTopTransformInverse();
		PanelComponent retPc = null;
		RayTraceResult retRay = null;
		Vec3d playerPosRelative = player.getPositionVector().addVector(-pos.getX(), player.getEyeHeight() - pos.getY(), -pos.getZ());
		Vec3d playerPosTransformed = mat.apply(playerPosRelative);
		Vec3d hitRel = hitAbs ? hitVec.addVector(-pos.getX(), -pos.getY(), -pos.getZ()) : hitVec;
		RayTraceResult r = getBoundingBox().calculateIntercept(playerPosRelative, playerPosRelative.add(player.getLookVec().scale(200)));
		if (r != null && r.hitVec != null) {
			hitRel = r.hitVec;
		}
		Vec3d ray = hitRel.subtract(playerPosRelative.subtract(hitRel).scale(10));
		Vec3d rayTransformed = mat.apply(ray);
		{
			//Check whether the player is clicking on the back of the panel
			Vec3d hitTransformed = mat.apply(new Vec3d(hitRel.x, hitRel.y, hitRel.z));
			if (hitTransformed.y < 0) {
				return null;
			}
		}
		for (PanelComponent pc : components) {
			AxisAlignedBB box = pc.getBlockRelativeAABB();
			if (box.maxY > box.minY) {
				box = box.grow(.002);
				RayTraceResult hit = box.calculateIntercept(playerPosTransformed, rayTransformed);
				if (hit != null) {
					if (retPc == null) {
						retPc = pc;
						retRay = hit;
					} else {
						double oldDist = retRay.hitVec.subtract(playerPosRelative).lengthSquared();
						double newDist = hit.hitVec.subtract(playerPosRelative).lengthSquared();
						if (newDist < oldDist) {
							retPc = pc;
							retRay = hit;
						}
					}
				}
			}
		}
		if (retRay != null) {
			retRay.hitVec = retRay.hitVec.subtract(retPc.getX(), 0, retPc.getY());
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
			components.get(pcId).interactWith(hitRelative, player);
		}
	}
}

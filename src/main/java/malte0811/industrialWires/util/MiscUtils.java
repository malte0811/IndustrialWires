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

package malte0811.industrialWires.util;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.common.collect.ImmutableSet;
import malte0811.industrialWires.IndustrialWires;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;

public final class MiscUtils {
	private MiscUtils() {
	}

	public static List<BlockPos> discoverLocal(World w, BlockPos here, BiPredicate<BlockPos, Integer> isValid) {
		List<BlockPos> ret = new ArrayList<>();
		List<BlockPos> open = new ArrayList<>();
		open.add(here);
		while (!open.isEmpty()) {
			BlockPos curr = open.get(0);
			ret.add(curr);
			open.remove(0);
			for (EnumFacing f : EnumFacing.VALUES) {
				BlockPos next = curr.offset(f);
				if (!open.contains(next) && !ret.contains(next) && isValid.test(next, ret.size())) {
					open.add(next);
				}
			}
		}
		return ret;
	}

	public static BlockPos offset(BlockPos p, EnumFacing f, boolean mirror, Vec3i relative) {
		return offset(p, f, mirror, relative.getX(), relative.getZ(), relative.getY());
	}
	/**
	 * @param mirror inverts right
	 */
	public static BlockPos offset(BlockPos p, EnumFacing f, boolean mirror, int right, int forward, int up) {
		if (mirror) {
			right *= -1;
		}
		return p.offset(f, forward).offset(f.rotateY(), right).add(0, up, 0);
	}

	public static Vec3d offset(Vec3d p, EnumFacing f, boolean mirror, Vec3d relative) {
		return offset(p, f, mirror, relative.x, relative.z, relative.y);
	}

	public static Vec3d offset(Vec3d p, EnumFacing f, boolean mirror, double right, double forward, double up) {
		if (mirror) {
			right *= -1;
		}
		return offset(offset(p, f, forward), f.rotateY(), right).addVector(0, up, 0);
	}

	public static Vec3d offset(Vec3d in, EnumFacing f, double amount) {
		if (amount==0) {
			return in;
		}
		return in.addVector(f.getFrontOffsetX()*amount, f.getFrontOffsetY()*amount, f.getFrontOffsetZ()*amount);
	}

	/**
	 * Calculates the parameters for offset to generate here from origin
	 *
	 * @return right, forward, up
	 */
	public static BlockPos getOffset(Vec3i origin, EnumFacing f, boolean mirror, Vec3i here) {
		int dX = here.getX()-origin.getX();
		int dZ = here.getZ()-origin.getZ();
		int forward = 0;
		int right = 0;
		int up = here.getY() - origin.getY();
		switch (f) {
			case NORTH:
				forward = dZ;
				right = -dX;
				break;
			case SOUTH:
				forward = -dZ;
				right = dX;
				break;
			case WEST:
				right = dZ;
				forward = dX;
				break;
			case EAST:
				right = -dZ;
				forward = -dX;
				break;
		}
		if (mirror) {
			right *= -1;
		}
		return new BlockPos(right, forward, up);
	}

	@Nonnull
	public static AxisAlignedBB apply(@Nonnull Matrix4 mat, @Nonnull AxisAlignedBB in) {
		Vec3d min = new Vec3d(in.minX, in.minY, in.minZ);
		Vec3d max = new Vec3d(in.maxX, in.maxY, in.maxZ);
		min = mat.apply(min);
		max = mat.apply(max);
		return new AxisAlignedBB(min.x, min.y, min.z, max.x, max.y, max.z);
	}

	public static float[] interpolate(double a, float[] cA, double b, float[] cB) {
		float[] ret = new float[cA.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = (float) (a * cA[i] + b * cB[i]);
		}
		return ret;
	}

	// Taken from TEImmersiveConnectable


	public static Set<ImmersiveNetHandler.Connection> genConnBlockstate(Set<ImmersiveNetHandler.Connection> conns, World world) {
		if (conns == null)
			return ImmutableSet.of();
		Set<ImmersiveNetHandler.Connection> ret = new HashSet<ImmersiveNetHandler.Connection>() {
			@Override
			public boolean equals(Object o) {
				if (o == this)
					return true;
				if (!(o instanceof HashSet))
					return false;
				HashSet<ImmersiveNetHandler.Connection> other = (HashSet<ImmersiveNetHandler.Connection>) o;
				if (other.size() != this.size())
					return false;
				for (ImmersiveNetHandler.Connection c : this)
					if (!other.contains(c))
						return false;
				return true;
			}
		};
		for (ImmersiveNetHandler.Connection c : conns) {
			IImmersiveConnectable end = ApiUtils.toIIC(c.end, world, false);
			if (end == null)
				continue;
			// generate subvertices
			c.getSubVertices(world);
			ret.add(c);
		}

		return ret;
	}

	public static void writeConnsToNBT(NBTTagCompound nbt, TileEntity te) {
		World world = te.getWorld();
		if (world != null && !world.isRemote && nbt != null) {
			NBTTagList connectionList = new NBTTagList();
			Set<ImmersiveNetHandler.Connection> conL = ImmersiveNetHandler.INSTANCE.getConnections(world, Utils.toCC(te));
			if (conL != null)
				for (ImmersiveNetHandler.Connection con : conL)
					connectionList.appendTag(con.writeToNBT());
			nbt.setTag("connectionList", connectionList);
		}
	}

	public static void loadConnsFromNBT(NBTTagCompound nbt, TileEntity te) {
		World world = te.getWorld();
		if (world != null && world.isRemote && !Minecraft.getMinecraft().isSingleplayer() && nbt != null) {
			NBTTagList connectionList = nbt.getTagList("connectionList", 10);
			ImmersiveNetHandler.INSTANCE.clearConnectionsOriginatingFrom(Utils.toCC(te), world);
			for (int i = 0; i < connectionList.tagCount(); i++) {
				NBTTagCompound conTag = connectionList.getCompoundTagAt(i);
				ImmersiveNetHandler.Connection con = ImmersiveNetHandler.Connection.readFromNBT(conTag);
				if (con != null) {
					ImmersiveNetHandler.INSTANCE.addConnection(world, Utils.toCC(te), con);
				} else
					IndustrialWires.logger.error("CLIENT read connection as null");
			}
		}
	}

	public static boolean handleUpdate(int id, BlockPos pos, World world) {
		if (id == -1 || id == 255) {
			IBlockState state = world.getBlockState(pos);
			world.notifyBlockUpdate(pos, state, state, 3);
			return true;
		} else if (id == 254) {
			IBlockState state = world.getBlockState(pos);
			if (state instanceof IExtendedBlockState) {
				state = state.getActualState(world, pos);
				state = state.getBlock().getExtendedState(state, world, pos);
				ImmersiveEngineering.proxy.removeStateFromSmartModelCache((IExtendedBlockState) state);
				ImmersiveEngineering.proxy.removeStateFromConnectionModelCache((IExtendedBlockState) state);
			}
			world.notifyBlockUpdate(pos, state, state, 3);
			return true;
		}
		return false;
	}
	//End of code from TEImmersiveConnectable

	@SideOnly(Side.CLIENT)
	public static Vec2f rotate90(Vec2f in) {
		//Yes, when rotating by 90 degrees, x becomes y!
		//noinspection SuspiciousNameCombination
		return new Vec2f(-in.y, in.x);
	}

	@SideOnly(Side.CLIENT)
	public static Vec2f subtract(Vec2f a, Vec2f b) {
		return new Vec2f(a.x-b.x, a.y-b.y);
	}

	@SideOnly(Side.CLIENT)
	public static Vec2f add(Vec2f a, Vec2f b) {
		return new Vec2f(a.x+b.x, a.y+b.y);
	}

	@SideOnly(Side.CLIENT)
	public static Vec2f scale(Vec2f a, float f) {
		return new Vec2f(a.x*f, a.y*f);
	}

	@SideOnly(Side.CLIENT)
	public static Vector3f withNewY(Vec2f in, float y) {
		return new Vector3f(in.x, y, in.y);
	}

	public static int count1Bits(int i) {
		int ret = 0;
		for (int j = 0; j < 32; j++) {
			ret += (i>>>j)&1;
		}
		return ret;
	}
}
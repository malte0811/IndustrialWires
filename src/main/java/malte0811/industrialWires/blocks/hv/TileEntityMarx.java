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

package malte0811.industrialWires.blocks.hv;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsIE;
import blusunrize.immersiveengineering.common.blocks.metal.*;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import ic2.api.item.IC2Items;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.ISyncReceiver;
import malte0811.industrialWires.blocks.IWProperties;
import malte0811.industrialWires.blocks.TileEntityIWMultiblock;
import malte0811.industrialWires.client.render.TileRenderMarx;
import malte0811.industrialWires.network.MessageTileSyncIW;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

public class TileEntityMarx extends TileEntityIWMultiblock implements ITickable, ISyncReceiver {
	// TODO do I want to do this?
	public static final Set<TileEntityMarx> dischargingMarxes = new HashSet<>();

	private static final String TYPE = "type";
	private static final String STAGES = "stages";

	public IWProperties.MarxType type = IWProperties.MarxType.NO_MODEL;
	public int stageCount = 0;
	public FiringState state = FiringState.CHARGING;
	@SideOnly(Side.CLIENT)
	public TileRenderMarx.Discharge dischargeData;

	public TileEntityMarx(EnumFacing facing, IWProperties.MarxType type, boolean mirrored) {
		this.facing = facing;
		this.type = type;
		this.mirrored = mirrored;
	}
	public TileEntityMarx() {}

	@Override
	public void writeNBT(NBTTagCompound out, boolean updatePacket) {
		super.writeNBT(out, updatePacket);
		out.setInteger(TYPE, type.ordinal());
		out.setInteger(STAGES, stageCount);
	}

	@Override
	public void readNBT(NBTTagCompound in, boolean updatePacket) {
		super.readNBT(in, updatePacket);
		type = IWProperties.MarxType.values()[in.getInteger(TYPE)];
		stageCount = in.getInteger(STAGES);
	}

	@Nonnull
	@Override
	protected BlockPos getOrigin() {
		return getPos().subtract(offset).offset(facing.getOpposite(), 3);
	}


	@SuppressWarnings("unchecked")
	@Override
	public IBlockState getOriginalBlock() {
		int forward = dot(offset, facing.getDirectionVec());
		int right = dot(offset, facing.rotateY().getDirectionVec())*(mirrored?-1:1);
		int up = offset.getY();
		if (forward==0) {
			return IEContent.blockMetalDevice0.getDefaultState().withProperty(IEContent.blockMetalDevice0.property, BlockTypes_MetalDevice0.CAPACITOR_HV);
		} else if (forward==-1) {
			return IEContent.blockConnectors.getDefaultState().withProperty(IEContent.blockConnectors.property, BlockTypes_Connector.RELAY_HV)
					.withProperty(IEProperties.FACING_ALL, facing);
		} else if (forward==4&&up==0&&right==1) {
			return IEContent.blockStorage.getDefaultState().withProperty(IEContent.blockStorage.property, BlockTypes_MetalsIE.STEEL);
		} else if (forward>0) {
			//NOP. handled by getOriginalBlockPlacer
		} else if (forward==-2) {
			return IEContent.blockMetalDecoration0.getDefaultState().withProperty(IEContent.blockMetalDecoration0.property, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING);
		} else if (right==0) {
			return IEContent.blockConnectors.getDefaultState().withProperty(IEContent.blockConnectors.property, BlockTypes_Connector.CONNECTOR_REDSTONE)
					.withProperty(IEProperties.FACING_ALL, facing);
		} else {
			return IEContent.blockConnectors.getDefaultState().withProperty(IEContent.blockConnectors.property, BlockTypes_Connector.CONNECTOR_HV)
					.withProperty(IEProperties.FACING_ALL, facing);
		}
		return null;
	}

	@Override
	public BiConsumer<World, BlockPos> getOriginalBlockPlacer() {
		IBlockState original = getOriginalBlock();
		if (original!=null) {
			if (original.getBlock()==IEContent.blockConnectors) {
				return (w, p)->{
					w.setBlockState(p, original);
					TileEntity te = w.getTileEntity(p);
					if (te instanceof TileEntityConnectorLV) {
						((TileEntityConnectorLV) te).facing = original.getValue(IEProperties.FACING_ALL);
						te.markDirty();
					} else if (te instanceof TileEntityConnectorRedstone) {
						((TileEntityConnectorRedstone) te).facing = original.getValue(IEProperties.FACING_ALL);
						te.markDirty();
					}
				};
			} else {
				return (w, p)->w.setBlockState(p, original);
			}
		} else {
			ItemStack hv = IC2Items.getItem("cable", "type:iron,insulation:0");
			return (w, p)->{
				w.setBlockToAir(p);
				EntityItem item = new EntityItem(w, p.getX(), p.getY(), p.getZ(), hv.copy());
				w.spawnEntity(item);
			};
		}
	}

	private int dot(Vec3i a, Vec3i b) {
		return a.getX()*b.getX()+a.getY()*b.getY()+a.getZ()*b.getZ();
	}

	@Override
	public void update() {
		if (state==FiringState.FIRE) {
			state = FiringState.CHARGING;
			if (world.isRemote) {
				dischargingMarxes.remove(this);
			}
		} else if (state==FiringState.NEXT_TICK) {
			state = FiringState.FIRE;
			if (!world.isRemote) {
				//TODO server-side effects of Marx discharges (damage (electric+sound), block processing, reset cap voltages, more?
			} else {
				dischargingMarxes.add(this);//TODO deal with breaking during discharges
			}
		}
		if (!world.isRemote&&type== IWProperties.MarxType.BOTTOM) {
			if (world.getTotalWorldTime()%40==0) {
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setFloat("energy", stageCount*9*9);
				IndustrialWires.packetHandler.sendToAll(new MessageTileSyncIW(this, nbt));
			}
		}
	}

	@Override
	public Vec3i getSize() {
		return new Vec3i(stageCount, 8, 2);
	}

	@Override
	public void onSync(NBTTagCompound nbt) {
		state = FiringState.NEXT_TICK;
		if (true||dischargeData==null) {
			dischargeData = new TileRenderMarx.Discharge();
			int count = 1;
			while (count<stageCount) {
				count <<= 1;
			}
			dischargeData.vertices = new Vector3f[2*count];
			for (int i = 0;i<dischargeData.vertices.length;i++) {
				dischargeData.vertices[i] = new Vector3f();
			}
			dischargeData.vertices[dischargeData.vertices.length-1] = new Vector3f(0, stageCount-1.9375F, 0);
		}
		dischargeData.energy = nbt.getFloat("energy");
		genMarxPoint(0, dischargeData.vertices.length-1);
	}
	// Meant to be const
	private final Vector3f up = new Vector3f(0, 1, 0);
	private final Vector3f side = new Vector3f(0, 0, 1);
	//used for calculation buffering
	private final Vector3f diff = new Vector3f();
	private final Vector3f center = new Vector3f();
	private final Vector3f v0 = new Vector3f();
	private final Matrix4 transform = new Matrix4();
	/**
	 * @param min The first point of the discharge section to be generated. has to be pre-populated
	 * @param max The last point of the discharge section to be generated. has to be pre-populated
	 */
	private void genMarxPoint(int min, int max) {
		int toGenerate = (min+max)/2;
		Vector3f.sub(dischargeData.vertices[max], dischargeData.vertices[min], diff);
		Vector3f.cross(diff, side, v0);
		transform.setIdentity();
		double noise = Math.sqrt(stageCount)*world.rand.nextDouble()*1/(1+Math.abs(stageCount/2.0-toGenerate))*.5;
		if ((max-min)%2==1) {
			noise *= (toGenerate-min)/(double)(max-min);
		}
		v0.scale((float) (noise/v0.length()));
		diff.scale(1/diff.length());
		transform.rotate(Math.PI*2*world.rand.nextDouble(), diff.x, diff.y, diff.z);
		Vector3f.add(dischargeData.vertices[max], dischargeData.vertices[min], center);
		center.scale(.5F);
		dischargeData.vertices[toGenerate] = transform.apply(v0);
		Vector3f.add(dischargeData.vertices[toGenerate], center, dischargeData.vertices[toGenerate]);

		if (toGenerate-min>1) {
			genMarxPoint(min, toGenerate);
		}
		if (max-toGenerate>1) {
			genMarxPoint(toGenerate, max);
		}
	}

	public enum FiringState {
		CHARGING,
		NEXT_TICK,
		FIRE;
	}
}

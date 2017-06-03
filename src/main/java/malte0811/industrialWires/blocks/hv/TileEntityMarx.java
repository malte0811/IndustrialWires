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
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.api.energy.wires.redstone.IRedstoneConnector;
import blusunrize.immersiveengineering.api.energy.wires.redstone.RedstoneWireNetwork;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsIE;
import blusunrize.immersiveengineering.common.blocks.metal.*;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import ic2.api.item.IC2Items;
import malte0811.industrialWires.IIC2Connector;
import malte0811.industrialWires.blocks.IBlockBoundsIW;
import malte0811.industrialWires.blocks.ISyncReceiver;
import malte0811.industrialWires.blocks.IWProperties;
import malte0811.industrialWires.blocks.TileEntityIWMultiblock;
import malte0811.industrialWires.client.render.TileRenderMarx;
import malte0811.industrialWires.util.DualEnergyStorage;
import malte0811.industrialWires.util.MiscUtils;
import malte0811.industrialWires.wires.IC2Wiretype;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

public class TileEntityMarx extends TileEntityIWMultiblock implements ITickable, ISyncReceiver, IBlockBoundsIW, IImmersiveConnectable, IIC2Connector,
		IRedstoneConnector{
	// TODO do I want to do this?
	public static final Set<TileEntityMarx> dischargingMarxes = new HashSet<>();

	private static final String TYPE = "type";
	private static final String STAGES = "stages";
	private static final String HAS_CONN = "hasConn";

	public IWProperties.MarxType type = IWProperties.MarxType.NO_MODEL;
	public int stageCount = 0;
	public FiringState state = FiringState.CHARGING;
	@SideOnly(Side.CLIENT)
	public TileRenderMarx.Discharge dischargeData;
	private DualEnergyStorage storage = new DualEnergyStorage(10000, 1000);
	private boolean hasConnection;

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
		out.setBoolean(HAS_CONN, hasConnection);
		storage.writeToNbt(out, ENERGY_TAG);
	}

	@Override
	public void readNBT(NBTTagCompound in, boolean updatePacket) {
		super.readNBT(in, updatePacket);
		type = IWProperties.MarxType.values()[in.getInteger(TYPE)];
		stageCount = in.getInteger(STAGES);
		storage = DualEnergyStorage.readFromNBT(in.getCompoundTag(ENERGY_TAG));
		hasConnection = in.getBoolean(HAS_CONN);
		boundingAabb = null;
		renderAabb = null;
	}

	@Nonnull
	@Override
	protected BlockPos getOrigin() {
		return getPos().subtract(offset).offset(facing.getOpposite(), 3);
	}


	@SuppressWarnings("unchecked")
	@Override
	public IBlockState getOriginalBlock() {
		int forward = getForward();
		int right = getRight();
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
			//	IndustrialWires.packetHandler.sendToAll(new MessageTileSyncIW(this, nbt));
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
		if (dischargeData==null) {
			dischargeData = new TileRenderMarx.Discharge(stageCount);
		}
		dischargeData.energy = nbt.getFloat("energy");
		dischargeData.diameter = dischargeData.energy/(stageCount*15*15);
		dischargeData.genMarxPoint(0, dischargeData.vertices.length-1);
	}

	private AxisAlignedBB renderAabb = null;
	@Nonnull
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if (renderAabb ==null) {
			if (type== IWProperties.MarxType.BOTTOM) {
				renderAabb = new AxisAlignedBB(pos,
						MiscUtils.offset(pos, facing, mirrored, 2, 4, stageCount));
			} else {
				renderAabb = new AxisAlignedBB(pos, pos);
			}
		}
		return renderAabb;
	}
	private AxisAlignedBB boundingAabb = null;
	@Override
	public AxisAlignedBB getBoundingBox() {
		if (boundingAabb==null||true) {
			int forward = getForward();
			int right = getRight();
			int up = offset.getY();
			AxisAlignedBB ret = Block.FULL_BLOCK_AABB;
			switch (forward) {
			case -3://IO
				if (right == 1) {
					ret = new AxisAlignedBB(5 / 16D, 5 / 16D, .25, 11 / 16D, 11 / 16D, 1);
				} else {
					ret = new AxisAlignedBB(5 / 16D, 5 / 16D, 7 / 16D, 11 / 16D, 11 / 16D, 1);
				}
				break;
			case -1://charging resistors
				if (up == 0) {
					ret = new AxisAlignedBB(.375, 0, 0, .625, 1, 1);
				} else if (up == stageCount - 1) {
					ret = new AxisAlignedBB(.375, 0, 9 / 16D, .625, 5 / 16D, 1);
				} else {
					ret = new AxisAlignedBB(.375, 0, 9 / 16D, .625, 1, 1);
				}
				break;
			case 1://spark gaps
				if (right == 0) {
					if (up!=0) {
						ret = new AxisAlignedBB(0, 0, 0, 9 / 16D, up == stageCount - 1 ? .5 : 1, 7 / 16D);
					} else {
						ret = new AxisAlignedBB(7/16D, 0, 0, 9/16D, 5/16D, 1);
					}
				} else {
					if (stageCount - 1 == up) {
						ret = new AxisAlignedBB(7 / 16D, 3 / 16D, 0, 9 / 16D, 5 / 16D, 1);
					} else {
						ret = new AxisAlignedBB(7 / 16D, 0, 0, 1, 1, 7 / 16D);
					}
				}
				break;
			case -2://Controller
				break;
			case 0://Caps
				if (up == stageCount - 1) {
					ret = new AxisAlignedBB(0, 0, 0, 1, .5, 1);
				}
				break;
			default:
				if (right == 0) {
					if (forward<4) {
						ret = new AxisAlignedBB(7/16D, 0, 0, 9/16D, 5/16D, 1);
					} else {
						ret = new AxisAlignedBB(0, 0, 0, 9/16D, 5/16D, 9/16D);
					}
				} else {
					if (up==0) {
						ret = Block.FULL_BLOCK_AABB;
					} else if (forward < 4) {
						ret = new AxisAlignedBB(7 / 16D, 3 / 16D, 0, 9 / 16D, 5 / 16D, 1);
					} else {
						ret = new AxisAlignedBB(6 / 16D, 1 / 16D, 0, 10 / 16D, 5 / 16D, 10 / 16D);
					}
				}
			}
			boundingAabb = MiscUtils.apply(getBaseTransform(), ret);
		}
		return boundingAabb;
	}

	private Matrix4 getBaseTransform() {
		Matrix4 transform = new Matrix4();
		transform.translate(.5, 0, .5);
		transform.rotate(facing.getHorizontalAngle() * Math.PI / 180, 0, 1, 0);
		if (mirrored) {
			transform.scale(-1, 1, 1);
		}
		transform.translate(-.5, 0, -.5);
		return transform;
	}
	//WIRE STUFF
	@Override
	public boolean canConnect() {
		return getForward()==-3;
	}

	@Override
	public boolean isEnergyOutput() {
		return getForward()==-3&&getRight()==1;
	}

	@Override
	public int outputEnergy(int amount, boolean simulate, int energyType) {
		TileEntityMarx master = master(this);
		if (master!=null) {
			return (int) master.storage.insertIF(amount, !simulate);
		} else {
			return 0;
		}
	}

	@Override
	public double insertEnergy(double eu, boolean simulate) {
		TileEntityMarx master = master(this);
		if (master!=null) {
			return eu-master.storage.insertEU(eu, !simulate);
		} else {
			return 0;
		}
	}

	@Override
	public BlockPos getConnectionMaster(@Nullable WireType cableType, TargetingInfo target) {
		return pos;
	}

	@Override
	public boolean canConnectCable(WireType cableType, TargetingInfo target) {
		if (hasConnection) {
			return false;
		}
		if (getRight()==0) {
			return cableType==WireType.REDSTONE;
		} else {
			return cableType==WireType.STEEL||cableType== IC2Wiretype.IC2_TYPES[3];
		}
	}

	@Override
	public void connectCable(WireType cableType, TargetingInfo target, IImmersiveConnectable other) {
		hasConnection = true;
	}

	@Override
	public WireType getCableLimiter(TargetingInfo target) {
		return getRight()==0?WireType.REDSTONE:IC2Wiretype.IC2_TYPES[3];
	}

	@Override
	public boolean allowEnergyToPass(ImmersiveNetHandler.Connection con) {
		return false;
	}

	@Override
	public void onEnergyPassthrough(int amount) {

	}

	@Override
	public void removeCable(ImmersiveNetHandler.Connection connection) {
		hasConnection = false;
	}

	@Override
	public Vec3d getRaytraceOffset(IImmersiveConnectable link) {
		Matrix4 transf = getBaseTransform();
		if (getRight()==0) {
			return transf.apply(new Vec3d(.5, .5, 7/16D));
		} else {
			return transf.apply(new Vec3d(.5, .5, 4/16D));
		}
	}

	@Override
	public Vec3d getConnectionOffset(ImmersiveNetHandler.Connection con) {
		return getRaytraceOffset(null);
	}

	private RedstoneWireNetwork net = new RedstoneWireNetwork().add(this);
	@Override
	public void setNetwork(RedstoneWireNetwork net) {
		this.net = net;
	}

	@Override
	public RedstoneWireNetwork getNetwork() {
		return net;
	}

	@Override
	public void onChange() {
		//TODO
	}

	@Override
	public World getConnectorWorld() {
		return world;
	}

	@Override
	public void updateInput(byte[] signals) {
		//TODO
	}


	public enum FiringState {
		CHARGING,
		NEXT_TICK,
		FIRE;
	}
}

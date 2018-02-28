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

package malte0811.industrialWires.entities;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

import static malte0811.industrialWires.util.NBTKeys.TEXTURE;

public class EntityBrokenPart extends EntityArrow {
	public static final DataSerializer<ResourceLocation> RES_LOC_SERIALIZER = new DataSerializer<ResourceLocation>() {
		@Override
		public void write(@Nonnull PacketBuffer buf, @Nonnull ResourceLocation value) {
			buf.writeString(value.getResourceDomain());
			buf.writeString(value.getResourcePath());
		}

		@Nonnull
		@Override
		public ResourceLocation read(@Nonnull PacketBuffer buf) throws IOException {
			String domain = buf.readString(128);
			return new ResourceLocation(domain, buf.readString(1024));
		}

		@Nonnull
		@Override
		public DataParameter<ResourceLocation> createKey(int id) {
			return new DataParameter<>(id, this);
		}

		@Nonnull
		@Override
		public ResourceLocation copyValue(@Nonnull ResourceLocation value) {
			return new ResourceLocation(value.getResourceDomain(), value.getResourcePath());
		}
	};
	private static DataParameter<ResourceLocation> MARKER_TEXTURE;
	static {
		DataSerializers.registerSerializer(RES_LOC_SERIALIZER);
		MARKER_TEXTURE = EntityDataManager.createKey(EntityBrokenPart.class, RES_LOC_SERIALIZER);
	}

	private static final double HARDNESS_MAX = 15;
	private static final double DESPAWN_DELAY = 400;

	public ResourceLocation texture = new ResourceLocation("blocks/stone");
	private int timeUnmoved = 0;

	public EntityBrokenPart(World worldIn) {
		super(worldIn);
		setSize(.5F, .5F);
	}

	public EntityBrokenPart(World worldIn, ResourceLocation texture) {
		this(worldIn);
		this.texture = texture;
	}

	@Override
	public void onUpdate() {
		if (firstUpdate &&!world.isRemote && texture != null) {
			dataManager.set(MARKER_TEXTURE, texture);
		}
		onEntityUpdate();


		//movement logic, modified to simulate higher speed than MC is happy about
		{
			this.motionY -= 0.02;
			this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
			this.motionX *= 0.98;
			this.motionY *= 0.98;
			this.motionZ *= 0.98;

			if (this.onGround) {
				this.motionX *= .7;
				this.motionZ *= .7;
				this.motionY *= -0.5D;
			}
		}
		if (world.isRemote) {
			texture = dataManager.get(MARKER_TEXTURE);
			return;
		}

		double speedSq = motionX * motionX + motionY * motionY + motionZ * motionZ;
		breakBlocks(speedSq);
		if (speedSq>.25) {
			List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, getEntityBoundingBox());
			for (EntityLivingBase e:entities) {
				e.attackEntityFrom(DamageSource.FALLING_BLOCK, 7);
			}
		}
		if (speedSq<1e-3) {
			timeUnmoved++;
			if (timeUnmoved>DESPAWN_DELAY) {
				setDead();
			}
		}
	}

	public void breakBlocks(double speedSq) {
		AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
		axisalignedbb = axisalignedbb.grow(motionX, motionY, motionZ);
		BlockPos.PooledMutableBlockPos min = BlockPos.PooledMutableBlockPos.retain(axisalignedbb.minX - .1,
				axisalignedbb.minY - .1, axisalignedbb.minZ - .1);
		BlockPos.PooledMutableBlockPos max = BlockPos.PooledMutableBlockPos.retain(axisalignedbb.maxX + .1,
				axisalignedbb.maxY + 0.1D, axisalignedbb.maxZ + .1);
		BlockPos.PooledMutableBlockPos iter = BlockPos.PooledMutableBlockPos.retain();
		double speed = -1;
		if (this.world.isAreaLoaded(min, max)) {
			for (int x = min.getX(); x <= max.getX(); ++x) {
				for (int y = min.getY(); y <= max.getY(); ++y) {
					for (int z = min.getZ(); z <= max.getZ(); ++z) {
						iter.setPos(x, y, z);
						if (!world.isAirBlock(iter)) {
							IBlockState state = world.getBlockState(iter);
							float hardness = state.getBlockHardness(world, iter);
							if (speed < 0) {
								speed = Math.sqrt(speedSq);
							}
							if (hardness>0&&hardness < HARDNESS_MAX*speed) {
								world.setBlockToAir(iter);
								double factor = (HARDNESS_MAX*speed - hardness) / (HARDNESS_MAX*speed);
								motionX *= factor;
								motionY *= factor;
								motionZ *= factor;
								speed *= factor;
							} else {
								motionX = 0;
								motionY = 0;
								motionZ = 0;
							}
						}
					}
				}
			}
		}

		min.release();
		max.release();
		iter.release();
	}

	@Override
	protected void entityInit() {
		dataManager.register(MARKER_TEXTURE, new ResourceLocation("blocks/stone"));
	}

	@Override
	public void readEntityFromNBT(@Nonnull NBTTagCompound compound) {
		texture = new ResourceLocation(compound.getString(TEXTURE));
	}


	@Override
	public void writeEntityToNBT(@Nonnull NBTTagCompound compound) {
		compound.setString(TEXTURE, texture.toString());
	}

	@Nullable
	@Override
	public AxisAlignedBB getCollisionBoundingBox() {
		return getEntityBoundingBox();
	}

	@Nonnull
	@Override
	protected ItemStack getArrowStack() {
		return ItemStack.EMPTY;
	}
}

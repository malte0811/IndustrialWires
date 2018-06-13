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
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

import static malte0811.industrialWires.util.NBTKeys.TEXTURE;

public class EntityBrokenPart extends Entity {
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
	public static DataParameter<ResourceLocation> MARKER_TEXTURE;


	private static final double HARDNESS_MAX = 15;
	private static final double DESPAWN_DELAY_GROUND = 400;
	private static final double DESPAWN_DELAY_AIR = 60*20;

	private static final ResourceLocation DEFAULT_TEXTURE = new ResourceLocation("blocks/stone");
	public ResourceLocation texture = DEFAULT_TEXTURE;
	private int timeUnmoved = 0;

	public EntityBrokenPart(World worldIn) {
		super(worldIn);
		setSize(.5F, .5F);
		preventEntitySpawning = true;
	}

	public EntityBrokenPart(World worldIn, ResourceLocation texture) {
		this(worldIn);
		this.texture = texture;
	}

	@Override
	public void onEntityUpdate() {
		if (firstUpdate && !world.isRemote && texture != null) {
			dataManager.set(MARKER_TEXTURE, texture);
		}
		super.onEntityUpdate();
		if (world.isRemote && DEFAULT_TEXTURE.equals(texture)) {
			texture = dataManager.get(MARKER_TEXTURE);
		}
	}

	//Taken from EntityIEProjectile and modified afterwards
	private int ticksOnGround;
	private int ticksInAir;
	@Override
	public void onUpdate() {
		this.onEntityUpdate();

		if (this.onGround) {
			++this.ticksOnGround;
			if (this.ticksOnGround >= DESPAWN_DELAY_GROUND) {
				this.setDead();
			}
		} else {
			++this.ticksInAir;

			if (ticksInAir >= DESPAWN_DELAY_AIR) {
				this.setDead();
				return;
			}

			Vec3d currentPos = new Vec3d(this.posX, this.posY, this.posZ);
			Vec3d nextPos = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
			RayTraceResult mop = this.world.rayTraceBlocks(currentPos, nextPos, false, true, false);

			currentPos = new Vec3d(this.posX, this.posY, this.posZ);

			if (mop != null)
				nextPos = new Vec3d(mop.hitVec.x, mop.hitVec.y, mop.hitVec.z);
			else
				nextPos = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

			if (mop == null || mop.entityHit == null) {
				Entity entity = null;
				List<Entity> list = this.world.getEntitiesInAABBexcluding(this,
						this.getEntityBoundingBox().expand(this.motionX, this.motionY, this.motionZ).grow(1), e -> true);
				double d0 = 0.0D;
				for (Entity e : list) {
					if (e.canBeCollidedWith()) {
						float f = 0.3F;
						AxisAlignedBB axisalignedbb = e.getEntityBoundingBox().grow((double) f, (double) f, (double) f);
						RayTraceResult movingobjectposition1 = axisalignedbb.calculateIntercept(currentPos, nextPos);

						if (movingobjectposition1 != null) {
							double d1 = currentPos.distanceTo(movingobjectposition1.hitVec);
							if (d1 < d0 || d0 == 0.0D) {
								entity = e;
								d0 = d1;
							}
						}
					}
				}
				if (entity != null)
					mop = new RayTraceResult(entity);
			}

			if (mop != null) {
				if (mop.entityHit != null) {
					this.attackEntity(mop.entityHit);
				}
			}

			float motion = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
			this.rotationYaw = (float) (Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);

			this.rotationPitch = (float) (Math.atan2(this.motionY, (double) motion) * 180.0D / Math.PI);
			while (this.rotationPitch - this.prevRotationPitch < -180.0F) {
				this.prevRotationPitch -= 360.0F;
			}
			while (this.rotationPitch - this.prevRotationPitch >= 180.0F)
				this.prevRotationPitch += 360.0F;
			while (this.rotationYaw - this.prevRotationYaw < -180.0F)
				this.prevRotationYaw -= 360.0F;
			while (this.rotationYaw - this.prevRotationYaw >= 180.0F)
				this.prevRotationYaw += 360.0F;
			this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
			this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
		}
		float movementDecay = onGround?.5F:.99F;

		if (this.isInWater()) {
			for (int j = 0; j < 4; ++j) {
				float f3 = 0.25F;
				this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX - this.motionX * (double) f3,
						this.posY - this.motionY * (double) f3, this.posZ - this.motionZ * (double) f3, this.motionX, this.motionY, this.motionZ);
			}
			movementDecay *= 0.8F;
		}
		this.motionX *= movementDecay;
		this.motionY *= movementDecay;
		this.motionZ *= movementDecay;
		this.motionY -= .05;
		breakBlocks(getSpeedSq());
		this.doBlockCollisions();
		move(MoverType.SELF, motionX, motionY, motionZ);
	}

	public void breakBlocks(double speedSq) {
		if (world.isRemote)
			return;
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
							if (hardness > 0 && hardness < HARDNESS_MAX * speed) {
								world.setBlockToAir(iter);
								double factor = (HARDNESS_MAX * speed - hardness) / (HARDNESS_MAX * speed);
								motionX *= factor;
								motionY *= factor;
								motionZ *= factor;
								speed *= factor;
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
		dataManager.register(MARKER_TEXTURE, DEFAULT_TEXTURE);
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

	private void attackEntity(Entity e) {
		e.attackEntityFrom(DamageSource.FALLING_BLOCK, 20*getSpeedSq());
	}

	private float getSpeedSq() {
		return (float) (motionX*motionX+motionY*motionY+motionZ*motionZ);
	}

	@Override
	public boolean canBeCollidedWith() {
		return true;
	}

	@Override
	public boolean canBePushed() {
		return false;
	}
}

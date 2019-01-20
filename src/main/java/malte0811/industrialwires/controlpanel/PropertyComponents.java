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

package malte0811.industrialwires.controlpanel;

import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import malte0811.industrialwires.IndustrialWires;
import malte0811.industrialwires.blocks.controlpanel.BlockTypes_Panel;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class PropertyComponents implements IUnlistedProperty<PropertyComponents.PanelRenderProperties> {
	public static PropertyComponents INSTANCE = new PropertyComponents();

	@Override
	public String getName() {
		return "components";
	}

	@Override
	public boolean isValid(PanelRenderProperties value) {
		return value != null;
	}

	@Override
	public Class<PanelRenderProperties> getType() {
		return PanelRenderProperties.class;
	}

	@Override
	public String valueToString(PanelRenderProperties value) {
		return value.toString();
	}

	public static class PanelRenderProperties extends ArrayList<PanelComponent> {
		private EnumFacing facing = EnumFacing.NORTH;
		private float height = .5F;
		private EnumFacing top = EnumFacing.UP;
		// as radians!
		private float angle = 0;
		private Matrix4 topTransform;
		private Matrix4 topTransformInverse;
		private Matrix4 baseTransform;
		private ItemStack textureSource = new ItemStack(IndustrialWires.panel, 1, BlockTypes_Panel.DUMMY.ordinal());


		public PanelRenderProperties() {
			super();
		}

		public PanelRenderProperties(int length) {
			super(length);
		}

		@Override
		public String toString() {
			StringBuilder ret = new StringBuilder("[");
			for (int i = 0; i < size(); i++) {
				ret.append(get(i));
				if (i < size() - 1) {
					ret.append(", ");
				}
			}
			return ret + "]";
		}

		@Nonnull
		public Matrix4 getPanelTopTransform() {
			if (topTransform == null) {
				topTransform = getPanelBaseTransform().copy().translate(0, getHeight(), .5)
						.rotate(angle, 1, 0, 0).translate(0, 0, -.5);
			}
			return topTransform;
		}

		@Nonnull
		public Matrix4 getPanelTopTransformInverse() {
			if (topTransformInverse == null) {
				topTransformInverse = getPanelTopTransform().copy();
				topTransformInverse.invert();
			}
			return topTransformInverse;
		}


		@SideOnly(Side.CLIENT)
		public void transformGLForTop(BlockPos panelPos) {
			double px = panelPos.getX() - TileEntityRendererDispatcher.staticPlayerX;
			double py = panelPos.getY() - TileEntityRendererDispatcher.staticPlayerY;
			double pz = panelPos.getZ() - TileEntityRendererDispatcher.staticPlayerZ;
			GlStateManager.translate(px + .5, py + .5, pz + .5);
			switch (top) {
				case DOWN:
					GlStateManager.rotate(180, 1, 0, 0);
					GlStateManager.rotate(-facing.getHorizontalAngle(), 0, 1, 0);
					break;
				case UP:
					GlStateManager.rotate(180 - facing.getHorizontalAngle(), 0, 1, 0);
					break;
				case NORTH:
				case SOUTH:
				case WEST:
				case EAST:
					GlStateManager.rotate(90, 1, 0, 0);
					GlStateManager.rotate(top.getHorizontalAngle(), 0, 0, 1);
					break;
			}
			GlStateManager.translate(-.5, getHeight() - .5, 0);
			GlStateManager.rotate((float) (angle * 180 / Math.PI), 1, 0, 0);
			GlStateManager.translate(0, 0, -.5);

		}

		@Nonnull
		public Matrix4 getPanelBaseTransform() {
			if (baseTransform == null) {
				baseTransform = new Matrix4();
				baseTransform.translate(.5, .5, .5);
				switch (top) {
					case DOWN:
						baseTransform.rotate(Math.PI, 0, 0, 1);
					case UP:
						baseTransform.rotate(-facing.getHorizontalAngle() * Math.PI / 180 + Math.PI, 0, 1, 0);
						break;
					case NORTH:
					case SOUTH:
					case WEST:
					case EAST:
						baseTransform.rotate(Math.PI / 2, 1, 0, 0);
						baseTransform.rotate(top.getHorizontalAngle() * Math.PI / 180, 0, 0, 1);
						break;
				}
				baseTransform.translate(-.5, -.5, -.5);
			}
			return baseTransform;
		}

		public float getMaxHeight() {
			float max = getPanelMaxHeight();
			for (PanelComponent pc : this) {
				float h = PanelUtils.getHeightWithComponent(pc, angle, getHeight());
				if (h > max) {
					max = h;
				}
			}
			return max;
		}

		public PanelRenderProperties copyOf() {
			PanelRenderProperties ret = new PanelRenderProperties(size());
			for (PanelComponent pc : this) {
				ret.add(pc.copyOf());
			}
			ret.facing = facing;
			ret.top = top;
			ret.angle = angle;
			ret.height = height;
			ret.textureSource = textureSource;
			return ret;
		}

		public float getPanelMaxHeight() {
			return (float) (getHeight() + Math.abs(Math.tan(angle) / 2));
		}

		private void resetMatrixes() {
			baseTransform = null;
			topTransformInverse = null;
			topTransform = null;
		}

		public EnumFacing getFacing() {
			return facing;
		}

		public void setFacing(EnumFacing facing) {
			if (facing != this.facing) {
				this.facing = facing;
				resetMatrixes();
			}
		}

		public void setTextureSource(ItemStack textureSource) {
			if (textureSource.getItem() instanceof ItemBlock)
				this.textureSource = textureSource;
		}

		public ItemStack getTextureSource() {
			return textureSource;
		}

		public float getHeight() {
			return height;
		}

		public void setHeight(float height) {
			if (height != this.height) {
				this.height = height;
				resetMatrixes();
			}
		}

		public EnumFacing getTop() {
			return top;
		}

		public void setTop(EnumFacing top) {
			if (top != this.top) {
				this.top = top;
				resetMatrixes();
			}
		}

		public float getAngle() {
			return angle;
		}

		public void setAngle(float angle) {
			if (angle != this.angle) {
				this.angle = angle;
				resetMatrixes();
			}
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			if (!super.equals(o)) return false;

			PanelRenderProperties that = (PanelRenderProperties) o;

			if (Float.compare(that.height, height) != 0) return false;
			if (Float.compare(that.angle, angle) != 0) return false;
			if (facing != that.facing) return false;
			if (top != that.top) return false;
			return ItemStack.areItemStacksEqual(textureSource, that.textureSource);
		}

		@Override
		public int hashCode() {
			int result = super.hashCode();
			result = 31 * result + facing.hashCode();
			result = 31 * result + (height != +0.0f ? Float.floatToIntBits(height) : 0);
			result = 31 * result + top.hashCode();
			result = 31 * result + (angle != +0.0f ? Float.floatToIntBits(angle) : 0);
			return result;
		}
	}
	public static class AABBPanelProperties extends PanelRenderProperties {
		private AxisAlignedBB aabb;
		private int lastHash;
		public AABBPanelProperties() {
			super(1);
		}

		public AxisAlignedBB getPanelBoundingBox() {
			if (size()<1) {
				aabb = Block.FULL_BLOCK_AABB;
			} else if (aabb!=null||get(0).hashCode()!=lastHash) {
				aabb = getPanelBoundingBox(get(0));
				lastHash = get(0).hashCode();
			}
			return aabb;
		}

		@Override
		public PanelComponent set(int index, PanelComponent pc) {
			AxisAlignedBB aabb = pc.getBlockRelativeAABB();
			pc.setX((float) ((1-aabb.maxX+aabb.minX)/2));
			pc.setY((float) ((1-aabb.maxZ+aabb.minZ)/2));
			return super.set(index, pc);
		}

		@Override
		public boolean add(PanelComponent pc) {
			AxisAlignedBB aabb = pc.getBlockRelativeAABB();
			pc.setX((float) ((1-aabb.maxX+aabb.minX)/2));
			pc.setY((float) ((1-aabb.maxZ+aabb.minZ)/2));
			return super.add(pc);
		}

		private AxisAlignedBB getPanelBoundingBox(PanelComponent element) {
			AxisAlignedBB compAABB = element.getBlockRelativeAABB();
			float height = 6/16F;
			double width = 3*(compAABB.maxX-compAABB.minX);
			double length = 3*(compAABB.maxZ-compAABB.minZ);
			width = MathHelper.clamp(width, 7/16F, 1);
			length = MathHelper.clamp(length, 7/16F, 1);
			double minX = (1-width)/2;
			double minZ = (1-length)/2;
			return new AxisAlignedBB(minX, 0, minZ, minX+width, height, minZ+length);
		}

		@Override
		public float getHeight() {
			return (float) getPanelBoundingBox().maxY;
		}
	}
}

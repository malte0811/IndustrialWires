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

package malte0811.industrialWires.client.panelmodel;

import blusunrize.immersiveengineering.api.IEApi;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import malte0811.industrialWires.blocks.controlpanel.BlockTypes_Panel;
import malte0811.industrialWires.blocks.controlpanel.PropertyComponents;
import malte0811.industrialWires.blocks.controlpanel.PropertyComponents.PanelRenderProperties;
import malte0811.industrialWires.blocks.controlpanel.TileEntityPanel;
import malte0811.industrialWires.controlpanel.PanelUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class PanelModel implements IBakedModel {
	public final static Cache<PanelRenderProperties, AssembledBakedModel> modelCache = CacheBuilder.newBuilder()
			.maximumSize(100)
			.expireAfterAccess(60, TimeUnit.SECONDS)
			.build();
	static {
		IEApi.renderCacheClearers.add(modelCache::invalidateAll);
		IEApi.renderCacheClearers.add(PanelItemOverride.ITEM_MODEL_CACHE::invalidateAll);
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
		if (side!=null) {
			return ImmutableList.of();
		}
		if (state instanceof IExtendedBlockState) {
			PanelRenderProperties cl = ((IExtendedBlockState) state).getValue(PropertyComponents.INSTANCE);
			if (cl == null) {
				return ImmutableList.of();
			}
			AssembledBakedModel m = modelCache.getIfPresent(cl);
			if (m == null) {
				m = new AssembledBakedModel(cl);
				modelCache.put(cl.copyOf(), m);
			}
			return m.getQuads(state, null, rand);
		}
		return ImmutableList.of();
	}

	@Override
	public boolean isAmbientOcclusion() {
		return false;
	}

	@Override
	public boolean isGui3d() {
		return true;
	}

	@Override
	public boolean isBuiltInRenderer() {
		return false;
	}

	@Nonnull
	@Override
	public TextureAtlasSprite getParticleTexture() {
		return PanelUtils.PANEL_TEXTURE;
	}

	@Nonnull
	@Override
	public ItemCameraTransforms getItemCameraTransforms() {
		return ItemCameraTransforms.DEFAULT;
	}

	@Nonnull
	@Override
	public ItemOverrideList getOverrides() {
		return INSTANCE;
	}


	public static class AssembledBakedModel implements IBakedModel {
		PanelRenderProperties components;
		List<BakedQuad> quadsDefault;


		public AssembledBakedModel(PanelRenderProperties comp) {
			components = comp;
		}

		@Nonnull
		@Override
		public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
			if (quadsDefault == null) {
				quadsDefault = PanelUtils.generateQuads(components);
				quadsDefault = Collections.synchronizedList(quadsDefault);
			}
			return quadsDefault;
		}

		@Override
		public boolean isAmbientOcclusion() {
			return false;
		}

		@Override
		public boolean isGui3d() {
			return false;
		}

		@Override
		public boolean isBuiltInRenderer() {
			return false;
		}

		@Nonnull
		@Override
		public TextureAtlasSprite getParticleTexture() {
			return PanelUtils.PANEL_TEXTURE;
		}

		ItemCameraTransforms transform = new ItemCameraTransforms(new ItemTransformVec3f(new Vector3f(45, 0, 0), new Vector3f(0, .2F, 0), new Vector3f(.5F, .5F, .5F)),//3Left
				new ItemTransformVec3f(new Vector3f(45, 0, 0), new Vector3f(0, .2F, 0), new Vector3f(.5F, .5F, .5F)),//3Right
				new ItemTransformVec3f(new Vector3f(), new Vector3f(0, .2F, 0), new Vector3f(.5F, .5F, .5F)),//1Left
				new ItemTransformVec3f(new Vector3f(), new Vector3f(0, .2F, 0), new Vector3f(.5F, .5F, .5F)),//1Right
				new ItemTransformVec3f(new Vector3f(), new Vector3f(), new Vector3f()),//Head?
				new ItemTransformVec3f(new Vector3f(30, 45, 0), new Vector3f(0, .125F, 0), new Vector3f(.6F, .6F, .6F)),//GUI
				new ItemTransformVec3f(new Vector3f(), new Vector3f(0, .1F, 0), new Vector3f(.25F, .25F, .25F)),//Ground
				new ItemTransformVec3f(new Vector3f(0, 180, 45), new Vector3f(0, 0, -.1875F), new Vector3f(.5F, .5F, .5F)));//Fixed;

		@Nonnull
		@Override
		public ItemCameraTransforms getItemCameraTransforms() {
			return transform;
		}

		@Nonnull
		@Override
		public ItemOverrideList getOverrides() {
			return INSTANCE;
		}

	}

	private static final PanelItemOverride INSTANCE = new PanelItemOverride();

	private static class PanelItemOverride extends ItemOverrideList {
		private static Cache<ItemStack, AssembledBakedModel> ITEM_MODEL_CACHE = CacheBuilder.newBuilder()
				.maximumSize(100)
				.expireAfterAccess(60, TimeUnit.SECONDS)
				.build();
		public PanelItemOverride() {
			super(ImmutableList.of());
		}

		@Nonnull
		@Override
		public IBakedModel handleItemState(@Nonnull IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) {
			if (stack != null && stack.getItem() == PanelUtils.PANEL_ITEM && stack.getMetadata() == BlockTypes_Panel.TOP.ordinal()) {
				try {
					return ITEM_MODEL_CACHE.get(stack, ()-> {
						TileEntityPanel te = new TileEntityPanel();
						te.readFromItemNBT(stack.getTagCompound());
						return new AssembledBakedModel(te.getComponents());
					});
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
			return originalModel;
		}
	}
}

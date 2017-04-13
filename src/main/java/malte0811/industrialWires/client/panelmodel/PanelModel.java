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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import malte0811.industrialWires.blocks.controlpanel.PropertyComponents;
import malte0811.industrialWires.blocks.controlpanel.PropertyComponents.PanelRenderProperties;
import malte0811.industrialWires.controlpanel.PanelUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PanelModel implements IBakedModel {
	public final static Cache<PanelRenderProperties, AssembledBakedModel> modelCache = CacheBuilder.newBuilder()
			.maximumSize(100)
			.expireAfterAccess(60, TimeUnit.SECONDS)
			.build();

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
			return m.getQuads(state, side, rand);
		}
		return ImmutableList.of();
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

	@Override
	public TextureAtlasSprite getParticleTexture() {
		return PanelUtils.IRON_BLOCK_TEX;
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms() {
		return ItemCameraTransforms.DEFAULT;
	}

	@Override
	public ItemOverrideList getOverrides() {
		return ItemOverrideList.NONE;
	}

	public class AssembledBakedModel implements IBakedModel {
		PanelRenderProperties components;
		List<BakedQuad> quadsDefault;


		public AssembledBakedModel(PanelRenderProperties comp) {
			components = comp;
		}

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

		@Override
		public TextureAtlasSprite getParticleTexture() {
			return PanelUtils.IRON_BLOCK_TEX;
		}

		@Override
		public ItemCameraTransforms getItemCameraTransforms() {
			return ItemCameraTransforms.DEFAULT;
		}

		@Override
		public ItemOverrideList getOverrides() {
			return ItemOverrideList.NONE;
		}

	}
}

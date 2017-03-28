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
			.build();//TODO make all components implement equals+hashCode

	private IBakedModel base;

	public PanelModel(IBakedModel base) {
		this.base = base;
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
		if (side!=null) {
			return ImmutableList.of();
		}
		if (state instanceof IExtendedBlockState) {
			PanelRenderProperties cl = ((IExtendedBlockState) state).getValue(PropertyComponents.INSTANCE);

			if (cl == null) {
				return base.getQuads(state, side, rand);
			}
			modelCache.invalidateAll();//TODO remove
			AssembledBakedModel m = modelCache.getIfPresent(cl);
			if (m == null) {
				m = new AssembledBakedModel(cl, base, rand);
				modelCache.put(cl.copyOf(), m);
			}
			return m.getQuads(state, side, rand);
		}
		return base.getQuads(state, side, rand);
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
		return base.getParticleTexture();
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
		IBakedModel basic;
		PanelRenderProperties components;
		List<BakedQuad> quads;


		public AssembledBakedModel(PanelRenderProperties comp, IBakedModel b, long posRand) {
			basic = b;
			components = comp;
		}

		@Override
		public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
			if (quads == null) {
				quads = PanelUtils.generateQuads(components);
				quads.addAll(basic.getQuads(state, side, rand));
				quads = Collections.synchronizedList(quads);
			}
			return quads;
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
			return basic.getParticleTexture();
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

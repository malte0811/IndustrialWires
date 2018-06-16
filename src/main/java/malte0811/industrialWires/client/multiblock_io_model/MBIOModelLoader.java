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

package malte0811.industrialWires.client.multiblock_io_model;

import blusunrize.immersiveengineering.api.IEApi;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import malte0811.industrialWires.IndustrialWires;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import static net.minecraftforge.fml.relauncher.Side.CLIENT;

@SideOnly(CLIENT)
public class MBIOModelLoader implements ICustomModelLoader {
	private static final Set<BakedMBIOModel> activeModels = new HashSet<>();
	static {
		IEApi.renderCacheClearers.add(()-> {
			for (BakedMBIOModel m:activeModels) {
				m.clearCache();
			}
		});
	}
	@Override
	public boolean accepts(@Nonnull ResourceLocation modelLocation) {
		return IndustrialWires.MODID.equals(modelLocation.getResourceDomain())
				&& "models/block/mbio".equals(modelLocation.getResourcePath());
	}

	@Nonnull
	@Override
	public IModel loadModel(@Nonnull ResourceLocation modelLocation) throws Exception {
		return new MBIOModel();
	}

	@Override
	public void onResourceManagerReload(@Nonnull IResourceManager resourceManager) {
		activeModels.clear();
		BakedMBIOModel.IO_TEX = null;
	}

	@SideOnly(CLIENT)
	private static class MBIOModel implements IModel {
		private static final Collection<ResourceLocation> TEXTURES = ImmutableList.of(
				BakedMBIOModel.IO_LOC
		);
		private ResourceLocation baseModel = new ResourceLocation(IndustrialWires.MODID, "missing");
		private ImmutableMap<String, String> custom = ImmutableMap.of();
		private int rotationOffset = 0;

		@Nonnull
		@Override
		public Collection<ResourceLocation> getDependencies() {
			return ImmutableList.of(baseModel);
		}

		@Nonnull
		@Override
		public Collection<ResourceLocation> getTextures() {
			return TEXTURES;
		}

		@Nonnull
		@Override
		public IBakedModel bake(@Nonnull IModelState state, @Nonnull VertexFormat format,
								@Nonnull Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
			try {
				IModel baseBaked = ModelLoaderRegistry.getModel(baseModel);
				baseBaked = baseBaked.process(custom);
				IBakedModel baked = baseBaked.bake(state, format, bakedTextureGetter);
				BakedMBIOModel ret = new BakedMBIOModel(baked, rotationOffset);
				activeModels.add(ret);
				return ret;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return ModelLoaderRegistry.getMissingModel().bake(state, format, bakedTextureGetter);
		}

		@Nonnull
		@Override
		public IModel process(ImmutableMap<String, String> customData) {
			MBIOModel ret = new MBIOModel();
			String bm = customData.get("base_model");
			ret.baseModel = new ResourceLocation(bm.substring(1, bm.length()-1));
			String rotOffsetTmp = customData.get("rotation_offset");
			if (rotOffsetTmp!=null) {
				ret.rotationOffset = Integer.parseInt(rotOffsetTmp)&3;
			}
			ret.custom = customData;
			return ret;
		}
	}
}

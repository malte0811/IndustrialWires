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

import blusunrize.immersiveengineering.common.util.IELogger;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import malte0811.industrialWires.IndustrialWires;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.common.model.IModelState;

import java.io.IOException;
import java.util.*;

public class PanelModelLoader implements ICustomModelLoader {
	public static final String RESOURCE_BASE = "models/block/";
	public static final String RESOURCE_LOCATION = "smartmodel/panel";

	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) {
		PanelModel.modelCache.invalidateAll();
		PanelUtils.IRON_BLOCK_TEX = null;
	}

	@Override
	public boolean accepts(ResourceLocation modelLocation) {
		return modelLocation.getResourcePath().contains(RESOURCE_BASE+RESOURCE_LOCATION);
	}

	@Override
	public IModel loadModel(ResourceLocation modelLocation) throws IOException {
		String resourcePath = modelLocation.getResourcePath();
		int pos = resourcePath.indexOf(RESOURCE_LOCATION);
		if (pos >= 0) {
				return new PanelModelBase();
		}
		return ModelLoaderRegistry.getMissingModel();
	}

	private class PanelModelBase implements IModel {

		@Override
		public Collection<ResourceLocation> getDependencies() {
			return ImmutableList.of();
		}

		@Override
		public Collection<ResourceLocation> getTextures() {
			try {
				List<ResourceLocation> ret = new ArrayList<>();
				ret.add(new ResourceLocation("minecraft", "font/ascii"));
				ret.add(new ResourceLocation("minecraft", "blocks/iron_block"));
				return ret;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public IBakedModel bake(IModelState state, VertexFormat format,	Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
			try {
				return new PanelModel();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public IModelState getDefaultState() {
			return null;
		}

	}
}
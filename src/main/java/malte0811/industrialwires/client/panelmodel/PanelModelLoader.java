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

package malte0811.industrialwires.client.panelmodel;

import com.google.common.collect.ImmutableList;
import malte0811.industrialwires.IndustrialWires;
import malte0811.industrialwires.controlpanel.PanelUtils;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class PanelModelLoader implements ICustomModelLoader {
	public static final String RESOURCE_BASE = "models/block/";
	public static final String RESOURCE_LOCATION = "smartmodel/panel";

	@Override
	public void onResourceManagerReload(@Nonnull IResourceManager resourceManager) {
		PanelModel.modelCache.invalidateAll();
		PanelUtils.PANEL_TEXTURE = null;
	}

	@Override
	public boolean accepts(@Nonnull ResourceLocation modelLocation) {
		return modelLocation.getPath().contains(RESOURCE_BASE + RESOURCE_LOCATION);
	}

	@Nonnull
	@Override
	public IModel loadModel(@Nonnull ResourceLocation modelLocation) throws IOException {
		String resourcePath = modelLocation.getPath();
		int pos = resourcePath.indexOf(RESOURCE_LOCATION);
		if (pos >= 0) {
			return new PanelModelBase();
		}
		return ModelLoaderRegistry.getMissingModel();
	}

	private class PanelModelBase implements IModel {

		@Nonnull
		@Override
		public Collection<ResourceLocation> getDependencies() {
			return ImmutableList.of();
		}

		@Nonnull
		@Override
		public Collection<ResourceLocation> getTextures() {
			try {
				List<ResourceLocation> ret = new ArrayList<>();
				ret.add(new ResourceLocation("minecraft", "font/ascii"));
				ret.add(new ResourceLocation(IndustrialWires.MODID, "blocks/control_panel"));
				return ret;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Nonnull
		@Override
		public IBakedModel bake(@Nonnull IModelState state, @Nonnull VertexFormat format, @Nonnull Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
			try {
				return new PanelModel();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

	}
}
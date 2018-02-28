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

package malte0811.industrialWires.compat;

import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.hv.BlockTypes_HVMultiblocks;
import malte0811.industrialWires.hv.MarxOreHandler;
import malte0811.industrialWires.hv.MarxOreHandler.OreInfo;
import mezz.jei.api.*;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@JEIPlugin
public class JEIMarx implements IModPlugin {
	public static IJeiHelpers jeiHelpers;
	private static JEIMarx.MarxCategory marx;


	@Override
	public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {
	}

	@Override
	public void registerIngredients(IModIngredientRegistration registry) {
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry) {
		jeiHelpers = registry.getJeiHelpers();
		marx = new MarxCategory();
		registry.addRecipeCategories(marx);

	}

	@Override
	public void register(IModRegistry registryIn) {
		registryIn.handleRecipes(OreInfo.class, MarxRecipeWrapper::new, IndustrialWires.MODID+".marx");
		registryIn.addRecipes(MarxOreHandler.getRecipes(), IndustrialWires.MODID+".marx");
		registryIn.addRecipeCatalyst(new ItemStack(IndustrialWires.hvMultiblocks, 1,
				BlockTypes_HVMultiblocks.MARX.getMeta()), marx.getUid());
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		Compat.addMarx = (o) -> jeiRuntime.getRecipeRegistry().addRecipe(new MarxRecipeWrapper(o), marx.getUid());
		Compat.removeMarx = (o) -> jeiRuntime.getRecipeRegistry().removeRecipe(new MarxRecipeWrapper(o), marx.getUid());
	}

	private class MarxCategory implements IRecipeCategory<MarxRecipeWrapper> {
		@Nonnull
		@Override
		public String getUid() {
			return IndustrialWires.MODID + ".marx";
		}

		@Nonnull
		@Override
		public String getTitle() {
			return I18n.format(IndustrialWires.MODID + ".desc.jei.marx");
		}

		@Nonnull
		@Override
		public String getModName() {
			return IndustrialWires.MODNAME;
		}

		@Nonnull
		@Override
		public IDrawable getBackground() {
			IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
			return guiHelper.createBlankDrawable(140, 50);
		}

		@Override
		public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull MarxRecipeWrapper recipeWrapper, @Nonnull IIngredients ingredients) {
			IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
			guiItemStacks.init(0, true, 10, 17);
			guiItemStacks.init(1, false, 62, 4);
			guiItemStacks.init(2, false, 62, 29);
			guiItemStacks.set(ingredients);
		}

		@Nullable
		@Override
		public IDrawable getIcon() {
			return null;
		}
	}
	private class MarxRecipeWrapper implements IRecipeWrapper {
		OreInfo recipe;
		public MarxRecipeWrapper(OreInfo recipe) {
			this.recipe = recipe;
		}

		@Override
		public void getIngredients(@Nonnull IIngredients ingredients) {
			ingredients.setInputLists(ItemStack.class, ImmutableList.of(recipe.exampleInput));
			if (recipe.outputSmall!=null) {
				ingredients.setOutputs(ItemStack.class, ImmutableList.of(
						recipe.output.get(),
						recipe.outputSmall.get()));
			} else {
				ingredients.setOutputs(ItemStack.class, ImmutableList.of(recipe.output.get(), ItemStack.EMPTY));//TODO remove second output?
			}
		}

		@Override
		public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
			IDrawable slot = jeiHelpers.getGuiHelper().getSlotDrawable();
			slot.draw(minecraft, 10, 17);
			slot.draw(minecraft, 62, 4);
			if (recipe.outputSmall!=null&&!recipe.outputSmall.get().isEmpty()) {
				slot.draw(minecraft, 62, 29);
				minecraft.fontRenderer.drawString("x"+ recipe.smallMax+I18n.format(IndustrialWires.MODID+".desc.jei.alt"), 85, 33, 0xff000000);
			}
			minecraft.fontRenderer.drawString("x"+ Utils.formatDouble(recipe.maxYield, "0.#") + I18n.format(IndustrialWires.MODID+".desc.jei.max"), 85, 8, 0xff000000);
			minecraft.fontRenderer.drawString("~", 0, 3, 0xff000000);
			minecraft.fontRenderer.drawString((int) (recipe.avgEnergy*MarxOreHandler.defaultEnergy/1000)+" kJ",
					minecraft.fontRenderer.getCharWidth('~'), 0, 0xff000000);
		}
	}
}
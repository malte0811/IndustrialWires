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

package malte0811.industrialWires.util;

import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import malte0811.industrialWires.compat.Compat;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.*;

//This is just for manual entries
public class MultiblockTemplateManual implements MultiblockHandler.IMultiblock {
	public static final TemplateManager RES_LOC_TEMPLATE_MANAGER = new TemplateManager("/dev/null/should not exist",
			DataFixesManager.createFixer());

	private final ResourceLocation loc;
	@Nullable
	private Template template;
	@Nullable
	private IngredientStack[] mats = null;
	@Nullable
	private ItemStack[][][] fakeStructure = null;
	@Nullable
	private Map<ItemStack, IBlockState> realStructure = null;

	public MultiblockTemplateManual(ResourceLocation loc) {
		this.loc = loc;
	}

	private void updateTemplate() {
		if (template == null)
		{
			template = RES_LOC_TEMPLATE_MANAGER.getTemplate(null, loc);
			Vec3i size = template.getSize();
			fakeStructure = new ItemStack[size.getY()][size.getX()][size.getZ()];
			for (int x = 0; x < size.getX(); x++) {
				for (int y = 0; y < size.getY(); y++) {
					for (int z = 0; z < size.getZ(); z++) {
						fakeStructure[y][x][z] = new ItemStack(Items.COMMAND_BLOCK_MINECART);
					}
				}
			}
			realStructure = new IdentityHashMap<>();
			List<Template.BlockInfo> blocks = template.blocks;
			Set<ItemStack> matsSet = new HashSet<>();
			for (Template.BlockInfo info : blocks) {
				ItemStack here = Compat.stackFromInfo.apply(new ItemStack(info.blockState.getBlock(), 1,
						info.blockState.getBlock().getMetaFromState(info.blockState)), info);
				if (!here.isEmpty()) {
					realStructure.put(fakeStructure[info.pos.getY()][info.pos.getX()][info.pos.getZ()],
							info.blockState);
					Optional<ItemStack> match = matsSet.stream().filter(s -> ItemStack.areItemsEqual(here, s)).findAny();
					if (match.isPresent()) {
						match.get().grow(1);
					} else {
						matsSet.add(here);
					}
				}
			}
			mats = matsSet.stream().map(IngredientStack::new).toArray(IngredientStack[]::new);
			for (int x = 0; x < size.getX(); x++) {
				for (int y = 0; y < size.getY(); y++) {
					for (int z = 0; z < size.getZ(); z++) {
						if (!realStructure.containsKey(fakeStructure[y][x][z])) {
							fakeStructure[y][x][z] = ItemStack.EMPTY;
						}
					}
				}
			}
		}
	}

	@Override
	public String getUniqueName() {
		return loc.toString();
	}

	@Override
	public boolean isBlockTrigger(IBlockState state) {
		return false;
	}

	@Override
	public boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player) {
		return false;
	}

	@Override
	public ItemStack[][][] getStructureManual() {
		updateTemplate();
		return fakeStructure;
	}

	@Override
	public IBlockState getBlockstateFromStack(int index, ItemStack stack) {
		updateTemplate();
		assert realStructure != null;
		return realStructure.getOrDefault(stack, Blocks.AIR.getDefaultState());
	}

	@Override
	public IngredientStack[] getTotalMaterials() {
		updateTemplate();
		return mats;
	}

	@Override
	public boolean overwriteBlockRender(ItemStack stack, int iterator) {
		return false;
	}

	@Override
	public float getManualScale() {
		return 12;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderFormedStructure() {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderFormedStructure() {
	}
}
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

package malte0811.industrialWires.blocks.controlpanel;

import blusunrize.immersiveengineering.api.tool.IConfigurableTool;
import malte0811.industrialWires.blocks.ItemBlockIW;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;

import static malte0811.industrialWires.util.NBTKeys.ANGLE;
import static malte0811.industrialWires.util.NBTKeys.HEIGHT;

public class ItemBlockPanel extends ItemBlockIW implements IConfigurableTool {

	public ItemBlockPanel(Block b) {
		super(b);
	}

	@Override
	public boolean canConfigure(ItemStack stack) {
		return stack.getMetadata() == BlockTypes_Panel.UNFINISHED.ordinal();
	}

	@Override
	public ToolConfig.ToolConfigBoolean[] getBooleanOptions(ItemStack stack) {
		return new ToolConfig.ToolConfigBoolean[0];
	}

	@Override
	public ToolConfig.ToolConfigFloat[] getFloatOptions(ItemStack stack) {
		float height;
		float angle;
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) {
			height = .5F;
			angle = 0;
		} else {
			height = nbt.getFloat(HEIGHT);
			angle = nbt.getFloat(ANGLE);
		}
		angle = (float) ((angle * 180 / Math.PI + 45) / 90);
		return new ToolConfig.ToolConfigFloat[]{
				new ToolConfig.ToolConfigFloat(HEIGHT, 60, 20, height),
				new ToolConfig.ToolConfigFloat(ANGLE, 60, 40, angle)
		};
	}

	@Override
	public void applyConfigOption(ItemStack stack, String key, Object value) {
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) {
			nbt = new NBTTagCompound();
			stack.setTagCompound(nbt);
		}
		switch (key) {
			case HEIGHT:
				float angle = nbt.getFloat(ANGLE);
				float halfH = (float) (.5 * Math.tan(angle));
				nbt.setFloat(HEIGHT, MathHelper.clamp((Float) value + .001F, halfH, 1 - halfH));
				break;
			case ANGLE:
				float height = nbt.getFloat(HEIGHT);
				float angleMax = (float) Math.atan(2 * Math.min(height, 1 - height));
				float newAngle = (float) ((Math.PI / 2 * ((Float) value - .5)));
				nbt.setFloat(ANGLE, MathHelper.clamp(newAngle, -angleMax, angleMax));
				break;
		}
	}

	@Override
	public String fomatConfigName(ItemStack stack, ToolConfig config) {
		switch (config.name) {
			case HEIGHT:
				return I18n.format("industrialwires.desc.height");
			case ANGLE:
				return I18n.format("industrialwires.desc.angle");
		}
		return null;
	}

	@Override
	public String fomatConfigDescription(ItemStack stack, ToolConfig config) {
		switch (config.name) {
			case HEIGHT:
				return I18n.format("industrialwires.desc.height_info");
			case ANGLE:
				return I18n.format("industrialwires.desc.angle_info");//TODO talk to blu to make this less awkward
		}
		return null;
	}
}

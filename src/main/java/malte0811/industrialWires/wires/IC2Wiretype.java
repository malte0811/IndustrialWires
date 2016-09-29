/*******************************************************************************
 * This file is part of Industrial Wires.
 * Copyright (C) 2016 malte0811
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
 *******************************************************************************/
package malte0811.industrialWires.wires;

import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.items.ItemIC2Coil;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class IC2Wiretype extends WireType{
	final int type;
	final int[] ic2Rates = {32*8, 128*8, 512*8, 2048*8, 8192*8};
	final int[] ic2Colors = {0xa5bcc7, 0xbc7945, 0xfeff73, 0xb9d6d9, 0xf1f1f1};
	final String[] ic2Names = {"ic2Tin", "ic2Copper", "ic2Gold", "ic2Hv", "ic2Glass"};
	final double[] lossPerBlock = {.2, .2, .4, .8, .025};
	final double[] ic2RenderDiameter = {.03125, .03125, .046875, .0625, .75*.03125};
	public static final IC2Wiretype[] IC2_TYPES = {new IC2Wiretype(0), new IC2Wiretype(1), new IC2Wiretype(2), new IC2Wiretype(3), new IC2Wiretype(4)};
	public IC2Wiretype(int ordinal) {
		super();
		this.type = ordinal;
	}
	/**
	 * In this case, this does not return the loss RATIO but the loss PER BLOCK
	 */
	@Override
	public double getLossRatio() {
		return lossPerBlock[type];
	}
	@Override
	public int getTransferRate() {
		return ic2Rates[type];
	}
	@Override
	public int getColour(Connection connection) {
		return ic2Colors[type];
	}
	@Override
	public double getSlack() {
		return type==2?1.03:1.005;
	}
	@Override
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getIcon(Connection connection) {
		return iconDefaultWire;
	}
	@Override
	public int getMaxLength() {
		return type>=3?32:16;
	}
	@Override
	public ItemStack getWireCoil(ImmersiveNetHandler.Connection con) {
		ItemStack ret = getWireCoil();
		ItemIC2Coil.setLength(ret, con.length);
		return ret;
	}
	@Override
	public ItemStack getWireCoil() {
		return new ItemStack(IndustrialWires.coil,1,type);
	}
	@Override
	public String getUniqueName() {
		return ic2Names[type];
	}
	@Override
	public double getRenderDiameter() {
		return ic2RenderDiameter[type];
	}
	@Override
	public boolean isEnergyWire() {
		return true;
	}
}

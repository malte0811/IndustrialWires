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
package malte0811.industrialWires.wires;

import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.wires.WireApi;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import malte0811.industrialWires.IWConfig;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.items.ItemIC2Coil;
import malte0811.industrialWires.util.ConversionUtil;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class MixedWireType extends WireType {
	public static final String IC2_TIN_CAT = "IC_TIN";
	public static final String IC2_COPPER_CAT = "IC_COPPER";
	public static final String IC2_GOLD_CAT = "IC_GOLD";
	public static final String IC2_HV_CAT = "IC_HV";
	public static final String IC2_GLASS_CAT = "IC_GLASS";
	private final int type;
	private final int[] ratesEU = {32, 128, 512, 2048, 8192};
	private final int[] ic2Colors = {0xa5bcc7, 0xbc7945, 0xfeff73, 0xb9d6d9, 0xf1f1f1};
	private final String[] ic2Names = {"ic2Tin", "ic2Copper", "ic2Gold", "ic2Hv", "ic2Glass",
			"ic2TinIns", "ic2CopperIns", "ic2GoldIns"};
	private final double[] lossPerBlock = {.2, .2, .4, .8, .025};
	private final double[] ic2RenderDiameter = {
			.03125, .03125, .046875, .0625, .75 * .03125, .0625, .0625, 2*.046875
	};

	public static final MixedWireType TIN = new MixedWireType(0);
	public static final MixedWireType COPPER_IC2 = new MixedWireType(1);
	public static final MixedWireType GOLD = new MixedWireType(2);
	public static final MixedWireType HV = new MixedWireType(3);
	public static final MixedWireType GLASS = new MixedWireType(4);
	public static final MixedWireType TIN_INSULATED = new MixedWireType(5);
	public static final MixedWireType COPPER_IC2_INSULATED = new MixedWireType(6);
	public static final MixedWireType GOLD_INSULATED = new MixedWireType(7);
	public static final MixedWireType[] ALL = {
		TIN, COPPER_IC2, GOLD, HV, GLASS, TIN_INSULATED, COPPER_IC2_INSULATED, GOLD_INSULATED
	};

	public MixedWireType(int ordinal) {
		super();
		this.type = ordinal;
		WireApi.registerWireType(this);
	}

	/**
	 * In this case, this does not return the loss RATIO but the loss PER BLOCK
	 */
	@Override
	public double getLossRatio() {
		return lossPerBlock[type%5];
	}

	@Override
	public int getTransferRate() {
		return (int) (getIORate()*getFactor());
	}

	public double getIORate() {
		return ratesEU[type%5] * ConversionUtil.joulesPerEu();
	}

	@Override
	public int getColour(Connection connection) {
		return type<5?ic2Colors[type]:0x2c2c2c;
	}

	@Override
	public double getSlack() {
		return type%5 == 2 ? 1.03 : 1.005;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getIcon(Connection connection) {
		return iconDefaultWire;
	}

	@Override
	public int getMaxLength() {
		return IWConfig.maxLengthPerConn[type%5];
	}

	@Override
	public ItemStack getWireCoil(ImmersiveNetHandler.Connection con) {
		ItemStack ret = getWireCoil();
		ItemIC2Coil.setLength(ret, con.length);
		return ret;
	}

	@Override
	public ItemStack getWireCoil() {
		return new ItemStack(IndustrialWires.coil, 1, type);
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

	@Override
	public boolean canCauseDamage() {
		return type<4;
	}

	@Override
	public double getDamageRadius() {
		if (type>=4) {
			return 0;
		}
		return .3/4*(type+1);
	}

	@Nullable
	@Override
	public String getCategory() {
		switch (type%5) {
			case 0:
				return IC2_TIN_CAT;
			case 1:
				return IC2_COPPER_CAT;
			case 2:
				return IC2_GOLD_CAT;
			case 3:
				return IC2_HV_CAT;
			case 4:
				return IC2_GLASS_CAT;
		}
		return null;
	}

	//Factor between transfer- and input rate
	public int getFactor() {
		return type<5?8:4;
	}

	public boolean isHV() {
		return this==HV||this==GLASS;
	}
}

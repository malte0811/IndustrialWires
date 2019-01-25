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

package malte0811.industrialwires.controlpanel;

import malte0811.industrialwires.IndustrialWires;
import malte0811.industrialwires.client.RawQuad;
import malte0811.industrialwires.client.gui.GuiPanelCreator;
import malte0811.industrialwires.controlpanel.ControlPanelNetwork.RSChannel;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static malte0811.industrialwires.util.NBTKeys.RS_CHANNEL;
import static malte0811.industrialwires.util.NBTKeys.RS_ID;

public class RGBIndicator extends PanelComponent implements IConfigurableComponent {
	@Nonnull
	private RSChannel[] input = {RSChannel.DEFAULT_CHANNEL, RSChannel.DEFAULT_CHANNEL, RSChannel.DEFAULT_CHANNEL};
	private int rgbState;

	public RGBIndicator() {
		super("rgb_led");
	}

	public RGBIndicator(@Nonnull RSChannel[] input) {
		this();
		this.input = input;
	}

	@Override
	protected void writeCustomNBT(NBTTagCompound nbt, boolean toItem) {
		NBTTagList channels = new NBTTagList();
		for (RSChannel c : input) {
			NBTTagCompound ch = new NBTTagCompound();
			ch.setInteger(RS_ID, c.getController());
			ch.setByte(RS_CHANNEL, c.getColor());
			channels.appendTag(ch);
		}
		nbt.setTag("channels", channels);
		if (!toItem) {
			nbt.setInteger("rgb", rgbState);
		}
	}

	@Override
	protected void readCustomNBT(NBTTagCompound nbt) {
		NBTTagList channels = nbt.getTagList("channels", Constants.NBT.TAG_COMPOUND);
		if (channels.tagCount() == 3) {
			RSChannel[] in = new RSChannel[channels.tagCount()];
			for (int i = 0; i < in.length; i++) {
				NBTTagCompound ch = channels.getCompoundTagAt(i);
				int rsController = ch.getInteger(RS_ID);
				byte rsColor = ch.getByte(RS_CHANNEL);
				in[i] = new RSChannel(rsController, rsColor);
			}
			input = in;
		}
		rgbState = nbt.getInteger("rgb");
	}

	private static final float size = .0625F;

	@Override
	@SideOnly(Side.CLIENT)
	public List<RawQuad> getQuads() {
		float[] color = new float[4];
		color[3] = 1;
		for (int i = 0; i < 3; i++) {
			color[i] = ((rgbState >> (i * 8)) & 255) / 255F;
		}
		List<RawQuad> ret = new ArrayList<>(1);
		PanelUtils.addColoredQuad(ret, new Vector3f(), new Vector3f(0, 0, size), new Vector3f(size, 0, size), new Vector3f(size, 0, 0), EnumFacing.UP, color);
		if (rgbState > 0) {
			ret.get(ret.size() - 1).light = 0xff0ff;
		}
		return ret;
	}

	@Nonnull
	@Override
	public PanelComponent copyOf() {
		RGBIndicator ret = new RGBIndicator(Arrays.copyOf(input, input.length));
		ret.rgbState = rgbState;
		ret.setX(x);
		ret.setY(y);
		ret.panelHeight = panelHeight;
		return ret;
	}

	@Nonnull
	@Override
	public AxisAlignedBB getBlockRelativeAABB() {
		if (aabb == null) {
			aabb = new AxisAlignedBB(x, 0, y, x + size, 0, y + size);
		}
		return aabb;
	}

	@Override
	public void interactWith(Vec3d hitRelative, EntityPlayerMP player) {
	}

	@Override
	public void update() {

	}


	@Override
	public void setNetwork(ControlPanelNetwork net) {
		super.setNetwork(net);
		for (int i = 0; i < input.length; i++) {
			int finalI = i;
			net.addListener(this, (state) -> {
				byte currState = (byte) ((rgbState >> (8 * finalI + 4)) & 15);
				if (state.getStrength() != currState) {
					rgbState &= ~(255 << (8 * finalI));
					rgbState |= state.getStrength() << (8 * finalI + 4);
					panel.markDirty();
					panel.triggerRenderUpdate();
				}
			}, input[i]);
		}
	}

	@Override
	public float getHeight() {
		return 0;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		RGBIndicator that = (RGBIndicator) o;

		return rgbState == that.rgbState;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + rgbState;
		return result;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderInGUI(GuiPanelCreator gui) {
		AxisAlignedBB aabb = getBlockRelativeAABB();
		int left = (int) (gui.getX0() + aabb.minX * gui.panelSize);
		int top = (int) (gui.getY0() + aabb.minZ * gui.panelSize);
		int right = (int) (gui.getX0() + aabb.maxX * gui.panelSize);
		int bottom = (int) (gui.getY0() + aabb.maxZ * gui.panelSize);
		int width = right - left;
		int height = bottom - top;
		for (int colorId = 0; colorId < 3; ++colorId) {
			int color = (0xff << (colorId * 8)) | 0xff000000;
			for (int row = 0; row < 3; ++row) {
				int col = (row * (row + 1) + colorId) % 3;
				Gui.drawRect(Math.round(left + width * col / 3.0F),
						Math.round(top + height * row / 3.0F),
						Math.round(left + width * (col + 1) / 3.0F),
						Math.round(top + height * (row + 1) / 3.0F),
						color);
			}
		}
	}

	@Override
	public void applyConfigOption(ConfigType type, int id, NBTBase value) {
		switch (type) {
			case RS_CHANNEL:
				input[id] = input[id].withColor(value);
				break;
			case INT:
				input[id] = input[id].withController(value);
				break;
		}
	}

	@Nullable
	@Override
	@SideOnly(Side.CLIENT)
	public String fomatConfigName(ConfigType type, int id) {
		switch (type) {
			case FLOAT:
				return I18n.format(IndustrialWires.MODID + ".desc." + (id == 0 ? "red" : (id == 1 ? "green" : "blue")));
			case RS_CHANNEL:
			case INT:
			default:
				return null;
		}
	}

	@Nullable
	@Override
	@SideOnly(Side.CLIENT)
	public String fomatConfigDescription(ConfigType type, int id) {
		String color = null;
		switch (id) {
			case 0:
				color = "red";
				break;
			case 1:
				color = "green";
				break;
			case 2:
				color = "blue";
				break;
		}
		switch (type) {
			case FLOAT:
				return null;
			case RS_CHANNEL:
				return I18n.format(IndustrialWires.MODID + ".desc.rschannel_" + color);
			case INT:
				return I18n.format(IndustrialWires.MODID + ".desc.rsid_" + color);
			default:
				return null;
		}
	}

	@Override
	public RSColorConfig[] getRSChannelOptions() {
		return new RSColorConfig[]{
				new RSColorConfig("channelR", 0, 10, input[0].getColor()),
				new RSColorConfig("channelG", 50, 10, input[1].getColor()),
				new RSColorConfig("channelB", 0, 70, input[2].getColor())
		};
	}

	@Override
	public IntConfig[] getIntegerOptions() {
		return new IntConfig[]{
				new IntConfig("rsIdR", 0, 0, input[0].getController(), 2, false),
				new IntConfig("rsIdG", 50, 0, input[1].getController(), 2, false),
				new IntConfig("rsIdB", 0, 60, input[2].getController(), 2, false)
		};
	}

	@Override
	public int getColor() {
		return 0xffffff;
	}
}
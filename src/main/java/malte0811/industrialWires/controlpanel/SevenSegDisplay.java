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

package malte0811.industrialWires.controlpanel;

import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.controlpanel.TileEntityPanel;
import malte0811.industrialWires.client.RawQuad;
import malte0811.industrialWires.client.gui.GuiPanelCreator;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static malte0811.industrialWires.util.MiscUtils.*;
import static malte0811.industrialWires.util.NBTKeys.*;

public class SevenSegDisplay extends PanelComponent implements IConfigurableComponent {
	public static final String NAME = "7seg";

	private static final float sizeX = 1.5F/16;
	private static final float sizeY = 3.0F/16;
	/*
	  0
	1   2
	  3
	4   5
	  6
	 */
	private static final boolean[][] numbers = {
			{true,	true,	true,	false,	true,	true,	true},//0
			{false,	false,	true,	false,	false,	true,	false},//1
			{true,	false,	true,	true,	true,	false,	true},//2
			{true,	false,	true,	true,	false,	true,	true},//3
			{false,	true,	true,	true,	false,	true,	false},//4
			{true,	true,	false,	true,	false,	true,	true},//5
			{true,	true,	false,	true,	true,	true,	true},//6
			{true,	false,	true,	false,	false,	true,	false},//7
			{true,	true,	true,	true,	true,	true,	true},//8
			{true,	true,	true,	true,	false,	true,	true},//9
			{true,	true,	true,	true,	true,	true,	false},//A
			{false,	true,	false,	true,	true,	true,	true},//b
			{true,	true,	false,	false,	true,	false,	true},//C
			{false,	false,	true,	true,	true,	true,	true},//d
			{true,	true,	false,	true,	true,	false,	true},//E
			{true,	true,	false,	true,	true,	false,	false}//F
	};
	/*
	  0      1
	2          3
	  4      5
	 */
	@SideOnly(Side.CLIENT)
	private static Vec2f[][] positions;


	private int color = 0xff00;
	private byte input = 0;
	private int rsInputId;
	private byte rsInputChannel;

	public SevenSegDisplay() {
		super(NAME);
	}

	public SevenSegDisplay(int rsId, byte rsChannel, int color) {
		this();
		this.color = color;
		rsInputChannel = rsChannel;
		rsInputId = rsId;
	}

	@Override
	protected void writeCustomNBT(NBTTagCompound nbt, boolean toItem) {
		nbt.setInteger(RS_ID, rsInputId);
		nbt.setByte(RS_CHANNEL, rsInputChannel);
		nbt.setInteger(COLOR, color);
		if (!toItem) {
			nbt.setInteger("rsInput", input);
		}
	}

	@Override
	protected void readCustomNBT(NBTTagCompound nbt) {
		rsInputId = nbt.getInteger(RS_ID);
		rsInputChannel = nbt.getByte(RS_CHANNEL);
		color = nbt.getInteger(COLOR);
		input = nbt.getByte("rsInput");
	}

	private TileEntityPanel panel;
	private Consumer<byte[]> handler = (inputNew) -> {
		if (inputNew[rsInputChannel] != input) {
			input = inputNew[rsInputChannel];
			panel.markDirty();
			panel.triggerRenderUpdate();
		}
	};

	@Nullable
	@Override
	public Consumer<byte[]> getRSInputHandler(int id, TileEntityPanel panel) {
		if (matchesId(rsInputId, id)) {
			this.panel = panel;
			return handler;
		}
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public List<RawQuad> getQuads() {
		if (positions==null) {
			positions = new Vec2f[7][6];
			Vec2f[][] startAndEnd = {
					{new Vec2f(1F/4*sizeX, 2F/8*sizeY), new Vec2f(3F/4*sizeX, 2F/8*sizeY)},
					{new Vec2f(1F/4*sizeX, 2F/8*sizeY), new Vec2f(1F/4*sizeX, 4F/8*sizeY)},
					{new Vec2f(3F/4*sizeX, 2F/8*sizeY), new Vec2f(3F/4*sizeX, 4F/8*sizeY)},
					{new Vec2f(1F/4*sizeX, 4F/8*sizeY), new Vec2f(3F/4*sizeX, 4F/8*sizeY)},
					{new Vec2f(1F/4*sizeX, 4F/8*sizeY), new Vec2f(1F/4*sizeX, 6F/8*sizeY)},
					{new Vec2f(3F/4*sizeX, 4F/8*sizeY), new Vec2f(3F/4*sizeX, 6F/8*sizeY)},
					{new Vec2f(1F/4*sizeX, 6F/8*sizeY), new Vec2f(3F/4*sizeX, 6F/8*sizeY)}
			};
			for (int i = 0;i<7;i++) {
				positions[i][2] = startAndEnd[i][0];
				positions[i][3] = startAndEnd[i][1];
				Vec2f dir = scale(subtract(startAndEnd[i][0], startAndEnd[i][1]), 1F/8);
				Vec2f perpend = rotate90(dir);
				positions[i][1] = add(add(startAndEnd[i][1], perpend), dir);
				positions[i][5] = add(subtract(startAndEnd[i][1], perpend), dir);
				positions[i][0] = subtract(add(startAndEnd[i][0], perpend), dir);
				positions[i][4] = subtract(subtract(startAndEnd[i][0], perpend), dir);
			}
		}
		List<RawQuad> ret = new ArrayList<>();
		PanelUtils.addColoredQuad(ret, new Vector3f(0, 0, 0), new Vector3f(0, 0, sizeY), new Vector3f(sizeX, 0, sizeY),
				new Vector3f(sizeX, 0, 0), EnumFacing.UP, BLACK);
		float[] colorOn = PanelUtils.getFloatColor(true, this.color);
		float[] colorOff = PanelUtils.getFloatColor(false, this.color);
		for (int i = 0;i<7;i++) {
			float[] colorToUse = numbers[input][i]?colorOn:colorOff;
			Vec2f[] pointsForSegment = positions[i];
			PanelUtils.addColoredQuad(ret,
					withNewY(pointsForSegment[1], Y_DELTA),
					withNewY(pointsForSegment[0], Y_DELTA),
					withNewY(pointsForSegment[2], Y_DELTA),
					withNewY(pointsForSegment[3], Y_DELTA),
					EnumFacing.UP, colorToUse);
			PanelUtils.addColoredQuad(ret,
					withNewY(pointsForSegment[3], Y_DELTA),
					withNewY(pointsForSegment[2], Y_DELTA),
					withNewY(pointsForSegment[4], Y_DELTA),
					withNewY(pointsForSegment[5], Y_DELTA),
					EnumFacing.UP, colorToUse);
		}
		return ret;
	}

	@Nonnull
	@Override
	public PanelComponent copyOf() {
		SevenSegDisplay ret = new SevenSegDisplay(rsInputId, rsInputChannel, color);
		ret.input = input;
		ret.setX(x);
		ret.setY(y);
		ret.panelHeight = panelHeight;
		return ret;
	}
	@Nonnull
	@Override
	public AxisAlignedBB getBlockRelativeAABB() {
		if (aabb == null) {
			aabb = new AxisAlignedBB(x, 0, y, x + sizeX, 0, y + sizeY);
		}
		return aabb;
	}

	@Override
	public void interactWith(Vec3d hitRelative, TileEntityPanel tile, EntityPlayerMP player) {
		//NOP
	}

	@Override
	public void update(TileEntityPanel tile) {
		//NOP
	}

	@Override
	public int getColor() {
		return color;
	}

	@Override
	public float getHeight() {
		return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderInGUI(GuiPanelCreator gui) {
		color |= 0xff000000;
		renderInGUIDefault(gui, 0xff000000);
		AxisAlignedBB aabb = getBlockRelativeAABB();
		int left = (int) (gui.getX0() + (aabb.minX+sizeX/4) * gui.panelSize);
		int top = (int) (gui.getY0() + (aabb.minZ+sizeY/4) * gui.panelSize);
		int sizeX = (int) (SevenSegDisplay.sizeX/2 * gui.panelSize);
		int sizeY = (int) (SevenSegDisplay.sizeY/2 * gui.panelSize);
		Gui.drawRect(left, top, left+sizeX, top+sizeY, color);
		final int width = 1;
		Gui.drawRect(left+width, top+width, left+sizeX-width, top+sizeY/2-width, 0xff000000);
		Gui.drawRect(left+width, top+sizeY/2+width, left+sizeX-width, top+sizeY-width, 0xff000000);
	}


	@Override
	public void applyConfigOption(ConfigType type, int id, NBTBase value) {
		switch (type) {
			case RS_CHANNEL:
				rsInputChannel = ((NBTTagByte) value).getByte();
				break;
			case INT:
				rsInputId = ((NBTTagInt) value).getInt();
				break;
			case FLOAT:
				color = PanelUtils.setColor(color, id, value);
				break;
		}
	}

	@Nullable
	@Override
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
	public String fomatConfigDescription(ConfigType type, int id) {
		switch (type) {
			case FLOAT:
				return null;
			case RS_CHANNEL:
				return I18n.format(IndustrialWires.MODID + ".desc.rschannel_info");
			case INT:
				return I18n.format(IndustrialWires.MODID + ".desc.rsid_info");
			default:
				return null;
		}
	}

	@Override
	public RSChannelConfig[] getRSChannelOptions() {
		return new RSChannelConfig[]{
				new RSChannelConfig("channel", 0, 0, rsInputChannel)
		};
	}

	@Override
	public IntConfig[] getIntegerOptions() {
		return new IntConfig[]{
				new IntConfig("rsId", 0, 45, rsInputId, 2, false)
		};
	}

	@Override
	public FloatConfig[] getFloatOptions() {
		float[] color = PanelUtils.getFloatColor(true, this.color);
		int x = 70;
		int yOffset = 10;
		return new FloatConfig[]{
				new FloatConfig("red", x, yOffset, color[0], 60),
				new FloatConfig("green", x, yOffset + 20, color[1], 60),
				new FloatConfig("blue", x, yOffset + 40, color[2], 60)
		};
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		SevenSegDisplay that = (SevenSegDisplay) o;

		if (color != that.color) return false;
		return input == that.input;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + color;
		result = 31 * result + (int) input;
		return result;
	}
}

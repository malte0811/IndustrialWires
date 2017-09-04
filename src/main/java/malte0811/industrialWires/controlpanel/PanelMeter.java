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

package malte0811.industrialWires.controlpanel;

import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.controlpanel.TileEntityPanel;
import malte0811.industrialWires.client.RawQuad;
import malte0811.industrialWires.client.gui.GuiPanelCreator;
import malte0811.industrialWires.client.panelmodel.RawModelFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class PanelMeter extends PanelComponent implements IConfigurableComponent {
	public static final String WIDE = "wide";

	private int rsInputId, rsInputId2 = -1;
	private byte rsInputChannel, rsInputChannel2;
	private int rsInput;
	private boolean wide = true;
	private boolean hasSecond;

	public PanelMeter() {
		super("panel_meter");
	}

	public PanelMeter(int rsId, byte rsChannel, int rsId2, byte rsChannel2, boolean wide, boolean hasSecond) {
		this();
		rsInputChannel = rsChannel;
		rsInputId = rsId;
		rsInputChannel2 = rsChannel2;
		rsInputId2 = rsId2;
		this.hasSecond = hasSecond;
		this.wide = wide;
	}

	@Override
	protected void writeCustomNBT(NBTTagCompound nbt, boolean toItem) {
		nbt.setInteger(RS_ID, rsInputId);
		nbt.setByte(RS_CHANNEL, rsInputChannel);
		nbt.setBoolean(HAS_SECOND_CHANNEL, hasSecond);
		if (hasSecond) {
			nbt.setInteger(RS_ID2, rsInputId2);
			nbt.setByte(RS_CHANNEL2, rsInputChannel2);
		}
		nbt.setBoolean(WIDE, wide);
		if (!toItem) {
			nbt.setInteger("rsInput", rsInput);
		}
	}

	@Override
	protected void readCustomNBT(NBTTagCompound nbt) {
		rsInputId = nbt.getInteger(RS_ID);
		rsInputChannel = nbt.getByte(RS_CHANNEL);
		rsInput = nbt.getInteger("rsInput");
		wide = nbt.getBoolean(WIDE);
		hasSecond = nbt.getBoolean(HAS_SECOND_CHANNEL);
		if (hasSecond) {
			rsInputId2 = nbt.getInteger(RS_ID2);
			rsInputChannel2 = nbt.getByte(RS_CHANNEL2);
		} else {
			rsInputId2 = -1;
			rsInputChannel2 = -1;
		}
	}

	private static final float SIZE = .25F;
	private static final float WIDTH = 1.5F*SIZE;
	private static final float BORDER = SIZE /20;
	private static final float antiZOffset = .001F;
	private static final float[] BLACK = {0, 0, 0, 1};
	private static final float[] WHITE = {1, 1, 1, 1};
	@Override
	public List<RawQuad> getQuads() {
		List<RawQuad> ret = new ArrayList<>();
		float width = wide?WIDTH:SIZE;
		//main panel
		PanelUtils.addColoredQuad(ret, new Vector3f(), new Vector3f(0, 0, SIZE), new Vector3f(width, 0, SIZE),
				new Vector3f(width, 0, 0), EnumFacing.UP, BLACK);
		PanelUtils.addColoredQuad(ret, new Vector3f(BORDER, antiZOffset, BORDER), new Vector3f(BORDER, antiZOffset, SIZE-BORDER),
				new Vector3f(width-BORDER, antiZOffset, SIZE-BORDER), new Vector3f(width-BORDER, antiZOffset, BORDER), EnumFacing.UP, WHITE);

		RawModelFontRenderer r = fontRenderer();
		r.transform = new Matrix4();
		for (int i = 0;i<=3;i++) {
			transformNumber(r.transform, 5*17*i);
			String asString = Integer.toString(5*i);
			int lengthHalf = r.getStringWidth(asString)/2;
			r.transform.translate(-lengthHalf*r.scale, 0, -3.5*r.scale);
			r.drawString(asString, 0, 0, 0xff000000);
			ret.addAll(r.build());
		}
		r.transform = null;
		Matrix4 mat = new Matrix4();
		transformNeedle(mat, rsInput);
		float wHalf = BORDER/2;
		float length = getLength();
		PanelUtils.addColoredQuad(ret, new Vector3f(wHalf, 0, 0),new Vector3f(-wHalf, 0, 0), new Vector3f(-wHalf, 0, length),
				new Vector3f(wHalf, 0, length), EnumFacing.UP, BLACK, mat);
		return ret;
	}

	private RawModelFontRenderer renderer;
	private RawModelFontRenderer fontRenderer() {
		if (renderer == null) {
			renderer = new RawModelFontRenderer(Minecraft.getMinecraft().gameSettings, Label.FONT, Minecraft.getMinecraft().getTextureManager(),
					false, .5F);
		}
		return renderer;
	}

	private void transformNumber(Matrix4 mat, int value) {
		if (wide) {
			transformNeedle(mat, value);
			mat.translate(0, 0, getLength()+1.5*BORDER);
			mat.scale(-1, 1, -1);
		} else {
			mat.setIdentity().translate(0, antiZOffset, SIZE);
			mat.translate(SIZE-3*BORDER, 0, -3*BORDER);
			float angle = 90*(1-value/255F);
			angle = (float) (angle*Math.PI/180);
			float length = getLength()+BORDER;
			mat.translate((float)(-Math.sin(angle)*length), 0, (float)(-Math.cos(angle)*length));
		}
	}

	private void transformNeedle(Matrix4 mat, int value) {
		mat.setIdentity().translate(0, 2*antiZOffset, SIZE);
		float angle;
		if (wide) {
			mat.translate(WIDTH/2, 0, -2*BORDER);
			angle = 50-(100*(value/255F));
		} else {
			mat.translate(SIZE-3*BORDER, 0, -3*BORDER);
			angle = 90-(90*(value/255F));
		}
		angle = (float) ((180+angle)*Math.PI/180);
		mat.rotate(angle, 0, 1, 0);
	}
	private float getLength() {
		return SIZE-(wide?6:7)*BORDER;
	}

	@Nonnull
	@Override
	public PanelComponent copyOf() {
		PanelMeter ret = new PanelMeter(rsInputId, rsInputChannel, rsInputId2, rsInputChannel2, wide, hasSecond);
		ret.rsInput = rsInput;
		ret.setX(x);
		ret.setY(y);
		ret.panelHeight = panelHeight;
		return ret;
	}

	@Nonnull
	@Override
	public AxisAlignedBB getBlockRelativeAABB() {
		if (aabb == null) {
			aabb = new AxisAlignedBB(x, 0, y, x + (wide?WIDTH:SIZE), 0, y + SIZE);
		}
		return aabb;
	}

	@Override
	public void interactWith(Vec3d hitRelative, TileEntityPanel tile, EntityPlayerMP player) {
	}

	@Override
	public void update(TileEntityPanel tile) {

	}

	private TileEntityPanel panel;
	private Consumer<byte[]> handlerSec = (input) -> {
		if (input[rsInputChannel2] != (rsInput&0xf)) {
			rsInput = (input[rsInputChannel2]&0xf)|(rsInput&0xf0);
			panel.markDirty();
			panel.triggerRenderUpdate();
		}
	};
	private Consumer<byte[]> handler = (input) -> {
		if (input[rsInputChannel] != rsInput>>4) {
			if (hasSecond) {
				rsInput = (input[rsInputChannel]<<4)|(rsInput&0xf);
			} else {
				rsInput = input[rsInputChannel]*17;
			}
			panel.markDirty();
			panel.triggerRenderUpdate();
		}
		if (rsInputId2==rsInputId) {
			handlerSec.accept(input);
		}
	};

	@Nullable
	@Override
	public Consumer<byte[]> getRSInputHandler(int id, TileEntityPanel panel) {
		if (matchesId(rsInputId, id)) {
			this.panel = panel;
			return handler;
		} else if (matchesId(rsInputId2, id)) {
			this.panel = panel;
			return handlerSec;
		}
		return null;
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

		PanelMeter that = (PanelMeter) o;

		if (rsInputId != that.rsInputId) return false;
		if (rsInputId2 != that.rsInputId2) return false;
		if (rsInputChannel != that.rsInputChannel) return false;
		if (rsInputChannel2 != that.rsInputChannel2) return false;
		if (rsInput != that.rsInput) return false;
		if (wide != that.wide) return false;
		return hasSecond == that.hasSecond;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + rsInputId;
		result = 31 * result + rsInputId2;
		result = 31 * result + (int) rsInputChannel;
		result = 31 * result + (int) rsInputChannel2;
		result = 31 * result + rsInput;
		result = 31 * result + (wide ? 1 : 0);
		result = 31 * result + (hasSecond ? 1 : 0);
		return result;
	}

	@Override
	public void renderInGUI(GuiPanelCreator gui) {
		renderInGUIDefault(gui, 0);
		AxisAlignedBB aabb = getBlockRelativeAABB();
		int left = (int) Math.ceil(gui.getX0() + (aabb.minX+BORDER) * gui.panelSize);
		int top = (int) Math.ceil(gui.getY0() + (aabb.minZ+BORDER) * gui.panelSize);
		int right = (int) Math.floor(gui.getX0() + (aabb.maxX-BORDER) * gui.panelSize);
		int bottom = (int) Math.floor(gui.getY0() + (aabb.maxZ-BORDER) * gui.panelSize);
		Gui.drawRect(left, top, right, bottom, ~0);
		GlStateManager.pushMatrix();
		int border = (int) Math.ceil(BORDER*gui.panelSize);
		int width = right-left;
		int height = bottom-top;
		if (wide) {
			GlStateManager.translate(left+width/2D, bottom-2*border, 0);
			GlStateManager.rotate(135, 0, 0, 1);
		} else {
			GlStateManager.translate(right-2*border, bottom-2*border, 0);
			GlStateManager.rotate(120, 0, 0, 1);
		}
		left = (int) Math.floor(-BORDER * gui.panelSize/2);
		top = 0;
		right = (int) Math.ceil(BORDER*gui.panelSize/2);
		bottom = (int) Math.floor(getLength() * gui.panelSize);
		Gui.drawRect(left, top, right, bottom, 0xff000000);
		GlStateManager.popMatrix();
	}

	@Override
	public void applyConfigOption(ConfigType type, int id, NBTBase value) {
		switch (type) {
			case RS_CHANNEL:
				if (id == 0) {
					rsInputChannel = ((NBTTagByte) value).getByte();
				} else {
					rsInputChannel2 = ((NBTTagByte) value).getByte();
				}
				break;
			case INT:
				if (id == 0) {
					rsInputId = ((NBTTagInt) value).getInt();
				} else {
					rsInputId2 = ((NBTTagInt) value).getInt();
					hasSecond = rsInputId2>=0;
				}
				break;
			case BOOL:
				wide = ((NBTTagByte) value).getByte() != 0;
		}
	}

	@Nullable
	@Override
	public String fomatConfigName(ConfigType type, int id) {
		switch (type) {
		case FLOAT:
			return I18n.format(IndustrialWires.MODID + ".desc." + (id == 0 ? "red" : (id == 1 ? "green" : "blue")));
		case BOOL:
			return I18n.format(IndustrialWires.MODID + ".desc.wide_info");
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
			return I18n.format(IndustrialWires.MODID + ".desc.rschannel_info"+(id==1?"2":""));
		case INT:
			return I18n.format(IndustrialWires.MODID + ".desc.rsid_info"+(id==1?"2":""));
		default:
			return null;
		}
	}

	@Override
	public RSChannelConfig[] getRSChannelOptions() {
		return new RSChannelConfig[]{
				new RSChannelConfig("channel", 0, 0, rsInputChannel, false),
				new RSChannelConfig("channel2", 60, 0, rsInputChannel2, false)
		};
	}

	@Override
	public IntConfig[] getIntegerOptions() {
		return new IntConfig[]{
				new IntConfig("rsId", 0, 60, rsInputId, 2, false),
				new IntConfig("rsId2", 60, 60, rsInputId2, 2, true)
		};
	}

	@Override
	public BoolConfig[] getBooleanOptions() {
		return new BoolConfig[]{
			new BoolConfig("wide", 0, 80, wide)
		};
	}

	@Override
	public int getColor() {
		return ~0;
	}
}
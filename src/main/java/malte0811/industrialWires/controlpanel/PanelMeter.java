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

import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.client.RawQuad;
import malte0811.industrialWires.client.gui.GuiPanelCreator;
import malte0811.industrialWires.client.panelmodel.RawModelFontRenderer;
import malte0811.industrialWires.controlpanel.ControlPanelNetwork.RSChannel;
import malte0811.industrialWires.controlpanel.ControlPanelNetwork.RSChannelState;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static malte0811.industrialWires.util.NBTKeys.*;

public class PanelMeter extends PanelComponent implements IConfigurableComponent {
	@Nonnull
	private RSChannel primary = RSChannel.DEFAULT_CHANNEL;
	@Nonnull
	private RSChannel secondary = RSChannel.INVALID_CHANNEL;
	private int rsInput;
	private boolean wide = true;

	public PanelMeter() {
		super("panel_meter");
	}

	public PanelMeter(@Nonnull RSChannel primary, @Nonnull RSChannel secondary, boolean wide) {
		this();
		this.primary = primary;
		this.secondary = secondary;
		this.wide = wide;
	}

	@Override
	protected void writeCustomNBT(NBTTagCompound nbt, boolean toItem) {
		nbt.setInteger(RS_ID, primary.getController());
		nbt.setByte(RS_CHANNEL, primary.getColor());
		nbt.setInteger(RS_ID2, secondary.getController());
		nbt.setByte(RS_CHANNEL2, secondary.getColor());
		nbt.setBoolean(WIDE, wide);
		if (!toItem) {
			nbt.setInteger("rsInput", rsInput);
		}
	}

	@Override
	protected void readCustomNBT(NBTTagCompound nbt) {
		int rsController = nbt.getInteger(RS_ID);
		byte rsColor = nbt.getByte(RS_CHANNEL);
		primary = new RSChannel(rsController, rsColor);
		rsInput = nbt.getInteger("rsInput");
		wide = nbt.getBoolean(WIDE);
		if (nbt.hasKey(RS_ID2)) {
			rsController = nbt.getInteger(RS_ID2);
			rsColor = nbt.getByte(RS_CHANNEL2);
			secondary = new RSChannel(rsController, rsColor);
		} else {
			secondary = RSChannel.INVALID_CHANNEL;
		}
	}

	private static final float SIZE = .25F;
	private static final float WIDTH = 1.5F*SIZE;
	private static final float BORDER = SIZE /20;
	private static final float[] BLACK = {0, 0, 0, 1};
	private static final float[] WHITE = {1, 1, 1, 1};
	@Override
	@SideOnly(Side.CLIENT)
	public List<RawQuad> getQuads() {
		List<RawQuad> ret = new ArrayList<>();
		float width = wide?WIDTH:SIZE;
		//main panel
		PanelUtils.addColoredQuad(ret, new Vector3f(), new Vector3f(0, 0, SIZE), new Vector3f(width, 0, SIZE),
				new Vector3f(width, 0, 0), EnumFacing.UP, BLACK);
		PanelUtils.addColoredQuad(ret, new Vector3f(BORDER, Y_DELTA, BORDER), new Vector3f(BORDER, Y_DELTA, SIZE-BORDER),
				new Vector3f(width-BORDER, Y_DELTA, SIZE-BORDER), new Vector3f(width-BORDER, Y_DELTA, BORDER), EnumFacing.UP, WHITE);

		RawModelFontRenderer r = RawModelFontRenderer.get();
		r.setScale(.5F);
		r.transform = new Matrix4();
		for (int i = 0;i<=3;i++) {
			transformNumber(r.transform, 5*17*i);
			String asString = Integer.toString(5*i);
			int lengthHalf = r.getStringWidth(asString)/2;
			r.transform.translate(-lengthHalf*r.getScale(), 0, -3.5*r.getScale());
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

	private void transformNumber(Matrix4 mat, int value) {
		if (wide) {
			transformNeedle(mat, value);
			mat.translate(0, 0, getLength()+1.5*BORDER);
			mat.scale(-1, 1, -1);
		} else {
			mat.setIdentity().translate(0, Y_DELTA, SIZE);
			mat.translate(SIZE-3*BORDER, 0, -3*BORDER);
			float angle = 90*(1-value/255F);
			angle = (float) (angle*Math.PI/180);
			float length = getLength()+BORDER;
			mat.translate((float)(-Math.sin(angle)*length), 0, (float)(-Math.cos(angle)*length));
		}
	}

	private void transformNeedle(Matrix4 mat, int value) {
		mat.setIdentity().translate(0, 2*Y_DELTA, SIZE);
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
		PanelMeter ret = new PanelMeter(primary, secondary, wide);
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
	public void interactWith(Vec3d hitRelative, EntityPlayerMP player) {
	}

	@Override
	public void update() {

	}

	@Override
	public void setNetwork(ControlPanelNetwork net) {
		super.setNetwork(net);
		Consumer<RSChannelState> listenerPrimary = (input) -> {
			byte strength = input.getStrength();
			if (strength != rsInput >> 4) {
				if (secondary.isValid()) {
					rsInput = (strength << 4) | (rsInput & 0xf);
				} else {
					rsInput = strength * 17;
				}
				panel.markDirty();
				panel.triggerRenderUpdate();
			}
		};
		net.addListener(this, listenerPrimary, primary);
		if (secondary.isValid()) {
			Consumer<RSChannelState> listenerSec = (input) -> {
				if (input.getStrength() != (rsInput & 0xf)) {
					rsInput = (input.getStrength() & 0xf) | (rsInput & 0xf0);
					panel.markDirty();
					panel.triggerRenderUpdate();
				}
			};
			net.addListener(this, listenerSec, secondary);
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

		PanelMeter that = (PanelMeter) o;

		if (rsInput != that.rsInput) return false;
		if (wide != that.wide) return false;
		if (!primary.equals(that.primary)) return false;
		return secondary.equals(that.secondary);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + primary.hashCode();
		result = 31 * result + secondary.hashCode();
		result = 31 * result + rsInput;
		result = 31 * result + (wide ? 1 : 0);
		return result;
	}

	@Override
	@SideOnly(Side.CLIENT)
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
					primary = primary.withColor(value);
				} else {
					secondary = secondary.withColor(value);
				}
				break;
			case INT:
				if (id == 0) {
					primary = primary.withController(value);
				} else {
					secondary = secondary.withController(value);
				}
				break;
			case BOOL:
				wide = ((NBTTagByte) value).getByte() != 0;
		}
	}

	@Nullable
	@Override
	@SideOnly(Side.CLIENT)
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
	@SideOnly(Side.CLIENT)
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
	public RSColorConfig[] getRSChannelOptions() {
		return new RSColorConfig[]{
				new RSColorConfig("channel", 0, 0, primary.getColor(), false),
				new RSColorConfig("channel2", 60, 0, secondary.getColor(), false)
		};
	}

	@Override
	public IntConfig[] getIntegerOptions() {
		return new IntConfig[]{
				new IntConfig("rsId", 0, 60, primary.getController(), 2, false),
				new IntConfig("rsId2", 60, 60, secondary.getController(), 2, true)
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
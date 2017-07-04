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

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.controlpanel.BlockPanel;
import malte0811.industrialWires.blocks.controlpanel.BlockTypes_Panel;
import malte0811.industrialWires.client.RawQuad;
import malte0811.industrialWires.client.panelmodel.SmartLightingQuadIW;
import malte0811.industrialWires.controlpanel.PropertyComponents.PanelRenderProperties;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import static malte0811.industrialWires.controlpanel.PanelComponent.*;
import static malte0811.industrialWires.util.MiscUtils.discoverLocal;

public final class PanelUtils {
	public static TextureAtlasSprite PANEL_TEXTURE;
	public static final Item PANEL_ITEM = new ItemStack(IndustrialWires.panel).getItem();
	private static ItemStack panelBase;

	private PanelUtils() {
	}

	@SideOnly(Side.CLIENT)
	public static List<BakedQuad> generateQuads(PanelRenderProperties components) {
		if (PANEL_TEXTURE == null) {
			PANEL_TEXTURE = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(IndustrialWires.MODID + ":blocks/control_panel");
		}
		List<BakedQuad> ret = new ArrayList<>();
		Matrix4 m4 = components.getPanelTopTransform();
		Matrix4 m4RotOnly = m4.copy();
		m4RotOnly.invert();
		m4RotOnly.transpose();
		//Intentionally not a for-each to help with CME's
		//noinspection ForLoopReplaceableByForEach
		for (int i = 0; i < components.size(); i++) {
			PanelComponent pc = components.get(i);
			Matrix4 m4Here = m4.copy().translate(pc.getX(), .0001, pc.getY());
			List<RawQuad> compQuads = pc.getQuads();
			for (RawQuad bq : compQuads) {
				ret.add(bakeQuad(bq, m4Here, m4RotOnly));
			}
		}
		Matrix4 baseTrans = components.getPanelBaseTransform();
		Matrix4 baseNorm = baseTrans.copy();
		baseNorm.invert();
		baseNorm.transpose();

		List<RawQuad> rawOut = new ArrayList<>();
		float height1 = getLocalHeightFromZ(1, components.getHeight(), components.getAngle());
		float height0 = getLocalHeightFromZ(0, components.getHeight(), components.getAngle());
		float vMax1 = 16 * height1;
		float vMax0 = 16 * height0;
		//TOP
		rawOut.add(new RawQuad(new Vector3f(0, height0, 0), new Vector3f(0, height1, 1),
				new Vector3f(1, height1, 1), new Vector3f(1, height0, 0),
				EnumFacing.UP, PANEL_TEXTURE, WHITE, null, UV_FULL, -1));
		//BOTTOM
		rawOut.add(new RawQuad(new Vector3f(0, 0, 0), new Vector3f(1, 0, 0),
				new Vector3f(1, 0, 1), new Vector3f(0, 0, 1),
				EnumFacing.DOWN, PANEL_TEXTURE, WHITE, null, UV_FULL, -1));
		//LEFT
		rawOut.add(new RawQuad(new Vector3f(0, 0, 0), new Vector3f(0, 0, 1),
				new Vector3f(0, height1, 1), new Vector3f(0, height0, 0),
				EnumFacing.UP, PANEL_TEXTURE, WHITE, null, new float[][]{
				{0, 0}, {0, 16},
				{vMax1, 16}, {vMax0, 0}
		}, -1));
		//RIGHT
		rawOut.add(new RawQuad(new Vector3f(1, 0, 0), new Vector3f(1, height0, 0),
				new Vector3f(1, height1, 1), new Vector3f(1, 0, 1),
				EnumFacing.UP, PANEL_TEXTURE, WHITE, null, new float[][]{
				{0, 0}, {vMax0, 0},
				{vMax1, 16}, {0, 16}
		}, -1));
		//BACK
		rawOut.add(new RawQuad(new Vector3f(1, 0, 0), new Vector3f(0, 0, 0),
				new Vector3f(0, height0, 0), new Vector3f(1, height0, 0),
				EnumFacing.UP, PANEL_TEXTURE, WHITE, null, new float[]{0, 0, vMax0, 16}, -1));
		//FRONT
		rawOut.add(new RawQuad(new Vector3f(0, 0, 1), new Vector3f(1, 0, 1),
				new Vector3f(1, height1, 1), new Vector3f(0, height1, 1),
				EnumFacing.UP, PANEL_TEXTURE, WHITE, null, new float[]{0, 0, vMax1, 16}, -1));
		for (RawQuad bq : rawOut) {
			ret.add(bakeQuad(bq, baseTrans, baseNorm));
		}

		return ret;
	}

	@SideOnly(Side.CLIENT)
	public static BakedQuad bakeQuad(RawQuad raw, Matrix4 transform, Matrix4 transfNormal) {
		VertexFormat format = DefaultVertexFormats.ITEM;
		UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
		builder.setQuadOrientation(raw.facing);
		builder.setTexture(raw.tex);
		Vector3f[] vertices = raw.vertices;
		float[][] uvs = raw.uvs;
		Vector3f normal = transfNormal.apply(raw.normal);
		OBJModel.Normal faceNormal = new OBJModel.Normal(normal.x, normal.y, normal.z);
		for (int i = 0; i < 4; i++) {
			putVertexData(format, builder, transform.apply(vertices[i]), faceNormal, uvs[i][0], uvs[i][1], raw.tex,
					raw.colorA);
		}
		BakedQuad ret = builder.build();
		if (raw.light>0) {
			ret = new SmartLightingQuadIW(ret, raw.light);
		}
		return ret;
	}

	//mostly copied from IE's ClientUtils, it has protected access there...
	@SideOnly(Side.CLIENT)
	public static void putVertexData(VertexFormat format, UnpackedBakedQuad.Builder builder, Vector3f pos, OBJModel.Normal faceNormal, double u, double v, TextureAtlasSprite sprite, float[] colorA) {
		for (int e = 0; e < format.getElementCount(); e++)
			switch (format.getElement(e).getUsage()) {
			case POSITION:
				builder.put(e, pos.getX(), pos.getY(), pos.getZ(), 0);
				break;
			case COLOR:
				builder.put(e, colorA[0], colorA[1], colorA[2], colorA[3]);
				break;
			case UV:
				if (sprite == null)//Double Safety. I have no idea how it even happens, but it somehow did .-.
					sprite = Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
				builder.put(e,
						sprite.getInterpolatedU(u),
						sprite.getInterpolatedV((v)),
						0, 1);
				break;
			case NORMAL:
				builder.put(e, faceNormal.x, faceNormal.y, faceNormal.z, 0);
				break;
			default:
				builder.put(e);
			}
	}

	private static final float[] UV_FULL = {0, 0, 16, 16};
	private static final float[] WHITE = {1, 1, 1, 1};

	@SideOnly(Side.CLIENT)
	public static void addTexturedBox(Vector3f min, Vector3f size, List<RawQuad> out, float[] uvs, TextureAtlasSprite tex) {
		addBox(WHITE, WHITE, WHITE, min, size, out, true, uvs, tex, null, false);
	}

	@SideOnly(Side.CLIENT)
	public static void addColoredBox(float[] colorTop, float[] colorSides, float[] colorBottom, Vector3f min, Vector3f size, List<RawQuad> out, boolean doBottom) {
		addBox(colorTop, colorSides, colorBottom, min, size, out, doBottom, UV_FULL, ModelLoader.White.INSTANCE, null, false);
	}

	@SideOnly(Side.CLIENT)
	public static void addColoredBox(float[] colorTop, float[] colorSides, float[] colorBottom, Vector3f min, Vector3f size, List<RawQuad> out, boolean doBottom, @Nullable Matrix4 mat) {
		addBox(colorTop, colorSides, colorBottom, min, size, out, doBottom, UV_FULL, ModelLoader.White.INSTANCE, mat, false);
	}

	@SideOnly(Side.CLIENT)
	public static void addColoredBox(float[] colorTop, float[] colorSides, float[] colorBottom, Vector3f min, Vector3f size, List<RawQuad> out, boolean doBottom, @Nullable Matrix4 mat, boolean inside) {
		addBox(colorTop, colorSides, colorBottom, min, size, out, doBottom, UV_FULL, ModelLoader.White.INSTANCE, mat, inside);
	}

	@SideOnly(Side.CLIENT)
	public static void addBox(float[] colorTop, float[] colorSides, float[] colorBottom, Vector3f min, Vector3f size, List<RawQuad> out, boolean doBottom, float[] uvs, TextureAtlasSprite tex,
							  @Nullable Matrix4 mat, boolean inside) {
		addQuad(out, new Vector3f(min.x, min.y + size.y, min.z), new Vector3f(min.x, min.y + size.y, min.z + size.z),
				new Vector3f(min.x + size.x, min.y + size.y, min.z + size.z), new Vector3f(min.x + size.x, min.y + size.y, min.z),
				EnumFacing.UP, colorTop, tex, uvs, mat, inside);
		if (doBottom) {
			addQuad(out, new Vector3f(min.x, min.y, min.z), new Vector3f(min.x + size.x, min.y, min.z),
					new Vector3f(min.x + size.x, min.y, min.z + size.z), new Vector3f(min.x, min.y, min.z + size.z),
					EnumFacing.UP, colorBottom, tex, uvs, mat, inside);
		}
		addQuad(out, new Vector3f(min.x, min.y, min.z), new Vector3f(min.x, min.y, min.z + size.z),
				new Vector3f(min.x, min.y + size.y, min.z + size.z), new Vector3f(min.x, min.y + size.y, min.z),
				EnumFacing.WEST, colorSides, tex, uvs, mat, inside);
		addQuad(out, new Vector3f(min.x + size.x, min.y, min.z), new Vector3f(min.x + size.x, min.y + size.y, min.z),
				new Vector3f(min.x + size.x, min.y + size.y, min.z + size.z), new Vector3f(min.x + size.x, min.y, min.z + size.z),
				EnumFacing.EAST, colorSides, tex, uvs, mat, inside);
		addQuad(out, new Vector3f(min.x, min.y, min.z), new Vector3f(min.x, min.y + size.y, min.z),
				new Vector3f(min.x + size.x, min.y + size.y, min.z), new Vector3f(min.x + size.x, min.y, min.z),
				EnumFacing.NORTH, colorSides, tex, uvs, mat, inside);
		addQuad(out, new Vector3f(min.x, min.y, min.z + size.z), new Vector3f(min.x + size.x, min.y, min.z + size.z),
				new Vector3f(min.x + size.x, min.y + size.y, min.z + size.z), new Vector3f(min.x, min.y + size.y, min.z + size.z),
				EnumFacing.SOUTH, colorSides, tex, uvs, mat, inside);
	}

	@SideOnly(Side.CLIENT)
	public static void addColoredQuad(List<RawQuad> out, Vector3f v0, Vector3f v1, Vector3f v2, Vector3f v3, EnumFacing dir, float[] color) {
		addQuad(out, v0, v1, v2, v3, dir, color, Minecraft.getMinecraft().getTextureMapBlocks().getTextureExtry(ModelLoader.White.LOCATION.toString()), UV_FULL, null, false);
	}

	@SideOnly(Side.CLIENT)
	public static void addColoredQuad(List<RawQuad> out, Vector3f v0, Vector3f v1, Vector3f v2, Vector3f v3, EnumFacing dir, float[] color, @Nullable Matrix4 mat) {
		addQuad(out, v0, v1, v2, v3, dir, color, Minecraft.getMinecraft().getTextureMapBlocks().getTextureExtry(ModelLoader.White.LOCATION.toString()), UV_FULL, mat, false);
	}

	@SideOnly(Side.CLIENT)
	public static void addQuad(List<RawQuad> out, Vector3f v0, Vector3f v1, Vector3f v2, Vector3f v3, EnumFacing dir, float[] color, TextureAtlasSprite tex, float[] uvs, @Nullable Matrix4 mat, boolean bidirectional) {
		Vec3i dirV = dir.getDirectionVec();
		RawQuad quad = new RawQuad(v0, v1, v2, v3, dir, tex,
				color, new Vector3f(dirV.getX(), dirV.getY(), dirV.getZ()), uvs);
		if (mat != null) {
			quad = quad.apply(mat);
		}
		out.add(quad);
		if (bidirectional) {
			dirV = dir.getOpposite().getDirectionVec();
			quad = new RawQuad(v3, v2, v1, v0, dir, tex,
					color, new Vector3f(dirV.getX(), dirV.getY(), dirV.getZ()), uvs);
			if (mat != null) {
				quad = quad.apply(mat);
			}
			out.add(quad);
		}
	}

	public static void addInfo(ItemStack stack, List<String> list, NBTTagCompound data) {
		switch (stack.getMetadata()) {
		case 0: //button
			addCommonInfo(data, list, true, true);
			if (data.hasKey(LATCHING)) {
				list.add(I18n.format(IndustrialWires.MODID + ".tooltip." + (data.getBoolean(LATCHING) ? "latching" : "instantaneous")));
			}
			break;
		case 1: //label
			if (data.hasKey(TEXT)) {
				list.add(I18n.format(IndustrialWires.MODID + ".tooltip.text", data.getString(TEXT)));
			}
			addCommonInfo(data, list, true, false);
			break;
		case 2: //indicator light
			addCommonInfo(data, list, true, true);
			break;
		case 3: //slider
			addCommonInfo(data, list, true, true);
			if (data.hasKey(HORIZONTAL)) {
				list.add(I18n.format(IndustrialWires.MODID + ".tooltip." + (data.getBoolean(HORIZONTAL) ? "horizontal" : "vertical")));
			}
			if (data.hasKey(LENGTH)) {
				list.add(I18n.format(IndustrialWires.MODID + ".tooltip.length", data.getFloat(LENGTH)));
			}

			break;
		case 4://variac
			addCommonInfo(data, list, false, true);
			break;
		case 5://Toggle switch
			addCommonInfo(data, list, false, true);
			break;
		case 6://Covered toggle switch
			addCommonInfo(data, list, true, true);
			break;
		case 7://Lock
			addCommonInfo(data, list, false, true);
			if (data.hasKey(LATCHING)) {
				list.add(I18n.format(IndustrialWires.MODID + ".tooltip." + (data.getBoolean(LATCHING) ? "latching" : "instantaneous")));
			}
			break;
		case 8://Panel meter
			addCommonInfo(data, list, false, true);
			if (data.hasKey(PanelMeter.WIDE)) {
				list.add(I18n.format(IndustrialWires.MODID + ".tooltip." + (data.getBoolean(PanelMeter.WIDE) ? "wide" : "narrow")));
			}
			break;
		}
	}

	public static void addCommonInfo(NBTTagCompound data, List<String> list, boolean color, boolean rs) {
		if (color && data.hasKey(COLOR)) {
			String hexCol = String.format("%6s", Integer.toHexString(data.getInteger(COLOR) & 0xffffff)).replace(' ', '0');
			list.add(I18n.format(Lib.DESC_INFO + "colour", "<hexcol=" + hexCol + ":#" + hexCol + ">"));
		}
		if (rs && data.hasKey(RS_CHANNEL)) {
			EnumDyeColor channColor = EnumDyeColor.byMetadata(data.getInteger(RS_CHANNEL));
			String hexCol = Integer.toHexString(channColor.getMapColor().colorValue);
			list.add(I18n.format("desc.immersiveengineering.info.redstoneChannel", "<hexcol=" + hexCol + ":" + channColor.getUnlocalizedName() + ">"));
		}
		if (rs && data.hasKey(RS_ID)) {
			list.add(I18n.format(IndustrialWires.MODID + ".tooltip.rsId", data.getInteger(RS_ID)));
		}
	}

	public static int setColor(int color, int id, NBTBase value) {
		id = 2 - id;
		color &= ~(0xff << (8 * id));
		color |= (int) (2.55 * (((NBTTagFloat) value).getFloat())) << (8 * id);
		return color;
	}

	public static float[] getFloatColor(boolean active, int color) {
		float[] ret = new float[4];
		ret[3] = 1;
		for (int i = 0; i < 3; i++) {
			ret[i] = ((color >> (8 * (2 - i))) & 255) / 255F * (active ? 1 : .5F);
		}
		return ret;
	}

	public static boolean intersectXZ(AxisAlignedBB aabb1, AxisAlignedBB aabb2) {
		return aabb1.minX < aabb2.maxX && aabb1.maxX > aabb2.minX && aabb1.minZ < aabb2.maxZ && aabb1.maxZ > aabb2.minZ;
	}

	public static void readListFromNBT(NBTTagList list, @Nonnull List<PanelComponent> base) {
		boolean allNew = list.tagCount() != base.size();
		if (allNew) {
			base.clear();
		}
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound nbt = list.getCompoundTagAt(i);
			PanelComponent pc = PanelComponent.read(nbt);
			if (pc != null) {
				if (allNew) {
					base.add(pc);
				} else {
					PanelComponent oldPc = base.get(i);
					if (pc.getClass() != oldPc.getClass()) {
						base.set(i, pc);
					} else {
						oldPc.readFromNBT(nbt);
					}
				}
			}
		}
	}

	public static ItemStack getPanelBase() {
		if (panelBase == null) {
			panelBase = new ItemStack(IndustrialWires.panel, 1, BlockTypes_Panel.UNFINISHED.ordinal());
		}
		return panelBase;
	}

	public static List<BlockPos> discoverPanelParts(World w, BlockPos here, int maxCount) {
		BiPredicate<BlockPos, Integer> isValid = (pos, count) -> {
			if (pos.equals(here)) {
				return true;
			}
			if (here.distanceSq(pos) > 25 || count > maxCount || !w.isBlockLoaded(pos)) {
				return false;
			}
			IBlockState state = w.getBlockState(pos);
			return state.getBlock() == IndustrialWires.panel && state.getValue(BlockPanel.type).isPanelConnector();
		};
		List<BlockPos> all = discoverLocal(w, here, isValid);
		List<BlockPos> ret = new ArrayList<>();
		for (BlockPos pos : all) {
			if (w.getBlockState(pos).getBlock() == IndustrialWires.panel && w.getBlockState(pos).getValue(BlockPanel.type) != BlockTypes_Panel.DUMMY) {
				ret.add(pos);
			}
		}
		return ret;
	}

	public static float getAngle(ItemStack inv) {
		float angle = 0;
		NBTTagCompound nbt = inv.getTagCompound();
		if (nbt != null && nbt.hasKey("angle")) {
			angle = nbt.getFloat("angle");
		}
		return angle;
	}

	public static float getHeight(ItemStack inv) {
		float height = .5F;
		NBTTagCompound nbt = inv.getTagCompound();
		if (nbt != null && nbt.hasKey("height")) {
			height = nbt.getFloat("height");
		}
		return height;
	}

	public static float getHeightWithComponent(PanelComponent pc, float angle, float height) {
		AxisAlignedBB aabb = pc.getBlockRelativeAABB();
		double y = angle > 0 ? aabb.minZ : aabb.maxZ;
		float hComp = (float) (pc.getHeight() * Math.cos(angle));
		float localPanelHeight = getLocalHeight(y, angle, height);
		return hComp + localPanelHeight;
	}

	public static float getLocalHeight(double y, float angle, float height) {
		double centerOffset = .5 * (1 / Math.cos(angle) - 1);
		y += centerOffset;
		return getLocalHeightFromZ(Math.cos(angle) * y, height, angle);
	}

	public static float getLocalHeightFromZ(double z, float height, float angle) {
		return (float) (height + (.5 - z) * Math.tan(angle));
	}
}
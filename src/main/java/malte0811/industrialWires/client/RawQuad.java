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

package malte0811.industrialWires.client;

import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import org.lwjgl.util.vector.Vector3f;

import java.util.Arrays;

public class RawQuad {
	public final Vector3f[] vertices = new Vector3f[4];
	public final EnumFacing facing;
	public final TextureAtlasSprite tex;
	public final float[] colorA;
	public final Vector3f normal;
	public final float[] uvs;
	public RawQuad(Vector3f v0, Vector3f v1, Vector3f v2, Vector3f v3,
				   EnumFacing facing, TextureAtlasSprite tex, float[] colorA,
				   Vector3f normal, float[] uvs) {
		vertices[0] = v0;
		vertices[1] = v1;
		vertices[2] = v2;
		vertices[3] = v3;
		this.facing = facing;
		this.tex = tex;
		if (colorA.length==3) {
			this.colorA = Arrays.copyOf(colorA, 4);
			this.colorA[3] = 1;
		} else {
			this.colorA = colorA;
		}
		this.normal = normal;
		this.uvs = uvs;
	}
	public RawQuad apply(Matrix4 mat) {
		Matrix4 matNormal = mat.copy().transpose();
		matNormal.invert();
		return new RawQuad(mat.apply(vertices[0]), mat.apply(vertices[1]), mat.apply(vertices[2]), mat.apply(vertices[3]),
				facing, tex, colorA, matNormal.apply(normal), uvs);
	}
}

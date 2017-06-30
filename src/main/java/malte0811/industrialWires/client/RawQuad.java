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

import javax.annotation.Nullable;
import java.util.Arrays;

import static org.lwjgl.util.vector.Vector3f.cross;
import static org.lwjgl.util.vector.Vector3f.sub;

public class RawQuad {
	public final Vector3f[] vertices = new Vector3f[4];
	public final EnumFacing facing;
	public final TextureAtlasSprite tex;
	public final float[] colorA;
	public final Vector3f normal;
	public final float[][] uvs;
	public int light;

	public RawQuad(Vector3f v0, Vector3f v1, Vector3f v2, Vector3f v3,
				   EnumFacing facing, TextureAtlasSprite tex, float[] colorA,
				   Vector3f normal, float[] uvs) {
		this(v0, v1, v2, v3, facing, tex, colorA, normal, uvs, -1);
	}

	public RawQuad(Vector3f v0, Vector3f v1, Vector3f v2, Vector3f v3,
				   EnumFacing facing, TextureAtlasSprite tex, float[] colorA,
				   Vector3f normal, float[] uvs, int light) {
		this(v0, v1, v2, v3, facing, tex, colorA, normal, new float[][]{
				{uvs[0], uvs[1]}, {uvs[0], uvs[3]},
				{uvs[2], uvs[3]}, {uvs[2], uvs[1]}
		}, light);
	}

	public RawQuad(Vector3f v0, Vector3f v1, Vector3f v2, Vector3f v3,
				   EnumFacing facing, TextureAtlasSprite tex, float[] colorA,
				   @Nullable Vector3f normal, float[][] uvs, int light) {
		vertices[0] = v0;
		vertices[1] = v1;
		vertices[2] = v2;
		vertices[3] = v3;
		this.facing = facing;
		this.tex = tex;
		if (colorA.length == 3) {
			this.colorA = Arrays.copyOf(colorA, 4);
			this.colorA[3] = 1;
		} else {
			this.colorA = colorA;
		}
		if (normal != null) {
			this.normal = normal;
		} else {
			this.normal = cross(sub(v1, v3, null), sub(v2, v0, null), null);
			this.normal.normalise(this.normal);
		}
		this.uvs = uvs;
		this.light = light;
	}

	public RawQuad apply(Matrix4 mat) {
		Matrix4 matNormal = mat.copy().transpose();
		matNormal.invert();
		return new RawQuad(mat.apply(vertices[0]), mat.apply(vertices[1]), mat.apply(vertices[2]), mat.apply(vertices[3]),
				facing, tex, colorA, matNormal.apply(normal), uvs, light);
	}
}

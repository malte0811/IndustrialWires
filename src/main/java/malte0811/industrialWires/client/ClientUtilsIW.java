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

package malte0811.industrialWires.client;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class ClientUtilsIW {
	/**
	 * Base on {@link blusunrize.immersiveengineering.client.ClientUtils#renderModelTESRFast(List, BufferBuilder, World, BlockPos)}
	 * (which I wrote)
	 */
	public static void renderModelTESRFast(List<BakedQuad> quads, BufferBuilder renderer) {
		int brightness = 15 << 20 | 15 << 4;
		int l1 = (brightness >> 0x10) & 0xFFFF;
		int l2 = brightness & 0xFFFF;
		for (BakedQuad quad : quads) {
			int[] vData = quad.getVertexData();
			VertexFormat format = quad.getFormat();
			int size = format.getIntegerSize();
			int uv = format.getUvOffsetById(0) / 4;
			for (int i = 0; i < 4; ++i) {
				renderer
						.pos(Float.intBitsToFloat(vData[size * i]),
								Float.intBitsToFloat(vData[size * i + 1]),
								Float.intBitsToFloat(vData[size * i + 2]))
						.color(255, 255, 255, 255)
						.tex(Float.intBitsToFloat(vData[size * i + uv]), Float.intBitsToFloat(vData[size * i + uv + 1]))
						.lightmap(l1, l2)
						.endVertex();
			}

		}
	}
}

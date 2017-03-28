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

package malte0811.industrialWires.blocks.controlpanel;

import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IUnlistedProperty;

import java.util.ArrayList;

public class PropertyComponents implements IUnlistedProperty<PropertyComponents.PanelRenderProperties> {
	public static PropertyComponents INSTANCE = new PropertyComponents();
	@Override
	public String getName() {
		return "components";
	}

	@Override
	public boolean isValid(PanelRenderProperties value) {
		return value!=null;
	}

	@Override
	public Class<PanelRenderProperties> getType() {
		return PanelRenderProperties.class;
	}

	@Override
	public String valueToString(PanelRenderProperties value) {
		return value.toString();
	}

	public static class PanelRenderProperties extends ArrayList<PanelComponent> {
		public EnumFacing facing = EnumFacing.NORTH;
		public float height = .5F;
		public TileEntityPanel panel;//Don't compare this+erase it on copying
		public PanelRenderProperties() {
			super();
		}
		public PanelRenderProperties(int length) {
			super(length);
		}
		@Override
		public String toString() {
			StringBuilder ret = new StringBuilder("[");
			for (int i = 0;i<size();i++) {
				ret.append(get(i));
				if (i<size()-1) {
					ret.append(", ");
				}
			}
			return ret+"]";
		}
		public Matrix4 getPanelTopTransform() {
			Matrix4 ret = new Matrix4();
			ret.translate(.5, height, .5);
			ret.rotate(-facing.getHorizontalAngle()*Math.PI/180+Math.PI, 0, 1, 0);
			ret.translate(-.5, 0, -.5);
			return ret;
		}
		public PanelRenderProperties copyOf() {
			PanelRenderProperties ret = new PanelRenderProperties(size());
			for (PanelComponent pc:this) {
				ret.add(pc.copyOf());
			}
			ret.facing = facing;
			return ret;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			if (!super.equals(o)) return false;

			PanelRenderProperties that = (PanelRenderProperties) o;

			return facing == that.facing;
		}

		@Override
		public int hashCode() {
			int result = super.hashCode();
			result = 31 * result + facing.hashCode();
			return result;
		}
	}
}

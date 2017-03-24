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

import net.minecraftforge.common.property.IUnlistedProperty;

import java.util.ArrayList;

public class PropertyComponents implements IUnlistedProperty<PropertyComponents.ComponentList> {
	public static PropertyComponents INSTANCE = new PropertyComponents();
	@Override
	public String getName() {
		return "components";
	}

	@Override
	public boolean isValid(ComponentList value) {
		return value!=null;
	}

	@Override
	public Class<ComponentList> getType() {
		return ComponentList.class;
	}

	@Override
	public String valueToString(ComponentList value) {
		return value.toString();
	}

	public static class ComponentList extends ArrayList<PanelComponent> {
		public ComponentList() {
			super();
		}
		public ComponentList(int length) {
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
		public ComponentList copyOf() {
			ComponentList ret = new ComponentList(size());
			ret.addAll(this);
			return ret;
		}
	}
}

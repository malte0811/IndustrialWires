package malte0811.industrialWires.util;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import com.google.common.collect.ImmutableSet;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

public final class MiscUtils {
	private MiscUtils() {}
	public static Set<ImmersiveNetHandler.Connection> genConnBlockstate(Set<ImmersiveNetHandler.Connection> conns, World world)
	{
		if (conns == null)
			return ImmutableSet.of();
		Set<ImmersiveNetHandler.Connection> ret = new HashSet<ImmersiveNetHandler.Connection>()
		{
			@Override
			public boolean equals(Object o)
			{
				if (o == this)
					return true;
				if (!(o instanceof HashSet))
					return false;
				HashSet<ImmersiveNetHandler.Connection> other = (HashSet<ImmersiveNetHandler.Connection>) o;
				if (other.size() != this.size())
					return false;
				for (ImmersiveNetHandler.Connection c : this)
					if (!other.contains(c))
						return false;
				return true;
			}
		};
		for (ImmersiveNetHandler.Connection c : conns)
		{
			IImmersiveConnectable end = ApiUtils.toIIC(c.end, world, false);
			if (end==null)
				continue;
			// generate subvertices
			c.getSubVertices(world);
			ret.add(c);
		}

		return ret;
	}
}

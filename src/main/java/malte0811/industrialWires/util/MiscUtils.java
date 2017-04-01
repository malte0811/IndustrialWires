package malte0811.industrialWires.util;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import com.google.common.collect.ImmutableSet;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.controlpanel.BlockPanel;
import malte0811.industrialWires.blocks.controlpanel.BlockTypes_Panel;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

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
	public static List<BlockPos> discoverPanelParts(World w, BlockPos here) {
		BiPredicate<BlockPos, Integer> isValid = (pos, count)->{
			if (here.distanceSq(pos)>25||count>100||!w.isBlockLoaded(pos)) {
				return false;
			}
			IBlockState state = w.getBlockState(pos);
			return state.getBlock()== IndustrialWires.panel;
		};
		List<BlockPos> all = discoverLocal(w, here, isValid);
		List<BlockPos> ret = new ArrayList<>();
		for (BlockPos pos:all) {
			if (w.getBlockState(pos).getValue(BlockPanel.type)!= BlockTypes_Panel.DUMMY) {
				ret.add(pos);
			}
		}
		return ret;
	}
	public static List<BlockPos> discoverLocal(World w, BlockPos here, BiPredicate<BlockPos, Integer> isValid) {
		List<BlockPos> ret = new ArrayList<>();
		List<BlockPos> open = new ArrayList<>();
		open.add(here);
		while (!open.isEmpty()) {
			BlockPos curr = open.get(0);
			ret.add(curr);
			open.remove(0);
			for (EnumFacing f:EnumFacing.VALUES) {
				BlockPos next = curr.offset(f);
				if (!open.contains(next)&&!ret.contains(next)&&isValid.test(next, ret.size())) {
					open.add(next);
				}
			}
		}
		return ret;
	}
}

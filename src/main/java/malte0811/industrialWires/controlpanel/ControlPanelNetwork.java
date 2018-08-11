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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import malte0811.industrialWires.blocks.controlpanel.TileEntityGeneralCP;
import malte0811.industrialWires.util.MiscUtils;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

@SuppressWarnings({"unused", "WeakerAccess"})
public class ControlPanelNetwork {
	protected Map<RSChannel, List<ChangeListener>> listeners = new HashMap<>();
	protected Map<RSChannel, List<OutputValue>> allOutputs = new HashMap<>();
	protected Map<RSChannel, OutputValue> activeOutputs = new HashMap<>();
	protected Map<RSChannel, OutputValue> secondActiveOutputs = new HashMap<>();
	protected Set<BlockPos> members = new HashSet<>();

	public void addListener(IOwner owner, Consumer<RSChannelState> listener, RSChannel... channels) {
		ChangeListener l = new ChangeListener(owner, listener);
		for (RSChannel channel:channels) {
			if (!channel.isValid()) {
				continue;
			}
			listeners.computeIfAbsent(channel, c->new ArrayList<>())
					.add(l);
			if (activeOutputs.containsKey(channel)) {
				listener.accept(activeOutputs.get(channel).targetState);
			} else {
				listener.accept(new RSChannelState(channel, (byte) 0));
			}
		}
	}

	public void setOutputs(IOwner owner, RSChannelState... out) {
		for (RSChannelState o:out) {
			if (!o.getChannel().isValid()) {
				continue;
			}
			if (removeForChannel(owner, allOutputs.get(o.getChannel()), null)) {
				allOutputs.remove(o.getChannel());
			}
			if (o.getStrength()>0) {
				OutputValue outVal = new OutputValue(owner, o);
				allOutputs.computeIfAbsent(o.getChannel(), c -> new ArrayList<>())
						.add(outVal);
			}
			recalculateOutput(o.getChannel(), Collections.singleton(owner), Collections.emptyList());
		}
	}

	public void removeIOFor(IOwner owner) {
		Iterator<Map.Entry<RSChannel, List<ChangeListener>>> iteratorL = listeners.entrySet().iterator();
		while (iteratorL.hasNext()) {
			Map.Entry<RSChannel, List<ChangeListener>> entry = iteratorL.next();
			removeForChannel(owner, entry.getValue(), iteratorL);
		}
		Iterator<Map.Entry<RSChannel, List<OutputValue>>> iteratorO = allOutputs.entrySet().iterator();
		while (iteratorO.hasNext()) {
			Map.Entry<RSChannel, List<OutputValue>> entry = iteratorO.next();
			if (!removeForChannel(owner, entry.getValue(), iteratorO)) {
				recalculateOutput(entry.getKey(), Collections.singleton(owner), Collections.emptyList());
			}
		}
	}

	public void removeMember(BlockPos pos, World w) {
		for (List<ChangeListener> list : listeners.values()) {
			list.removeIf(l->l.ownerAtPos(pos));
		}
		Iterator<Map.Entry<RSChannel, List<OutputValue>>> iterator = allOutputs.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<RSChannel, List<OutputValue>> entry = iterator.next();
			entry.getValue().removeIf(l -> l.ownerAtPos(pos));
			if (entry.getValue().isEmpty()) {
				iterator.remove();
			}
			recalculateOutput(entry.getKey(), Collections.emptyList(), Collections.singleton(pos));
		}
		members.remove(pos);
		split(pos, w);
	}

	//This does not call split!
	private void removeAllMembers(Collection<BlockPos> toRemove) {
		Iterator<Map.Entry<RSChannel, List<ChangeListener>>> iteratorL = listeners.entrySet().iterator();
		while (iteratorL.hasNext()) {
			Map.Entry<RSChannel, List<ChangeListener>> entry = iteratorL.next();
			entry.getValue().removeIf(l -> l.ownerAtPos(toRemove));
			if (entry.getValue().isEmpty()) {
				iteratorL.remove();
			}
		}
		Iterator<Map.Entry<RSChannel, List<OutputValue>>> iteratorO = allOutputs.entrySet().iterator();
		while (iteratorO.hasNext()) {
			Map.Entry<RSChannel, List<OutputValue>> entry = iteratorO.next();
			entry.getValue().removeIf(l -> l.ownerAtPos(toRemove));
			if (entry.getValue().isEmpty()) {
				iteratorO.remove();
			}
			recalculateOutput(entry.getKey(), Collections.emptyList(), toRemove);
		}
		members.removeAll(toRemove);
	}

	public void addMember(TileEntityGeneralCP member) {
		members.add(member.getBlockPos());
		member.setNetworkAndInit(this);
	}

	public void replaceWith(ControlPanelNetwork newNet, World w) {
		replaceWith(newNet, w, ImmutableSet.copyOf(members));
	}

	private void replaceWith(ControlPanelNetwork newNet, World w, Collection<BlockPos> toReplace) {
		removeAllMembers(ImmutableList.copyOf(toReplace));
		for (BlockPos member:toReplace) {
			TileEntityGeneralCP te = MiscUtils.getLoadedTE(w, member, TileEntityGeneralCP.class);
			if (te!=null) {
				newNet.addMember(te);
			}
		}
	}

	private void recalculateOutput(RSChannel channel, Collection<IOwner> excluded, Collection<BlockPos> excludedPos) {
		OutputValue oldMax = activeOutputs.get(channel);
		OutputValue oldSecMax = secondActiveOutputs.get(channel);
		OutputValue newMax = null;
		OutputValue newSecMax = null;
		if (allOutputs.containsKey(channel)) {
			for (OutputValue v : allOutputs.get(channel)) {
				if (v.isStrongerThan(newMax)) {
					newSecMax = newMax;
					newMax = v;
				} else if (v.isStrongerThan(newSecMax)) {
					newSecMax = v;
				}
			}
		}
		if (newMax == null) {
			newMax = new OutputValue(null, new RSChannelState(channel, (byte) 0));
			newSecMax = newMax;
			activeOutputs.remove(channel);
		} else {
			activeOutputs.put(channel, newMax);
		}
		secondActiveOutputs.put(channel, newSecMax);
		if (newSecMax == null) {
			newSecMax = new OutputValue(null, new RSChannelState(channel, (byte) 0));
		}
		if (!newSecMax.equals(oldSecMax) || !newMax.equals(oldMax)) {
			List<ChangeListener> listenersForChannel = listeners.get(channel);
			if (listenersForChannel != null) {
				for (ChangeListener l : listenersForChannel) {
					if (!l.isOwnedBy(excluded) && !l.ownerAtPos(excludedPos)) {
						if (!l.hasSameOwner(newMax)) {
							l.onChange(newMax.getTargetState());
						} else {
							l.onChange(newSecMax.getTargetState());
						}
					}
				}
			}
		}
	}

	private <T extends Owned> boolean removeForChannel(IOwner owner, List<T> l, Iterator<?> it) {
		if (l==null) {
			return false;
		}
		l.removeIf(val -> val.isOwnedBy(owner));
		if (l.isEmpty()) {
			if (it!=null) {
				it.remove();
			}
			return true;
		} else {
			return false;
		}
	}

	private <T extends Owned> boolean removeForChannel(BlockPos owner, List<T> l, Iterator<?> it) {
		l.removeIf(val -> val.ownerAtPos(owner));
		if (l.isEmpty()) {
			if (it!=null) {
				it.remove();
			}
			return true;
		} else {
			return false;
		}
	}

	private void split(BlockPos pos, World w) {
		Set<BlockPos> reached = new HashSet<>();
		List<BlockPos> newForThis = null;
		for (EnumFacing side : EnumFacing.VALUES) {
			BlockPos start = pos.offset(side);
			if (!reached.contains(start)) {
				List<BlockPos> netForSide = MiscUtils.discoverLocal(start, (p, s) -> members.contains(p));
				if (!netForSide.isEmpty()) {
					reached.addAll(netForSide);
					if (newForThis == null) {
						newForThis = netForSide;
					} else {
						replaceWith(new ControlPanelNetwork(), w, netForSide);
					}
				}
			}
		}
	}

	protected static class ChangeListener extends Owned {
		private final Consumer<RSChannelState> listener;

		private ChangeListener(IOwner owner, Consumer<RSChannelState> listener) {
			super(owner);
			this.listener = listener;
		}

		public void onChange(RSChannelState newState) {
			listener.accept(newState);
		}
	}

	protected static class OutputValue extends Owned {
		private final RSChannelState targetState;

		private OutputValue(@Nullable IOwner owner, RSChannelState targetState) {
			super(owner);
			this.targetState = targetState;
		}

		public RSChannelState getTargetState() {
			return targetState;
		}

		public boolean isStrongerThan(OutputValue other) {
			return other==null || targetState.getStrength()>other.getTargetState().getStrength();
		}
	}

	private static class Owned {
		@Nullable
		private final IOwner owner;

		private Owned(@Nullable IOwner owner) {
			this.owner = owner;
		}

		public final boolean isOwnedBy(IOwner o) {
			return o.equals(owner);
		}

		public final boolean ownerAtPos(BlockPos o) {
			return o.equals(getOwnerPos());
		}

		public final boolean isOwnedBy(Collection<IOwner> o) {
			return o.contains(owner);
		}

		public final boolean ownerAtPos(Collection<BlockPos> o) {
			return o.contains(getOwnerPos());
		}

		public final BlockPos getOwnerPos() {
			return owner==null?BlockPos.ORIGIN:owner.getBlockPos();
		}

		public boolean hasSameOwner(Owned active) {
			return Objects.equals(owner, active.owner);
		}
	}

	public interface IOwner {
		BlockPos getBlockPos();
	}

	public static class RSChannel {
		public static final RSChannel INVALID_CHANNEL = new RSChannel(-1, (byte)0);
		public static final RSChannel DEFAULT_CHANNEL = new RSChannel(0, (byte)0);
		private final int controller;
		private final byte color;

		public RSChannel(int controller, byte color) {
			this.controller = controller;
			this.color = color;
		}

		public byte getColor() {
			return color;
		}

		public int getController() {
			return controller;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			RSChannel rsChannel = (RSChannel) o;

			if (controller != rsChannel.controller) return false;
			return color == rsChannel.color;
		}

		@Override
		public int hashCode() {
			int result = controller;
			result = 31 * result + color;
			return result;
		}

		public boolean isValid() {
			return controller>=0 && color >= 0;
		}

		public RSChannel withController(int controller) {
			return new RSChannel(controller, color);
		}

		public RSChannel withColor(byte color) {
			return new RSChannel(controller, color);
		}

		public RSChannel withController(NBTBase nbt) {
			return withController(((NBTTagInt)nbt).getInt());
		}

		public RSChannel withColor(NBTBase nbt) {
			return withColor(((NBTTagByte)nbt).getByte());
		}
	}

	public static class RSChannelState {
		private final RSChannel channel;
		private final byte strength;

		public RSChannelState(RSChannel channel, byte strength) {
			this.channel = channel;
			this.strength = strength;
		}

		public byte getStrength() {
			return strength;
		}

		public RSChannel getChannel() {
			return channel;
		}

		public int getColor() {
			return getChannel().getColor();
		}

		public int getController() {
			return getChannel().getController();
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			RSChannelState that = (RSChannelState) o;

			if (strength != that.strength) return false;
			return channel.equals(that.channel);
		}

		@Override
		public int hashCode() {
			int result = channel.hashCode();
			result = 31 * result + strength;
			return result;

		}
	}
}

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
package malte0811.industrialWires.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.IWireCoil;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.common.IESaveData;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import malte0811.industrialWires.IWConfig;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.wires.IC2Wiretype;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemIC2Coil extends Item implements IWireCoil {
	public final static String[] subNames = {"tin", "copper", "gold", "hv", "glass"};
	public final static String lengthKey = "wireLength";
	public final static String NAME = "ic2_wire_coil";

	public ItemIC2Coil() {
		setUnlocalizedName(IndustrialWires.MODID + "."+NAME);
		setHasSubtypes(true);
		this.setCreativeTab(IndustrialWires.creativeTab);
		setMaxStackSize(1);
		setRegistryName(new ResourceLocation(IndustrialWires.MODID, NAME));
		IndustrialWires.items.add(this);
	}


	@Override
	public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> subItems) {
		if (isInCreativeTab(tab)) {
			for (int i = 0; i < subNames.length; i++) {
				ItemStack tmp = new ItemStack(this, 1, i);
				setLength(tmp, getMaxWireLength(tmp));
				subItems.add(tmp);
			}
		}
	}

	@Nonnull
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return this.getUnlocalizedName() + "." + subNames[stack.getMetadata()];
	}

	@Override
	public WireType getWireType(ItemStack stack) {
		return IC2Wiretype.IC2_TYPES[stack.getMetadata()];
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {

		tooltip.add(I18n.format(IndustrialWires.MODID + ".desc.wireLength", getLength(stack)));
		int transferRate = IC2Wiretype.IC2_TYPES[stack.getMetadata()].getTransferRate();
		tooltip.add(I18n.format(IndustrialWires.MODID + ".tooltip.transfer_rate", transferRate));
		tooltip.add(I18n.format(IndustrialWires.MODID + ".tooltip.input_rate", transferRate / 8));
		if (stack.getTagCompound() != null && stack.getTagCompound().hasKey("linkingPos")) {
			int[] link = stack.getTagCompound().getIntArray("linkingPos");
			if (link.length > 3) {
				tooltip.add(I18n.format(Lib.DESC_INFO + "attachedToDim", link[1], link[2], link[3], link[0]));
			}
		}
		tooltip.add(I18n.format(IndustrialWires.MODID + ".desc.recipe"));
	}

	//mostly copied from IE
	@Nonnull
	@Override
	public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if (!world.isRemote && !stack.isEmpty()) {
			if (stack.getCount() > 1) {
				player.sendMessage(new TextComponentTranslation(IndustrialWires.MODID + ".chat.stackSize"));
				return EnumActionResult.FAIL;
			}
			TileEntity tileEntity = world.getTileEntity(pos);
			if (tileEntity instanceof IImmersiveConnectable && ((IImmersiveConnectable) tileEntity).canConnect()) {
				TargetingInfo target = new TargetingInfo(side, hitX, hitY, hitZ);
				WireType wire = getWireType(stack);
				BlockPos masterPos = ((IImmersiveConnectable) tileEntity).getConnectionMaster(wire, target);
				tileEntity = world.getTileEntity(masterPos);
				if (!(tileEntity instanceof IImmersiveConnectable) || !((IImmersiveConnectable) tileEntity).canConnect()) {
					return EnumActionResult.PASS;
				}

				if (!((IImmersiveConnectable) tileEntity).canConnectCable(wire, target)) {
					player.sendMessage(new TextComponentTranslation(Lib.CHAT_WARN + "wrongCable"));
					return EnumActionResult.FAIL;
				}

				if (!ItemNBTHelper.hasKey(stack, "linkingPos")) {
					ItemNBTHelper.setIntArray(stack, "linkingPos", new int[]{world.provider.getDimension(), masterPos.getX(), masterPos.getY(), masterPos.getZ()});
					target.writeToNBT(stack.getTagCompound());
				} else {
					WireType type = getWireType(stack);
					int[] array = ItemNBTHelper.getIntArray(stack, "linkingPos");
					BlockPos linkPos = new BlockPos(array[1], array[2], array[3]);
					TileEntity tileEntityLinkingPos = world.getTileEntity(linkPos);
					int distanceSq = (int) Math.ceil(linkPos.distanceSq(masterPos));
					if (array[0] != world.provider.getDimension()) {
						player.sendMessage(new TextComponentTranslation(Lib.CHAT_WARN + "wrongDimension"));
					} else if (linkPos.equals(masterPos)) {
						player.sendMessage(new TextComponentTranslation(Lib.CHAT_WARN + "sameConnection"));
					} else if (distanceSq > (type.getMaxLength() * type.getMaxLength())) {
						player.sendMessage(new TextComponentTranslation(Lib.CHAT_WARN + "tooFar"));
					} else if (!(tileEntityLinkingPos instanceof IImmersiveConnectable) || !((IImmersiveConnectable) tileEntityLinkingPos).canConnectCable(type, TargetingInfo.readFromNBT(stack.getTagCompound()))) {
						player.sendMessage(new TextComponentTranslation(Lib.CHAT_WARN + "invalidPoint"));
					} else {
						IImmersiveConnectable nodeHere = (IImmersiveConnectable) tileEntity;
						IImmersiveConnectable nodeLink = (IImmersiveConnectable) tileEntityLinkingPos;
						boolean connectionExists = false;
						Set<Connection> outputs = ImmersiveNetHandler.INSTANCE.getConnections(world, Utils.toCC(nodeHere));
						if (outputs != null) {
							for (Connection con : outputs) {
								if (con.end.equals(Utils.toCC(nodeLink))) {
									connectionExists = true;
								}
							}
						}
						if (connectionExists) {
							player.sendMessage(new TextComponentTranslation(Lib.CHAT_WARN + "connectionExists"));
						} else {
							Vec3d rtOff0 = nodeHere.getRaytraceOffset(nodeLink).addVector(masterPos.getX(), masterPos.getY(), masterPos.getZ());
							Vec3d rtOff1 = nodeLink.getRaytraceOffset(nodeHere).addVector(linkPos.getX(), linkPos.getY(), linkPos.getZ());
							Set<BlockPos> ignore = new HashSet<>();
							ignore.addAll(nodeHere.getIgnored(nodeLink));
							ignore.addAll(nodeLink.getIgnored(nodeHere));
							boolean canSee = Utils.rayTraceForFirst(rtOff0, rtOff1, world, ignore) == null;
							if (canSee) {
								int lengthOnStack = getLength(stack);
								int length = (int) Math.sqrt(distanceSq);
								if (length <= lengthOnStack) {
									TargetingInfo targetLink = TargetingInfo.readFromNBT(stack.getTagCompound());
									ImmersiveNetHandler.INSTANCE.addConnection(world, Utils.toCC(nodeHere), Utils.toCC(nodeLink), length, type);

									nodeHere.connectCable(type, target, nodeLink);
									nodeLink.connectCable(type, targetLink, nodeHere);
									IESaveData.setDirty(world.provider.getDimension());
									Utils.unlockIEAdvancement(player, "main/connect_wire");

									if (!player.capabilities.isCreativeMode) {
										if (length < lengthOnStack) {
											setLength(stack, lengthOnStack - length);
										} else {
											player.setHeldItem(hand, ItemStack.EMPTY);
										}
									}
									((TileEntity) nodeHere).markDirty();
									world.addBlockEvent(masterPos, ((TileEntity) nodeHere).getBlockType(), -1, 0);
									IBlockState state = world.getBlockState(masterPos);
									world.notifyBlockUpdate(masterPos, state, state, 3);
									((TileEntity) nodeLink).markDirty();
									world.addBlockEvent(linkPos, ((TileEntity) nodeLink).getBlockType(), -1, 0);
									state = world.getBlockState(linkPos);
									world.notifyBlockUpdate(linkPos, state, state, 3);
								} else {
									player.sendMessage(new TextComponentTranslation(IndustrialWires.MODID + ".chat.tooLong"));
								}
							} else {
								player.sendMessage(new TextComponentTranslation(Lib.CHAT_WARN + "cantSee"));
							}
						}
					}
					ItemNBTHelper.remove(stack, "linkingPos");
					ItemNBTHelper.remove(stack, "side");
					ItemNBTHelper.remove(stack, "hitX");
					ItemNBTHelper.remove(stack, "hitY");
					ItemNBTHelper.remove(stack, "hitZ");
				}
				return EnumActionResult.SUCCESS;
			}
		}
		return EnumActionResult.PASS;
	}

	public static void setLength(ItemStack i, int blocks) {
		i.setTagInfo(lengthKey, new NBTTagInt(blocks));
	}

	public static int getLength(ItemStack i) {
		if (i.getTagCompound() == null) {
			setLength(i, 4);
		}
		return i.getTagCompound().getInteger(lengthKey)*i.getCount();
	}

	public static int getMaxWireLength(ItemStack i) {
		return IWConfig.maxLengthOnCoil[i.getItemDamage()];
	}
}

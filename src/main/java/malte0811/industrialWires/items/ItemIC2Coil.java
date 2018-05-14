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
package malte0811.industrialWires.items;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.IWireCoil;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.common.IESaveData;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBreakerSwitch;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityRedstoneBreaker;
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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemIC2Coil extends Item implements IWireCoil {
	public final static String[] subNames = {
			"tin", "copper", "gold", "hv", "glass", "tin_ins", "copper_ins", "gold_ins"
	};
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
	public IC2Wiretype getWireType(ItemStack stack) {
		return IC2Wiretype.ALL[stack.getMetadata()];
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {

		tooltip.add(I18n.format(IndustrialWires.MODID + ".desc.wireLength", getLength(stack)));
		IC2Wiretype wireType = IC2Wiretype.ALL[stack.getMetadata()];
		int transferRate = wireType.getTransferRate();
		tooltip.add(I18n.format(IndustrialWires.MODID + ".tooltip.transfer_rate", transferRate));
		tooltip.add(I18n.format(IndustrialWires.MODID + ".tooltip.input_rate",
				transferRate / wireType.getFactor()));
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
		if (!stack.isEmpty()) {
			if (stack.getCount() > 1) {
				player.sendMessage(new TextComponentTranslation(IndustrialWires.MODID + ".chat.stackSize"));
				return EnumActionResult.FAIL;
			}
			TileEntity tileEntity = world.getTileEntity(pos);
			if (tileEntity instanceof IImmersiveConnectable && ((IImmersiveConnectable) tileEntity).canConnect()) {
				TargetingInfo target = new TargetingInfo(side, hitX, hitY, hitZ);
				IC2Wiretype wire = getWireType(stack);
				BlockPos masterPos = ((IImmersiveConnectable) tileEntity).getConnectionMaster(wire, target);
				Vec3i offset = pos.subtract(masterPos);
				tileEntity = world.getTileEntity(masterPos);
				if (!(tileEntity instanceof IImmersiveConnectable) || !((IImmersiveConnectable) tileEntity).canConnect())
					return EnumActionResult.PASS;

				boolean canConnect = ((IImmersiveConnectable) tileEntity).canConnectCable(wire, target, offset);
				if (canConnect&&tileEntity instanceof TileEntityBreakerSwitch) {
					canConnect = !wire.isHV()||tileEntity instanceof TileEntityRedstoneBreaker;
				}
				if (!canConnect) {
					if (!world.isRemote)
						player.sendMessage(new TextComponentTranslation(Lib.CHAT_WARN + "wrongCable"));
					return EnumActionResult.FAIL;
				}

				if (!world.isRemote)
					if (!ItemNBTHelper.hasKey(stack, "linkingPos")) {
						ItemNBTHelper.setIntArray(stack, "linkingPos", new int[]{world.provider.getDimension(), masterPos.getX(), masterPos.getY(), masterPos.getZ(),
								offset.getX(), offset.getY(), offset.getZ()});
						NBTTagCompound targetNbt = new NBTTagCompound();
						target.writeToNBT(targetNbt);
						ItemNBTHelper.setTagCompound(stack, "targettingInfo", targetNbt);
					} else {
						int[] array = ItemNBTHelper.getIntArray(stack, "linkingPos");
						BlockPos linkPos = new BlockPos(array[1], array[2], array[3]);
						Vec3i offsetLink = BlockPos.NULL_VECTOR;
						if (array.length == 7)
							offsetLink = new Vec3i(array[4], array[5], array[6]);
						TileEntity tileEntityLinkingPos = world.getTileEntity(linkPos);
						int distanceSq = (int) Math.ceil(linkPos.distanceSq(masterPos));
						if (array[0] != world.provider.getDimension()) {
							player.sendMessage(new TextComponentTranslation(Lib.CHAT_WARN + "wrongDimension"));
						} else if (linkPos.equals(masterPos)) {
							player.sendMessage(new TextComponentTranslation(Lib.CHAT_WARN + "sameConnection"));
						} else if (distanceSq > (wire.getMaxLength() * wire.getMaxLength())) {
							player.sendMessage(new TextComponentTranslation(Lib.CHAT_WARN + "tooFar"));
						} else {
							TargetingInfo targetLink = TargetingInfo.readFromNBT(ItemNBTHelper.getTagCompound(stack, "targettingInfo"));
							if (!(tileEntityLinkingPos instanceof IImmersiveConnectable) || !((IImmersiveConnectable) tileEntityLinkingPos).canConnectCable(wire, targetLink, offset)) {
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
									Set<BlockPos> ignore = new HashSet<>();
									ignore.addAll(nodeHere.getIgnored(nodeLink));
									ignore.addAll(nodeLink.getIgnored(nodeHere));
									Connection tmpConn = new Connection(Utils.toCC(nodeHere), Utils.toCC(nodeLink), wire,
											(int) Math.sqrt(distanceSq));
									Vec3d start = nodeHere.getConnectionOffset(tmpConn, target, pos.subtract(masterPos)).addVector(tileEntity.getPos().getX(),
											tileEntity.getPos().getY(), tileEntity.getPos().getZ());
									Vec3d end = nodeLink.getConnectionOffset(tmpConn, targetLink, offsetLink).addVector(tileEntityLinkingPos.getPos().getX(),
											tileEntityLinkingPos.getPos().getY(), tileEntityLinkingPos.getPos().getZ());
									boolean canSee = ApiUtils.raytraceAlongCatenary(tmpConn, (p) -> {
										if (ignore.contains(p.getLeft())) {
											return false;
										}
										IBlockState state = world.getBlockState(p.getLeft());
										return ApiUtils.preventsConnection(world, p.getLeft(), state, p.getMiddle(), p.getRight());
									}, (p) -> {
									}, start, end);
									if (canSee) {
										int lengthOnStack = getLength(stack);
										int length = (int) Math.sqrt(distanceSq);
										if (length <= lengthOnStack) {
											Connection conn = ImmersiveNetHandler.INSTANCE.addAndGetConnection(world, Utils.toCC(nodeHere), Utils.toCC(nodeLink),
													(int) Math.sqrt(distanceSq), wire);


											nodeHere.connectCable(wire, target, nodeLink, offset);
											nodeLink.connectCable(wire, targetLink, nodeHere, offsetLink);
											ImmersiveNetHandler.INSTANCE.addBlockData(world, conn);
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
											player.sendMessage(new TextComponentTranslation(Lib.CHAT_WARN + "tooFar"));
										}
									} else {
										player.sendMessage(new TextComponentTranslation(Lib.CHAT_WARN + "cantSee"));
									}
								}
							}
						}
						ItemNBTHelper.remove(stack, "linkingPos");
						ItemNBTHelper.remove(stack, "targettingInfo");
					}
				return EnumActionResult.SUCCESS;
			}
			return EnumActionResult.PASS;
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
		return ItemNBTHelper.getInt(i, lengthKey)*i.getCount();
	}

	public static int getMaxWireLength(ItemStack i) {
		return IWConfig.maxLengthOnCoil[i.getItemDamage()%5];
	}
}

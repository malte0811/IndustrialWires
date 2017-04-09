package malte0811.industrialWires.blocks;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public interface INetGUI {
	void onChange(NBTTagCompound nbt, EntityPlayer p);
}

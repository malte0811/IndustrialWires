package malte0811.industrialWires.blocks;

import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxConnection;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxProvider;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxReceiver;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.energy.IEnergyStorage;

public class EnergyAdapter implements IEnergyStorage {
	/**
	 * 3 different copies of the same thing, the TE this adapter is mirroring.
	 * rec and prov are null if the TE does not implement them
	 */
	IFluxConnection tile;
	IFluxReceiver rec;
	IFluxProvider prov;
	
	EnumFacing dir;
	public EnergyAdapter(IFluxConnection te, EnumFacing f) {
		tile = te;
		dir = f;
		if (te instanceof IFluxReceiver) {
			rec = (IFluxReceiver) te;
		}
		if (te instanceof IFluxProvider) {
			prov = (IFluxProvider) te;
		}
	}
	
	@Override
	public int receiveEnergy(int maxReceive, boolean simulate) {
		if (rec==null) {
			return 0;
		} else {
			return rec.receiveEnergy(dir, maxReceive, simulate);
		}
	}

	@Override
	public int extractEnergy(int maxExtract, boolean simulate) {
		if (prov==null) {
			return 0;
		} else {
			return prov.extractEnergy(dir, maxExtract, simulate);
		}
	}

	@Override
	public int getEnergyStored() {
		if (prov!=null) {
			return prov.getEnergyStored(dir);
		} else if (rec!=null) {
			return rec.getEnergyStored(dir);
		} else {
			return 0;
		}
	}

	@Override
	public int getMaxEnergyStored() {
		if (prov!=null) {
			return prov.getMaxEnergyStored(dir);
		} else if (rec!=null) {
			return rec.getMaxEnergyStored(dir);
		} else {
			return 0;
		}
	}

	@Override
	public boolean canExtract() {
		return prov!=null;
	}

	@Override
	public boolean canReceive() {
		return rec!=null;
	}
}

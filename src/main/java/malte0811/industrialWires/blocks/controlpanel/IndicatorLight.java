package malte0811.industrialWires.blocks.controlpanel;

import blusunrize.immersiveengineering.common.util.IELogger;
import com.google.common.collect.ImmutableList;
import malte0811.industrialWires.client.RawQuad;
import malte0811.industrialWires.client.panelmodel.PanelUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.ModelLoader;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class IndicatorLight extends PanelComponent {
	int rsInputId;
	int rsInputChannel;
	int colorA;
	byte rsInput;
	public IndicatorLight() {
		super("indicator_light");
	}
	public IndicatorLight(int rsId, int rsChannel, int color) {
		this();
		colorA = color;
		rsInputChannel = rsChannel;
		rsInputId = rsId;
	}


	@Override
	protected void writeCustomNBT(NBTTagCompound nbt) {
		nbt.setInteger("rsId", rsInputId);
		nbt.setInteger("rsChannel", rsInputChannel);
		nbt.setInteger("color", colorA);
		nbt.setInteger("rsInput", rsInput);
	}

	@Override
	protected void readCustomNBT(NBTTagCompound nbt) {
		rsInputId = nbt.getInteger("rsId");
		rsInputChannel = nbt.getInteger("rsChannel");
		colorA = nbt.getInteger("color");
		rsInput = nbt.getByte("rsInput");
	}

	private static final float size = .0625F;
	private static final float antiZOffset = .001F;
	@Override
	public List<RawQuad> getQuads() {
		float[] color = new float[4];
		color[3] = 1;
		for (int i = 0;i<3;i++) {
			color[i] = ((this.colorA>>(8*(2-i)))&255)/255F*(rsInput+15F)/30F;
		}
		List<RawQuad> ret = new ArrayList<>(1);
		PanelUtils.addColoredQuad(ret, new Vector3f(), new Vector3f(0, antiZOffset, size), new Vector3f(size, antiZOffset, size), new Vector3f(size, antiZOffset, 0), EnumFacing.UP, color);
		return ret;
	}

	@Nonnull
	@Override
	public PanelComponent copyOf() {
		IndicatorLight ret = new IndicatorLight(rsInputId, rsInputChannel, colorA);
		ret.rsInput = rsInput;
		ret.setX(x);
		ret.setY(y);
		ret.panelHeight = panelHeight;
		return ret;
	}

	@Override
	public AxisAlignedBB getBlockRelativeAABB() {
		return null;
	}

	@Override
	public boolean interactWith(Vec3d hitRelative, TileEntityPanel tile) {
		return false;
	}

	@Override
	public void update(TileEntityPanel tile) {

	}

	@Nullable
	@Override
	public Consumer<byte[]> getRSInputHandler(int id, TileEntityPanel panel) {
		if (id==rsInputId) {
			return (input)->{
				if (input[rsInputChannel]!=rsInput) {
					rsInput = input[rsInputChannel];
					panel.markDirty();
					panel.triggerRenderUpdate();
				}
			};
		}
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		IndicatorLight that = (IndicatorLight) o;

		if (colorA != that.colorA) return false;
		return rsInput == that.rsInput;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + colorA;
		result = 31 * result + (int) rsInput;
		return result;
	}
}
//TODO optimize RS net in IE, updates multiple times per tick. 0-length pulses?
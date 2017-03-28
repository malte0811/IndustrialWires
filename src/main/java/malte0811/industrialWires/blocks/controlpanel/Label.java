package malte0811.industrialWires.blocks.controlpanel;

import malte0811.industrialWires.client.RawQuad;
import malte0811.industrialWires.client.panelmodel.RawModelFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;
import java.util.List;

public class Label extends PanelComponent {
	private static final ResourceLocation font = new ResourceLocation("minecraft", "textures/font/ascii.png");
	String text;

	public Label(String text) {
		this();
		this.text = text;
	}
	public Label() {
		super("label");
	}

	@Override
	protected void writeCustomNBT(NBTTagCompound nbt) {
		nbt.setString("text", text);
	}

	@Override
	protected void readCustomNBT(NBTTagCompound nbt) {
		text = nbt.getString("text");
	}

	@Override
	public List<RawQuad> getQuads() {
		RawModelFontRenderer render = fontRenderer();
		render.drawString(text, 0, 0, 0xff0000);
		return render.build();
	}

	@Nonnull
	@Override
	public Label copyOf() {
		Label ret = new Label(text);
		ret.setX(x);
		ret.setY(y);
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

	private RawModelFontRenderer fontRenderer() {
		return new RawModelFontRenderer(Minecraft.getMinecraft().gameSettings, font, Minecraft.getMinecraft().getTextureManager(),
				false,  .01F);
	}

}
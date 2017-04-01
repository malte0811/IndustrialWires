package malte0811.industrialWires.client.panelmodel;

import blusunrize.immersiveengineering.common.util.IELogger;
import com.google.common.collect.ImmutableList;
import malte0811.industrialWires.client.RawQuad;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.util.vector.Vector3f;

public class RawModelFontRenderer extends FontRenderer {
	float[] colorA = new float[4];
	private ImmutableList.Builder<RawQuad> builder = ImmutableList.builder();
	private final Vector3f normal = new Vector3f(0, 1, 0);
	private final float scale;

	private TextureAtlasSprite sprite;

	public RawModelFontRenderer(GameSettings settings, ResourceLocation font, TextureManager manager, boolean isUnicode, float scale) {
		super(settings, font, manager, isUnicode);
		manager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		this.scale = scale/(9*16);
		onResourceManagerReload(null);
	}

	@Override
	protected float renderDefaultChar(int pos, boolean italic) {
		float x = (pos % 16);
		float y = (pos / 16);
		float w = charWidth[pos] - 1.01f;
		float h = FONT_HEIGHT - 1.01f;
		float wt = w  / 128f*16;
		float ht = h  / 128f*16;
		float h0 = .01F;
		Vector3f v0 = new Vector3f(posX, h0, posY);
		v0.scale(scale);
		Vector3f v1 = new Vector3f(posX, h0, posY+h);
		v1.scale(scale);
		Vector3f v2 = new Vector3f(posX+w, h0, posY+h);
		v2.scale(scale);
		Vector3f v3 = new Vector3f(posX+w, h0, posY);
		v3.scale(scale);
		builder.add(new RawQuad(v0, v1, v2, v3,
				EnumFacing.UP, sprite, new float[]{1, 0, 0, 1}, new Vector3f(0, 1, 0),
				new float[]{x, y, x+wt, y+ht}));
		return charWidth[pos];
	}
	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) {
		super.onResourceManagerReload(resourceManager);
		String p = locationFontTexture.getResourcePath();
		if (p.startsWith("textures/")) p = p.substring("textures/".length(), p.length());
		if (p.endsWith(".png")) p = p.substring(0, p.length() - ".png".length());
		String f = locationFontTexture.getResourceDomain() + ":" + p;
		sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(f);
	}

	@Override
	protected void doDraw(float shift) {
		posX += (int) shift;
	}

	@Override
	protected void setColor(float r, float g, float b, float a) {
		colorA[0] = r;
		colorA[1] = g;
		colorA[2] = b;
		colorA[3] = 1;
	}

	@Override
	public void enableAlpha() {
	}

	public ImmutableList<RawQuad> build() {
		ImmutableList<RawQuad> ret = builder.build();
		builder = ImmutableList.builder();
		return ret;
	}


}
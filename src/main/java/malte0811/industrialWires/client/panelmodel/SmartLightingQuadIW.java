package malte0811.industrialWires.client.panelmodel;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement.EnumType;
import net.minecraft.client.renderer.vertex.VertexFormatElement.EnumUsage;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.client.model.pipeline.QuadGatheringTransformer;
import net.minecraftforge.client.model.pipeline.VertexLighterFlat;

import java.lang.reflect.Field;

//Yes, this is copied from the IE version. But I wrote that one, so...
public class SmartLightingQuadIW extends BakedQuad {
	private static Field parent;

	static {
		try {
			parent = QuadGatheringTransformer.class.getDeclaredField("parent");
			parent.setAccessible(true);
		} catch (Exception x) {
			x.printStackTrace();
		}
	}

	private int brightness;

	public SmartLightingQuadIW(int[] vertexDataIn, int tintIndexIn, EnumFacing faceIn, TextureAtlasSprite spriteIn, VertexFormat format, int brightness) {
		super(vertexDataIn, tintIndexIn, faceIn, spriteIn, false, format);
		this.brightness = brightness;
	}

	public SmartLightingQuadIW(BakedQuad ret, int light) {
		super(ret.getVertexData(), ret.getTintIndex(), ret.getFace(), ret.getSprite(), false, ret.getFormat());
		this.brightness = light;
	}

	@Override
	public void pipe(IVertexConsumer consumer) {
		if (consumer instanceof VertexLighterFlat) {
			try {
				consumer = (IVertexConsumer) parent.get(consumer);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		consumer.setQuadOrientation(this.getFace());
		if (this.hasTintIndex()) {
			consumer.setQuadTint(this.getTintIndex());
		}
		float[] data = new float[4];
		VertexFormat format = consumer.getVertexFormat();
		int count = format.getElementCount();
		int[] eMap = LightUtil.mapFormats(format, DefaultVertexFormats.ITEM);
		int itemCount = DefaultVertexFormats.ITEM.getElementCount();
		eMap[eMap.length - 1] = 2;
		for (int v = 0; v < 4; v++) {
			for (int e = 0; e < count; e++) {
				if (eMap[e] != itemCount) {
					if (format.getElement(e).getUsage() == EnumUsage.UV && format.getElement(e).getType() == EnumType.SHORT)//lightmap is UV with 2 shorts
					{
						data[0] = ((float) ((brightness >> 0x04) & 0xF) * 0x20) / 0xFFFF;
						data[1] = ((float) ((brightness >> 0x14) & 0xF) * 0x20) / 0xFFFF;
					} else {
						LightUtil.unpack(this.getVertexData(), data, DefaultVertexFormats.ITEM, v, eMap[e]);
					}
					consumer.put(e, data);
				} else {
					consumer.put(e, 0);
				}
			}
		}
	}
}
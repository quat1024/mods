package agency.highlysuspect.quatlib.craftful.client.model;

import agency.highlysuspect.quatlib.craftless.util.QuatUtil;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;

public class SortedUvBounds {
	float minU, maxU, minV, maxV;
	
	public <Q> void initFrom(IQuadView<Q> quad) {
		float x0 = quad.getU(0);
		float x1 = quad.getU(1);
		float x2 = quad.getU(2);
		float u3 = quad.getU(3);
		minU = Math.min(Math.min(x0, x1), Math.min(x2, u3));
		maxU = Math.max(Math.max(x0, x1), Math.max(x2, u3));
		float v0 = quad.getV(0);
		float v1 = quad.getV(1);
		float v2 = quad.getV(2);
		float v3 = quad.getV(3);
		minV = Math.min(Math.min(v0, v1), Math.min(v2, v3));
		maxV = Math.max(Math.max(v0, v1), Math.max(v2, v3));
	}
	
	public boolean displaysSprite(TextureAtlasSprite sprite) {
		return sprite.getU0() <= minU && sprite.getU1() >= maxU && sprite.getV0() <= minV && sprite.getV1() >= maxV;
	}
	
	//move texture coordinates from the "from" texture to corresponding spot on the "to" texture
	public <Q> void retexture(IQuadView<Q> quad, TextureAtlasSprite from, TextureAtlasSprite to) {
		doRetexture(quad, from, to.getU0(), to.getU1(), to.getV0(), to.getV1());
	}
	
	//move texture coordinates from the "from" texture to corresponding spot on the unit square
	//(format expected by frapi spriteBake function w/ BAKE_NORMALIZED)
	public <Q> void prepareNormalizedSpriteBake(IQuadView<Q> quad, TextureAtlasSprite from) {
		doRetexture(quad, from, 0, 1, 0, 1);
	}
	
	private <Q> void doRetexture(IQuadView<Q> quad, TextureAtlasSprite from, float toMinU, float toMaxU, float toMinV, float toMaxV) {
		float remappedMinU = QuatUtil.rangeRemap(minU, from.getU0(), from.getU1(), toMinU, toMaxU);
		float remappedMaxU = QuatUtil.rangeRemap(maxU, from.getU0(), from.getU1(), toMinU, toMaxU);
		float remappedMinV = QuatUtil.rangeRemap(minV, from.getV0(), from.getV1(), toMinV, toMaxV);
		float remappedMaxV = QuatUtil.rangeRemap(maxV, from.getV0(), from.getV1(), toMinV, toMaxV);
		
		for(int i = 0; i < 4; i++) {
			quad.setUv(i,
				Mth.equal(quad.getU(i), minU) ? remappedMinU : remappedMaxU,
				Mth.equal(quad.getV(i), minV) ? remappedMinV : remappedMaxV);
		}
	}
}

package agency.highlysuspect.quatlib.craftful.client.model;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.IQuadTransformer;

public class BakedQuadView implements IQuadView<BakedQuad> {
	public BakedQuadView(boolean swapRedAndBlue) {
		this.swapRedAndBlue = swapRedAndBlue;
	}
	
	private final boolean swapRedAndBlue;
	
	private BakedQuad q;
	
	private static final int vertexStride = IQuadTransformer.STRIDE;
	private static final int vertexColorOffset = IQuadTransformer.COLOR;
	private static final int vertexUOffset = IQuadTransformer.UV0;
	private static final int vertexVOffset = vertexUOffset + 1;
	//the v is stored directly after the u.
	//n.b. In vanilla there's something called "uv2",
	//it's used for lightmap coords, nothing to do with texture uv
	
	@Override
	public void fromVanilla(BakedQuad quad, Direction cullFace) {
		q = quad;
	}
	
	@Override
	public BakedQuad keep() {
		return q;
	}
	
	@Override
	public void enableWriting() {
		//work on a copy of the quad
		int[] vertsCopy = new int[q.getVertices().length];
		System.arraycopy(q.getVertices(), 0, vertsCopy, 0, vertsCopy.length);
		q = new BakedQuad(vertsCopy, q.getTintIndex(), q.getDirection(), q.getSprite(), q.isShade());
	}
	
	@Override
	public int getTintIndex() {
		return q.getTintIndex();
	}
	
	@Override
	public void setTintAllVerts(int tint) {
		int[] verts = q.getVertices();
		
		if(swapRedAndBlue) {
			tint = ((tint & 0x00FF0000) >> 16) | ((tint & 0x000000FF) << 16) | (tint & 0xFF00FF00);
		}
		
		verts[                   vertexColorOffset] = tint;
		verts[    vertexStride + vertexColorOffset] = tint;
		verts[2 * vertexStride + vertexColorOffset] = tint;
		verts[3 * vertexStride + vertexColorOffset] = tint;
	}
	
	@Override
	public float getU(int vert) {
		return Float.intBitsToFloat(q.getVertices()[vert * vertexStride + vertexUOffset]);
	}
	
	@Override
	public float getV(int vert) {
		return Float.intBitsToFloat(q.getVertices()[vert * vertexStride + vertexVOffset]);
	}
	
	@Override
	public void setUv(int vert, float u, float v) {
		int[] verts = q.getVertices();
		verts[vert * vertexStride + vertexUOffset] = Float.floatToRawIntBits(u);
		verts[vert * vertexStride + vertexVOffset] = Float.floatToRawIntBits(v);
	}
	
	@Override
	public void retexture(SortedUvBounds myBounds, TextureAtlasSprite from, TextureAtlasSprite to) {
		myBounds.retexture(this, from, to);
	}
}

package agency.highlysuspect.quatlib.craftful.client.model;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

/**
 * Utilities for reading and writing to BakedQuads and/or QuadEmitters
 * @param <Q> either BakedQuad or QuadEmitter
 */
public interface IQuadView<Q> {
	//swap out the quad being examined by this view (saves allocations over allocating a new iquadview)
	void fromVanilla(BakedQuad quad, Direction cullFace);
	
	Q keep();
	
	//Forge works with actual bakedquad objects, this is where the quad would be copied
	//to avoid messing with the quad in the model it's sourced from
	void enableWriting();
	
	int getTintIndex();
	void setTintAllVerts(int tint);
	
	float getU(int vert);
	float getV(int vert);
	void setUv(int vert, float u, float v);
	
	//frapi has a spriteBake function which might be better to use?
	//on forge it's fine to just punt the uv coords around manually
	void retexture(SortedUvBounds myBounds, TextureAtlasSprite from, TextureAtlasSprite to);
}

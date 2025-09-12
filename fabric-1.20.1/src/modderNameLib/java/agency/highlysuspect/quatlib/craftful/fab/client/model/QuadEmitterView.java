package agency.highlysuspect.quatlib.craftful.fab.client.model;

import agency.highlysuspect.quatlib.craftful.client.model.IQuadView;
import agency.highlysuspect.quatlib.craftful.client.model.SortedUvBounds;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

public class QuadEmitterView implements IQuadView<QuadEmitter> {
	public QuadEmitterView(RenderMaterial material, MeshBuilder meshBuilder) {
		this.material = material;
		this.emitter = meshBuilder.getEmitter();
	}
	
	public final RenderMaterial material;
	public final QuadEmitter emitter;
	
	@Override
	public void fromVanilla(BakedQuad quad, Direction cullFace) {
		emitter.fromVanilla(quad, material, cullFace);
	}
	
	@Override
	public QuadEmitter keep() {
		return emitter;
	}
	
	@Override
	public void enableWriting() {
		//QuadEmitter#fromVanilla already makes a copy of all relevant data.
	}
	
	@Override
	public int getTintIndex() {
		return emitter.colorIndex();
	}
	
	@Override
	public void setTintAllVerts(int tint) {
		emitter.color(tint, tint, tint, tint);
	}
	
	@Override
	public float getU(int vert) {
		return emitter.u(vert);
	}
	
	@Override
	public float getV(int vert) {
		return emitter.v(vert);
	}
	
	@Override
	public void setUv(int vert, float u, float v) {
		emitter.uv(vert, u, v);
	}
	
	@Override
	public void retexture(SortedUvBounds myBounds, TextureAtlasSprite from, TextureAtlasSprite to) {
		myBounds.prepareNormalizedSpriteBake(this, from);
		emitter.spriteBake(to, MutableQuadView.BAKE_NORMALIZED);
	}
}

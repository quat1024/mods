package agency.highlysuspect.packages.craftful.fab.client.model;

import agency.highlysuspect.packages.craftful.client.PackageModelBakery;
import agency.highlysuspect.packages.craftful.client.PackageRetexturizer;
import agency.highlysuspect.quatlib.craftful.fab.client.model.QuadEmitterView;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class FrapiMeshModelBakery extends PackageRetexturizer<QuadEmitter> implements PackageModelBakery<Mesh> {
	public FrapiMeshModelBakery(BakedModel baseModel, BlockState defaultState, TextureAtlasSprite specialFrameSprite, TextureAtlasSprite specialInnerSprite) {
		super(baseModel, defaultState, specialFrameSprite, specialInnerSprite);
	}
	
	@Override
	public @Nullable TextureAtlasSprite getParticleIcon(@Nullable Block b) {
		if(b == null) return null;
		else return Minecraft.getInstance().getBlockRenderer().getBlockModel(b.defaultBlockState()).getParticleIcon();
	}
	
	public Mesh bake(@Nullable Object cacheKey, @Nullable DyeColor faceColor, @Nullable Block frameBlock, @Nullable Block innerBlock) {
		Renderer renderer = RendererAccess.INSTANCE.getRenderer();
		Objects.requireNonNull(renderer, "A fabric-renderer-api-v1 implementation is required to use Packages");
		
		MeshBuilder meshBuilder = renderer.meshBuilder();
		super.process(new QuadEmitterView(renderer.materialFinder().find(), meshBuilder), faceColor, frameBlock, innerBlock, QuadEmitter::emit);
		return meshBuilder.build();
	}
}

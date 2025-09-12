package agency.highlysuspect.packages.craftful.fab.client.model;

import agency.highlysuspect.packages.craftful.Packages;
import agency.highlysuspect.packages.craftful.client.PackageModelBakery;
import agency.highlysuspect.packages.craftful.client.PackagesClient;
import agency.highlysuspect.packages.craftful.client.PropsClient;
import agency.highlysuspect.packages.craftful.content.PLatches;
import agency.highlysuspect.packages.craftful.junk.PackageMakerStyle;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This class is basically identical to FrapiMeshPackageModel.
 * It uses PackageMakerStyle instead of PackageStyle, and uses a different style of choosing the model from the item stack.
 */
public class FrapiMeshPackageMakerModel implements UnbakedModel {
	private static final ResourceLocation BLOCK_MODEL_ID = Packages.rl("block/package_maker");
	
	@Override
	public Collection<ResourceLocation> getDependencies() {
		return List.of(BLOCK_MODEL_ID);
	}
	
	@Override
	public void resolveParents(Function<ResourceLocation, UnbakedModel> function) {
		function.apply(BLOCK_MODEL_ID).resolveParents(function);
	}
	
	@Nullable
	@Override
	public BakedModel bake(ModelBaker bakery, Function<Material, TextureAtlasSprite> textureGetter, ModelState modelState, ResourceLocation modelId) {
		//vanilla model
		BakedModel base = bakery.bake(BLOCK_MODEL_ID, modelState);
		
		//texture bakery
		PackageModelBakery<Mesh> pmb = new FrapiMeshModelBakery(
			base,
			PLatches.Blocks.PACKAGE_MAKER.get().defaultBlockState(),
			textureGetter.apply(PackageModelBakery.SPECIAL_FRAME),
			textureGetter.apply(PackageModelBakery.SPECIAL_INNER)
		);
		if(PackagesClient.inst().config.get(PropsClient.CACHE_MESHES)) pmb = pmb.withCache();
		
		return new Baked(base, pmb);
	}
	
	private static class Baked extends ForwardingBakedModel {
		public Baked(BakedModel base, PackageModelBakery<Mesh> bakery) {
			this.wrapped = base;
			this.bakery = bakery;
		}
		
		private final PackageModelBakery<Mesh> bakery;
		
		@Override
		public boolean isVanillaAdapter() {
			return false;
		}
		
		@Override
		public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
			if(blockView.getBlockEntityRenderData(pos) instanceof PackageMakerStyle style) {
				context.meshConsumer().accept(bakery.bake(style));
			}
		}
		
		@Override
		public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
			//filters out all the special quads
			context.meshConsumer().accept(bakery.bake((PackageMakerStyle) null));
		}
	}
}

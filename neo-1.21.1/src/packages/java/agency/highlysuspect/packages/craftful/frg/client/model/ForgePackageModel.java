package agency.highlysuspect.packages.craftful.frg.client.model;

import agency.highlysuspect.packages.craftful.Packages;
import agency.highlysuspect.packages.craftful.block.PackageBlock;
import agency.highlysuspect.packages.craftful.block.PackageBlockEntity;
import agency.highlysuspect.packages.craftful.client.PackageModelBakery;
import agency.highlysuspect.packages.craftful.client.PackagesClient;
import agency.highlysuspect.packages.craftful.client.PropsClient;
import agency.highlysuspect.packages.craftful.content.PLatches;
import agency.highlysuspect.packages.craftful.junk.PackageStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public class ForgePackageModel implements IUnbakedGeometry<ForgePackageModel> {
	protected static final ModelProperty<PackageStyle> STYLE_PROPERTY = new ModelProperty<>();
	
	protected static final ModelProperty<BlockAndTintGetter> BATG_PROPERTY = new ModelProperty<>(); //To support getParticleIcon.
	protected static final ModelProperty<BlockPos> BLOCKPOS_PROPERTY = new ModelProperty<>();//To support getParticleIcon.
	
	protected static final ResourceLocation BLOCK_MODEL_ID = Packages.rl("block/package");
	
	@Override
	public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context) {
		modelGetter.apply(BLOCK_MODEL_ID).resolveParents(modelGetter);
	}
	
	@Override
	public BakedModel bake(IGeometryBakingContext context, ModelBaker bakery, Function<Material, TextureAtlasSprite> textureGetter, ModelState modelState, ItemOverrides overrides) {
		//vanilla model
		BakedModel base = bakery.bake(BLOCK_MODEL_ID, modelState, textureGetter);
		
		//package texture bakery
		PackageModelBakery<List<BakedQuad>> pmb = new BakedQuadPackageModelBakery(
			base,
			PLatches.Blocks.PACKAGE.get().defaultBlockState(),
			textureGetter.apply(PackageModelBakery.SPECIAL_FRAME),
			textureGetter.apply(PackageModelBakery.SPECIAL_INNER)
		);
		if(PackagesClient.inst().config.get(PropsClient.CACHE_MESHES)) pmb = pmb.withCache();
		
		//actual bakedmodel
		return new Baked(base, pmb);
	}
	
	private static class Baked extends BakedModelWrapper<BakedModel> {
		public Baked(BakedModel base, PackageModelBakery<List<BakedQuad>> factory) {
			super(base);
			this.factory = factory;
			this.itemOverrideThing = new WeirderItemOverrideThing(base,
				s -> factory.bake(s.getOrDefault(PackageStyle.DATA_COMPONENT_TYPE, PackageStyle.ERROR_LOL)));
		}
		
		private final PackageModelBakery<List<BakedQuad>> factory;
		private final ItemOverrides itemOverrideThing;
		
		@NotNull
		@Override
		public ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData modelData) {
			if(level.getBlockEntity(pos) instanceof PackageBlockEntity be) {
				return modelData.derive()
					.with(STYLE_PROPERTY, be.getStyle())
					.with(BATG_PROPERTY, level)
					.with(BLOCKPOS_PROPERTY, pos)
					.build();
			}
			return modelData;
		}
		
		@Override
		public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType) {
			PackageStyle style = extraData.get(STYLE_PROPERTY);
			if(style == null) style = PackageStyle.ERROR_LOL;
			return factory.bake(style);
		}
		
		//Nice Forge API for overriding particle textures from your baked model. This is cool!
		//This is implemented on Fabric using a couple of mixins (see "particleslol").
		//Only thing with this API is it's kind of hard to get access to level/pos
		@Override
		public TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
			PackageStyle style = data.get(STYLE_PROPERTY);
			BlockAndTintGetter batg = data.get(BATG_PROPERTY);
			BlockPos pos = data.get(BLOCKPOS_PROPERTY);
			
			if(batg instanceof Level level && pos != null && style != null && !(style.innerBlock() instanceof PackageBlock)) {
				return Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getTexture(style.innerBlock().defaultBlockState(), level, pos);
			} else return super.getParticleIcon(data);
		}
		
		@Override
		public ItemOverrides getOverrides() {
			return itemOverrideThing;
		}
	}
}

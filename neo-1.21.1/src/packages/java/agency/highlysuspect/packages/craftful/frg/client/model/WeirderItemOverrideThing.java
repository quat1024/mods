package agency.highlysuspect.packages.craftful.frg.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public class WeirderItemOverrideThing extends ItemOverrides {
	public WeirderItemOverrideThing(BakedModel base, Function<ItemStack, List<BakedQuad>> baker) {
		this.base = base;
		this.baker = baker;
	}
	
	private final BakedModel base;
	private final Function<ItemStack, List<BakedQuad>> baker;
	
	//The magic method from ItemOverrides
	@Nullable
	@Override
	public BakedModel resolve(BakedModel originalModel, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity player, int idk) {
		return new FixedQuadListModel(base, baker.apply(stack));
	}
	
	private static class FixedQuadListModel extends BakedModelWrapper<BakedModel> {
		public FixedQuadListModel(BakedModel base, List<BakedQuad> quads) {
			super(base);
			this.quads = quads;
		}
		
		private final List<BakedQuad> quads;
		private final List<BakedModel> thisButList = List.of(this);
		
		@Override
		public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
			return quads;
		}
		
		@Override
		public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType) {
			return quads;
		}
		
		@Override
		public ItemOverrides getOverrides() {
			return ItemOverrides.EMPTY;
		}
		
		@Override
		public BakedModel applyTransform(ItemDisplayContext cameraTransformType, PoseStack poseStack, boolean applyLeftHandTransform) {
			//Have to override this or Forge throws away all your work making a nontrivial BakedModelWrapper.
			super.applyTransform(cameraTransformType, poseStack, applyLeftHandTransform);
			return this;
		}
		
		@Override
		public List<BakedModel> getRenderPasses(ItemStack itemStack, boolean fabulous) {
			//This one too.
			return thisButList;
		}
	}
}

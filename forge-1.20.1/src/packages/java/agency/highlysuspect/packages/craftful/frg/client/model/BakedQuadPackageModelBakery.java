package agency.highlysuspect.packages.craftful.frg.client.model;

import agency.highlysuspect.packages.craftful.client.PackageModelBakery;
import agency.highlysuspect.packages.craftful.client.PackageRetexturizer;
import agency.highlysuspect.packages.craftful.client.PackagesClient;
import agency.highlysuspect.packages.craftful.client.PropsClient;
import agency.highlysuspect.quatlib.craftful.frg.client.model.BakedQuadView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BakedQuadPackageModelBakery extends PackageRetexturizer<BakedQuad> implements PackageModelBakery<List<BakedQuad>> {
	public BakedQuadPackageModelBakery(BakedModel baseModel, BlockState defaultState, TextureAtlasSprite specialFrameSprite, TextureAtlasSprite specialInnerSprite) {
		super(baseModel, defaultState, specialFrameSprite, specialInnerSprite);
	}
	
	private final boolean swapRedAndBlue = PackagesClient.inst().config.get(PropsClient.FORGE_SWAP_RED_AND_BLUE);
	
	@Override
	public @Nullable TextureAtlasSprite getParticleIcon(@Nullable Block b) {
		if(b == null) return null;
		//forge has a slightly fancier way to get particle icons
		else return Minecraft.getInstance().getBlockRenderer().getBlockModel(b.defaultBlockState()).getParticleIcon(ModelData.EMPTY);
	}
	
	@Override
	public List<BakedQuad> bake(@Nullable Object cacheKey, @Nullable DyeColor faceColor, @Nullable Block frameBlock, @Nullable Block innerBlock) {
		ArrayList<BakedQuad> result = new ArrayList<>();
		super.process(new BakedQuadView(swapRedAndBlue), faceColor, frameBlock, innerBlock, result::add);
		result.trimToSize();
		return result;
	}
}

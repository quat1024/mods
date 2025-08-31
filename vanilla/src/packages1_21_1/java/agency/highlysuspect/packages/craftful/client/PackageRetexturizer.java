package agency.highlysuspect.packages.craftful.client;

import agency.highlysuspect.quatlib.craftful.client.model.IQuadView;
import agency.highlysuspect.quatlib.craftful.client.model.SortedUvBounds;
import agency.highlysuspect.quatlib.craftless.util.QuatUtil;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Contains the mod's retexturing algorithm:
 * - Quads with tintindex 1 are tinted according to the color of the package.
 * - Quads displaying specialFrameSprite are retextured to the particle texture of frameBlock.
 * - Quads displaying specialInnerSprite are retextured to the particle texture of innerBlock.
 * - Any other quads are passed through unchanged.
 *
 * If a color / frameBlock / innerBlock are not provided, the relevant quads are omitted.
 * This is how the Package Crafter model works.
 *
 * @param <Q> Underlying quad type used by the IQuadView helper
 */
public abstract class PackageRetexturizer<Q> {
	public final BakedModel baseModel;
	public final BlockState defaultState;
	public final TextureAtlasSprite specialFrameSprite;
	public final TextureAtlasSprite specialInnerSprite;
	
	public PackageRetexturizer(BakedModel baseModel, BlockState defaultState, TextureAtlasSprite specialFrameSprite, TextureAtlasSprite specialInnerSprite) {
		this.baseModel = baseModel;
		this.defaultState = defaultState;
		this.specialFrameSprite = specialFrameSprite;
		this.specialInnerSprite = specialInnerSprite;
	}
	
	//slightly different across fabric and forge
	public abstract @Nullable TextureAtlasSprite getParticleIcon(@Nullable Block b);
	
	public void process(IQuadView<Q> baseQuad, @Nullable DyeColor faceColor, @Nullable Block frameBlock, @Nullable Block innerBlock, Consumer<Q> keeper) {
		SortedUvBounds boundsScratch = new SortedUvBounds();
		RandomSource random = new LegacyRandomSource(42);
		@Nullable TextureAtlasSprite frameSprite = getParticleIcon(frameBlock);
		@Nullable TextureAtlasSprite innerSprite = getParticleIcon(innerBlock);
		
		for(Direction cullFace : QuatUtil.DIRECTIONS_AND_NULL) {
			for(BakedQuad vanillaQuad : baseModel.getQuads(defaultState, cullFace, random)) {
				baseQuad.fromVanilla(vanillaQuad, cullFace);
				
				if(baseQuad.getTintIndex() == 1) {
					if(faceColor != null) {
						baseQuad.enableWriting();
						baseQuad.setTintAllVerts(faceColor.getMapColor().col);
						keeper.accept(baseQuad.keep());
					}
					continue;
				}
				
				boundsScratch.initFrom(baseQuad);
				if(boundsScratch.displaysSprite(specialFrameSprite)) {
					if(frameSprite != null) {
						baseQuad.enableWriting();
						baseQuad.retexture(boundsScratch, specialFrameSprite, frameSprite);
						keeper.accept(baseQuad.keep());
					}
					continue;
				}
				
				if(boundsScratch.displaysSprite(specialInnerSprite)) {
					if(innerSprite != null) {
						baseQuad.enableWriting();
						baseQuad.retexture(boundsScratch, specialInnerSprite, innerSprite);
						keeper.accept(baseQuad.keep());
					}
					continue;
				}
				
				//keep all the non-special sprites
				keeper.accept(baseQuad.keep());
			}
		}
	}
}

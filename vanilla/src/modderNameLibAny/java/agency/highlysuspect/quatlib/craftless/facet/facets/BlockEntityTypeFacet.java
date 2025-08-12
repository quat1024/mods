package agency.highlysuspect.quatlib.craftless.facet.facets;

import agency.highlysuspect.quatlib.craftless.QuatlibBase;
import agency.highlysuspect.quatlib.craftless.facet.Facet;
import agency.highlysuspect.quatlib.craftless.facet.Latch;
import agency.highlysuspect.quatlib.craftless.facet.RegType;
import agency.highlysuspect.quatlib.craftless.util.BlockEntityFactory;
import agency.highlysuspect.quatlib.craftless.util.QuatUtil;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Facet
public class BlockEntityTypeFacet {
	public BlockEntityFactory<?> factory;
	public Latch<BlockEntityType<?>> latch;
	public List<Latch<? extends Block>> blocks = new ArrayList<>();
	
	public BlockEntityTypeFacet factory(BlockEntityFactory<?> factory) {
		this.factory = factory;
		return this;
	}
	
	//yeah the java type inference is kinda falling apart right now
	@SuppressWarnings("unchecked")
	public BlockEntityTypeFacet latch(Latch<?> latch) {
		this.latch = (Latch<BlockEntityType<?>>) latch;
		return this;
	}
	
	@SafeVarargs
	public final BlockEntityTypeFacet addBlocks(Latch<? extends Block>... blocks) {
		this.blocks.addAll(Arrays.asList(blocks));
		return this;
	}
	
	public static void handle(RegFacet.RegistryGetter getter, List<BlockEntityTypeFacet> allFacets) {
		QuatUtil.collate(allFacets, f -> f.latch).forEach((typeLatch, facets) -> {
			//pick one with a nonnull factory
			BlockEntityFactory<?> factory = facets.stream()
				.map(f -> f.factory)
				.filter(Objects::nonNull)
				.findFirst()
				.orElseThrow(() -> new RuntimeException("No factory registered for BlockEntityType " + typeLatch.id));
			
			//register the block entity type
			getter.getReg(RegType.BLOCK_ENTITY_TYPES).defer(typeLatch, () -> {
				return (BlockEntityType<?>) QuatlibBase.inst().makeBlockEntityType(factory, facets.stream()
					.flatMap(f -> f.blocks.stream())
					.map(Latch::get)
					.toArray(Block[]::new));
			});
		});
	}
}

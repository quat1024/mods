package agency.highlysuspect.quatlib.craftless.facet.facets;

import agency.highlysuspect.quatlib.craftless.QuatlibBase;
import agency.highlysuspect.quatlib.craftless.facet.*;
import agency.highlysuspect.quatlib.craftless.util.BlockEntityFactory;
import agency.highlysuspect.quatlib.craftless.util.QuatUtil;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

@Facet
public record BlockEntityTypeFacet(Latch<BlockEntityType<?>> latch, @Nullable BlockEntityFactory<?> factory, List<Latch<? extends Block>> blocks) {
	
	@SuppressWarnings("unchecked")
	public static void handle(RegistryGetter getter, List<BlockEntityTypeFacet> allFacets) {
		QuatUtil.collate(allFacets, f -> f.latch).forEach((typeLatch, facets) -> {
			//pick one with a nonnull factory
			BlockEntityFactory<?> factory = facets.stream()
				.map(BlockEntityTypeFacet::factory)
				.filter(Objects::nonNull)
				.findFirst()
				.orElseThrow(() -> new RuntimeException("No factory registered for BlockEntityType " + typeLatch.id));
			
			//register the block entity type
			Reg<BlockEntityType<?>> blockEntityTypeReg = (Reg<BlockEntityType<?>>) getter.getReg(RegType.BLOCK_ENTITY_TYPES);
			
			blockEntityTypeReg.defer(typeLatch, () ->
				QuatlibBase.inst().makeBlockEntityType(factory, facets.stream()
					.flatMap(f -> f.blocks.stream())
					.map(Latch::get)
					.toArray(Block[]::new)));
		});
	}
}

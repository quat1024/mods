package agency.highlysuspect.quatlib.craftless.facet.facets;

import agency.highlysuspect.quatlib.craftless.facet.FacetBucket;
import agency.highlysuspect.quatlib.craftless.facet.Latch;
import agency.highlysuspect.quatlib.craftless.util.BlockEntityFactory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//TODO: collate these builders somehow and make the FacetBuilder impl
// just add a RegFacet
public class BlockEntityTypeFacetBuilder implements FacetBuilder {
	@SuppressWarnings("unchecked") //i'm blaming java
	public <T extends BlockEntity> BlockEntityTypeFacetBuilder(Latch<BlockEntityType<T>> latch) {
		this.latch = (Latch<BlockEntityType<?>>) (Object) latch;
	}
	
	final Latch<BlockEntityType<?>> latch;
	BlockEntityFactory<?> factory;
	List<Latch<? extends Block>> blocks = new ArrayList<>();
	
	public BlockEntityTypeFacetBuilder factory(BlockEntityFactory<?> factory) {
		this.factory = factory;
		return this;
	}
	
	public BlockEntityTypeFacetBuilder blocks(List<Latch<? extends Block>> blocks) {
		this.blocks.addAll(blocks);
		return this;
	}
	
	@SafeVarargs
	public final BlockEntityTypeFacetBuilder blocks(Latch<? extends Block>... blocks) {
		return blocks(Arrays.asList(blocks));
	}
	
	@Override
	public void build(FacetBucket facets) {
		facets.add(new BlockEntityTypeFacet(latch, factory, blocks));
	}
}

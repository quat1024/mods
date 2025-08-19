package agency.highlysuspect.quatlib.craftless.facet;

import agency.highlysuspect.quatlib.craftless.facet.dgen.DgenHelper;
import agency.highlysuspect.quatlib.craftless.facet.facets.*;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface Gen {
	void gen(Ctx ctx, Consumer<Gen> more);
	
	static void run(Ctx ctx, Gen... topLevelGens) {
		List<Gen> gensToRun = Arrays.asList(topLevelGens);
		while(!gensToRun.isEmpty()) {
			List<Gen> more = new ArrayList<>();
			for(Gen gen : gensToRun) gen.gen(ctx, more::add);
			gensToRun = more;
		}
	}
	
	class Ctx {
		public Ctx(@Nullable DgenHelper dgen) {
			this.dgen = dgen;
		}
		
		public final List<FacetBuilder> facetBuilders = new ArrayList<>();
		public final @Nullable DgenHelper dgen;
		
		public void buildFacets(FacetBucket bucket) {
			for(FacetBuilder builder : facetBuilders) builder.build(bucket);
		}
		
		//TODO: explore whether this kinda thing is needed
		public <T> T getService(Class<T> key) throws UnsupportedOperationException {
			throw new UnsupportedOperationException("Unknown service " + key.getName());
		}
		
		//shortcuts
		public <T extends FacetBuilder> T add(T facet) {
			facetBuilders.add(facet);
			return facet;
		}
		
		public RegFacetBuilder reg(Latch<?> latch, Supplier<?> thing) {
			return add(new RegFacetBuilder(latch, thing));
		}
		
		public <T extends BlockEntity> BlockEntityTypeFacetBuilder blockEntity(Latch<BlockEntityType<T>> latch) {
			return add(new BlockEntityTypeFacetBuilder(latch));
		}
		
		public DispenserBehaviorFacetBuilder dispenser(Latch<? extends Item> latch) {
			return add(new DispenserBehaviorFacetBuilder(latch));
		}
		
		public DispenserBehaviorFacetBuilder dispenser(Latch<? extends Item> latch, DispenseItemBehavior behavior) {
			return dispenser(latch).behavior(behavior);
		}
		
		public LangFacetBuilder lang(String file) {
			return add(new LangFacetBuilder(file));
		}
		
		public SoundEventFacetBuilder sound(Latch<SoundEvent> sound) {
			return add(new SoundEventFacetBuilder(sound));
		}
	}
}

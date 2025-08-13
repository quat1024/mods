package agency.highlysuspect.quatlib.craftless.facet;

import agency.highlysuspect.quatlib.craftless.facet.dgen.DgenHelper;
import agency.highlysuspect.quatlib.craftless.facet.facets.BlockEntityTypeFacet;
import agency.highlysuspect.quatlib.craftless.facet.facets.DispenserBehaviorFacet;
import agency.highlysuspect.quatlib.craftless.facet.facets.RegFacet;
import agency.highlysuspect.quatlib.craftless.facet.facets.SoundEventFacet;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
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
		public Ctx(FacetBucket facets, @Nullable DgenHelper dgen) {
			this.facets = facets;
			this.dgen = dgen;
		}
		
		public final FacetBucket facets;
		public final @Nullable DgenHelper dgen;
		
		public <T> T getService(Class<T> key) throws UnsupportedOperationException {
			throw new UnsupportedOperationException("Unknown service " + key.getName());
		}
		
		//shortcuts
		public <T> T add(T facet) {
			return facets.add(facet);
		}
		
		public RegFacet reg(Latch<?> latch, Supplier<?> thing) {
			return add(new RegFacet()).latch(latch).sup(thing);
		}
		
		public BlockEntityTypeFacet blockEntity(Latch<?> latch) {
			return add(new BlockEntityTypeFacet()).latch(latch);
		}
		
		public DispenserBehaviorFacet dispenser(Latch<? extends Item> latch) {
			return add(new DispenserBehaviorFacet()).latch(latch);
		}
		
		public SoundEventFacet soundsJson(Latch<SoundEvent> latch) {
			return add(new SoundEventFacet()).latch(latch);
		}
	}
}

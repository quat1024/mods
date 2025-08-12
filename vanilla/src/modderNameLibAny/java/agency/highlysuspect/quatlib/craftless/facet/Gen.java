package agency.highlysuspect.quatlib.craftless.facet;

import agency.highlysuspect.quatlib.craftless.facet.facets.BlockEntityTypeFacet;
import agency.highlysuspect.quatlib.craftless.facet.facets.RegFacet;

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
		public Ctx(FacetBucket facets, Period period) {
			this.facets = facets;
			this.period = period;
		}
		
		public final FacetBucket facets;
		public final Period period;
		
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
		
		public boolean isDatagen() {
			return period == Period.DATAGEN;
		}
		
		public boolean isRuntime() {
			return period == Period.RUNTIME;
		}
	}
}

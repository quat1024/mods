package agency.highlysuspect.quatlib.craftless.facet.facets;

import agency.highlysuspect.quatlib.craftless.facet.FacetBucket;
import agency.highlysuspect.quatlib.craftless.facet.dgen.DgenHelper;
import org.jetbrains.annotations.Nullable;

public interface FacetBuilder {
	void build(FacetBuilder.BuildCtx ctx, FacetBucket facets);
	
	class BuildCtx {
		public BuildCtx(@Nullable DgenHelper dgen) {
			this.dgen = dgen;
		}
		
		public final @Nullable DgenHelper dgen;
	}
}

package agency.highlysuspect.quatlib.craftless.facet.facets;

import agency.highlysuspect.quatlib.craftless.facet.FacetBucket;
import agency.highlysuspect.quatlib.craftless.facet.Id;
import agency.highlysuspect.quatlib.craftless.facet.Idable;

import java.util.Objects;

public class EmiTagExclusionFacetBuilder implements FacetBuilder {
	public EmiTagExclusionFacetBuilder(Idable itemTag) {
		this.itemTag = Objects.requireNonNull(itemTag.getId());
	}
	
	public EmiTagExclusionFacetBuilder(String itemTag) {
		this(Id.parse(itemTag));
	}
	
	final Id itemTag;
	
	@Override
	public void build(FacetBuilder.BuildCtx ctx, FacetBucket facets) {
		if(ctx.dgen == null) return; //not doing datagen
		
		facets.add(new EmiTagExclusionFacet(itemTag));
	}
}

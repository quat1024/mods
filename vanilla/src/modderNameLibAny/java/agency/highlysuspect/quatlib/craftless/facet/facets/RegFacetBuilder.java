package agency.highlysuspect.quatlib.craftless.facet.facets;

import agency.highlysuspect.quatlib.craftless.facet.FacetBucket;
import agency.highlysuspect.quatlib.craftless.facet.Latch;

import java.util.Objects;
import java.util.function.Supplier;

public class RegFacetBuilder implements FacetBuilder {
	public RegFacetBuilder() { }
	
	public RegFacetBuilder(Latch<?> latch) {
		this.latch = latch;
	}
	
	public RegFacetBuilder(Latch<?> latch, Supplier<?> sup) {
		this.latch = latch;
		this.sup = sup;
	}
	
	Latch<?> latch;
	Supplier<?> sup;
	
	@Override
	public void build(FacetBucket facets) {
		Objects.requireNonNull(latch, this::toString);
		Objects.requireNonNull(sup, this::toString);
		
		facets.add(new RegFacet(latch, sup));
	}
	
	@Override
	public String toString() {
		return "RegFacetBuilder{latch=%s, sup=%s}".formatted(latch, sup);
	}
}

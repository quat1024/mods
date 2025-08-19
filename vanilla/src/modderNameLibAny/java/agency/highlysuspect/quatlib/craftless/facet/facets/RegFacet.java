package agency.highlysuspect.quatlib.craftless.facet.facets;

import agency.highlysuspect.quatlib.craftless.facet.Facet;
import agency.highlysuspect.quatlib.craftless.facet.Latch;
import agency.highlysuspect.quatlib.craftless.facet.Reg;
import agency.highlysuspect.quatlib.craftless.facet.RegistryGetter;

import java.util.List;
import java.util.function.Supplier;

@Facet
public record RegFacet(Latch<?> latch, Supplier<?> sup) {
	public static void handle(RegistryGetter regGetter, List<RegFacet> facets) {
		facets.forEach(facet -> doHandle(regGetter, facet));
	}
	
	//RegFacet2 is type-erased but this definitely isn't, so we need some type football
	@SuppressWarnings("unchecked")
	private static <T, X extends T> void doHandle(RegistryGetter getter, RegFacet facet) {
		Reg<T> reg = (Reg<T>) getter.getReg(facet.latch.type);
		reg.defer((Latch<X>) facet.latch, (Supplier<X>) facet.sup);
	}
}

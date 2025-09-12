package agency.highlysuspect.quatlib.craftless.facet.facets;

import agency.highlysuspect.quatlib.craftless.facet.Latch;
import agency.highlysuspect.quatlib.craftless.facet.Reg;
import agency.highlysuspect.quatlib.craftless.facet.RegistryGetter;

import java.util.List;
import java.util.function.Supplier;

public record RegFacet(Latch<?> latch, Supplier<?> sup) {
	public static void handle(RegistryGetter regGetter, List<RegFacet> facets) {
		facets.forEach(facet -> doHandle(regGetter, facet));
	}
	
	//Need some type football because i'm type-erased but the registry system is not
	//TODO don't type erase so hard
	@SuppressWarnings("unchecked")
	private static <T, X extends T> void doHandle(RegistryGetter getter, RegFacet facet) {
		Reg<T> reg = (Reg<T>) getter.getReg(facet.latch.type);
		reg.defer((Latch<X>) facet.latch, (Supplier<X>) facet.sup);
	}
}

package agency.highlysuspect.quatlib.craftless.facet.facets;

import agency.highlysuspect.quatlib.craftless.facet.Facet;
import agency.highlysuspect.quatlib.craftless.facet.Latch;
import agency.highlysuspect.quatlib.craftless.facet.Reg;
import agency.highlysuspect.quatlib.craftless.facet.RegType;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@Facet
public class RegFacet {
	public Latch<?> latch;
	public Supplier<?> sup;
	
	public RegFacet latch(Latch<?> latch) {
		this.latch = latch;
		return this;
	}
	
	public RegFacet sup(Supplier<?> sup) {
		this.sup = sup;
		return this;
	}
	
	private void check() {
		Objects.requireNonNull(latch, () -> "null latch (supplier: " + sup + ")");
		Objects.requireNonNull(sup, () -> "null supplier (latch: " + latch + ")");
	}
	
	public static void handle(RegistryGetter regGetter, List<RegFacet> facets) {
		facets.forEach(RegFacet::check);
		facets.forEach(facet -> doHandle(regGetter, facet));
	}
	
	//RegFacet is type-erased but this definitely isn't, so we need some type football
	@SuppressWarnings("unchecked")
	private static <T, X extends T> void doHandle(RegistryGetter getter, RegFacet facet) {
		Reg<T> reg = (Reg<T>) getter.getReg(facet.latch.type);
		reg.defer((Latch<X>) facet.latch, (Supplier<X>) facet.sup);
	}
	
	public interface RegistryGetter {
		Reg<?> getReg(RegType<?> type) throws UnsupportedOperationException;
	}
}

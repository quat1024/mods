package agency.highlysuspect.quatlib.craftless.facet.facets;

import agency.highlysuspect.quatlib.craftless.facet.Facet;
import agency.highlysuspect.quatlib.craftless.facet.Latch;
import agency.highlysuspect.quatlib.craftless.facet.Reg;
import agency.highlysuspect.quatlib.craftless.facet.RegType;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

//TODO(season2): remove the type parameter, it's just getting in the way of stuff.
@Facet
public class RegFacet<T> {
	public Latch<? extends T> latch;
	public Supplier<? extends T> sup;
	
	public RegFacet<T> latch(Latch<? extends T> latch) {
		this.latch = latch;
		return this;
	}
	
	public RegFacet<T> sup(Supplier<? extends T> sup) {
		this.sup = sup;
		return this;
	}
	
	private void check() {
		Objects.requireNonNull(latch, () -> "null latch (supplier: " + sup + ")");
		Objects.requireNonNull(sup, () -> "null supplier (latch: " + latch + ")");
	}
	
	public static void handle(RegistryGetter regGetter, List<RegFacet<?>> facets) {
		facets.forEach(RegFacet::check);
		facets.forEach(facet -> doHandle(regGetter, facet));
	}
	
	//this is some horrendously unsound casting, but it works
	@SuppressWarnings("unchecked")
	private static <T> void doHandle(RegistryGetter getter, RegFacet<T> facet) {
		Reg<T> reg = getter.getReg((RegType<T>) facet.latch.type);
		reg.defer((Latch<T>) facet.latch, (Supplier<T>) facet.sup);
	}
	
	/**
	 * sadly the generic placement means this can't be a lambda function.
	 * method references are okay though
	 */
	public interface RegistryGetter {
		<T> Reg<T> getReg(RegType<T> type) throws UnsupportedOperationException;
	}
}

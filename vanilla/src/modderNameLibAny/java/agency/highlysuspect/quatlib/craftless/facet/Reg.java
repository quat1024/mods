package agency.highlysuspect.quatlib.craftless.facet;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class Reg<T> {
	protected final Map<Id, Latch<? extends T>> openLatches = new HashMap<>();
	
	/**
	 * at some later point, construct "sup" and register it under "latch"
	 */
	protected abstract <X extends T> void doDefer(Latch<X> latch, Supplier<X> sup);
	
	public <X extends T> void defer(Latch<X> latch, Supplier<X> sup) {
		Latch<? extends T> old = openLatches.put(latch.id, latch);
		if(old != null) throw new IllegalStateException("Two latches registered for id " + latch.id);
		
		doDefer(latch, sup);
	}
	
	@SuppressWarnings("unchecked")
	protected void shut(Id id, T registered) {
		Latch<? extends T> l = openLatches.remove(id);
		Objects.requireNonNull(l, () -> "No latch registered for id " + id);
		
		//unsound cast b/c the ID being correct doesn't guarantee the subtype is correct
		((Latch<T>) l).shut(registered);
	}
}

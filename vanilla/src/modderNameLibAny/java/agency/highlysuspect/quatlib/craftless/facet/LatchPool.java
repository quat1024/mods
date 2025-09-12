package agency.highlysuspect.quatlib.craftless.facet;

import java.util.HashMap;
import java.util.Map;

/**
 * Keeps track of & deduplicates all Latches in the system.
 * Two latches for the same ID in the same regtype will end up being the same object,
 * even if the same latch is referred to under different types via type-erasure.
 * (latch MyBlock, vs latch Block, vs latch Object)
 */
public class LatchPool {
	public static final LatchPool INST = new LatchPool();
	
	private final Map<RegType<?>, Map<Id, Latch<?>>> latchTable = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	public <REG, OBJ> Latch<OBJ> get(RegType<REG> reg, Id id) {
		synchronized(this) {
			Map<Id, Latch<?>> latchesForRegistry = latchTable.computeIfAbsent(reg, __ -> new HashMap<>());
			
			//this is global-stateful:
			// if it's the first time we're seeing this latch, certainly the object is not registered yet so make a new open latch
			// otherwise if a latch already exists, it will return the existing latch, which may or may not be shut depending on when i'm called
			return (Latch<OBJ>) latchesForRegistry.computeIfAbsent(id, __ -> new Latch<>(reg, id, null));
		}
	}
	
	@SuppressWarnings("unchecked")
	public <REG, OBJ> void shut(RegType<REG> reg, Id id, OBJ thing) {
		Latch<OBJ> latch;
		synchronized(this) {
			Map<Id, Latch<?>> latchesForRegistry = latchTable.get(reg);
			if(latchesForRegistry == null || latchesForRegistry.isEmpty()) return;
			latch = (Latch<OBJ>) latchesForRegistry.get(id);
		}
		latch.shut(thing);
	}
}

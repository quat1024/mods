package agency.highlysuspect.rebindnarrator.craftful.fab;

import net.minecraft.client.KeyMapping;

import java.util.List;

public class NmukShim {
	public Iterable<KeyMapping> selfAndAlternates(KeyMapping key) {
		return List.of(key);
	}
}

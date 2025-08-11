package agency.highlysuspect.rebindnarrator.craftful.fab;

import de.siphalor.nmuk.api.NMUKAlternatives;
import net.minecraft.client.KeyMapping;

import java.util.ArrayList;
import java.util.List;

public class NmukSupport extends NmukShim {
	@Override
	public Iterable<KeyMapping> selfAndAlternates(KeyMapping self) {
		ArrayList<KeyMapping> real = new ArrayList<>(2);
		real.add(self);
		
		List<KeyMapping> alternates = NMUKAlternatives.getAlternatives(self);
		if(alternates != null && !alternates.isEmpty()) real.addAll(alternates);
		
		return real;
	}
}

package agency.highlysuspect.mods.test.neo;

import agency.highlysuspect.mods.test.SharedAcrossAllVanilla;
import agency.highlysuspect.mods.test.TwentyOne;

public class Neo121Shared {
	static {
		System.out.println(SharedAcrossAllVanilla.class);
		System.out.println(TwentyOne.class);
//		System.out.println(TwentyZero.class); //can't see it, good
	}
}

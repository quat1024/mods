package agency.highlysuspect.mods.test.neo;

import agency.highlysuspect.mods.test.SharedAcrossAllVanilla;

public class TestModClass2 {
	static {
		System.out.println(SharedAcrossAllVanilla.class);
		//System.out.println(TestModClass1.class); //can't see it, good
		System.out.println(Neo121Shared.class);
	}
}

package agency.highlysuspect.testmod2.neo;

import agency.highlysuspect.Neo121Shared;
import agency.highlysuspect.testmod2.TestMod2;
import agency.highlysuspect.testmod2.TestMod21_21_1;
import net.neoforged.fml.common.Mod;

@Mod(TestMod2.MODID)
public class TestModClass2 extends TestMod21_21_1 {
	static {
//		System.out.println(QuatBaseMod.class);
		//System.out.println(TestModClass1.class); //can't see it, good
		System.out.println(Neo121Shared.class);
		
		LOG.info("Hello from TestModClass2");
	}
}

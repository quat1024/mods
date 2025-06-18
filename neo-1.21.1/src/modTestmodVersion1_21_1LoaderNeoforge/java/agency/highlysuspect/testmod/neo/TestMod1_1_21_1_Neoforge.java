package agency.highlysuspect.testmod.neo;

import agency.highlysuspect.Neo121Shared;
import agency.highlysuspect.SharedAcrossAllVanilla;
import agency.highlysuspect.TwentyOne;
import agency.highlysuspect.testmod.TestMod1;
import agency.highlysuspect.testmod.Testmod1_1_21_1;
import net.neoforged.fml.common.Mod;

@Mod(TestMod1.MODID)
public class TestMod1_1_21_1_Neoforge extends Testmod1_1_21_1 {
	static {
		System.out.println(TwentyOne.class);
		System.out.println(Neo121Shared.class);
		System.out.println(SharedAcrossAllVanilla.class);
		
		LOG.info("XXXXXXXX hello from Testmod1_1_21_1_Neoforge");
	}
}

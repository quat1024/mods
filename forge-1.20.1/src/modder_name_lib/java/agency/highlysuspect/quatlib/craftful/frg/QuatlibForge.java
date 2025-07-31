package agency.highlysuspect.quatlib.craftful.frg;

import agency.highlysuspect.quatlib.craftful.QuatlibMc;
import agency.highlysuspect.quatlib.craftless.QuatlibBase;
import net.minecraftforge.fml.common.Mod;

@Mod(QuatlibBase.MODID)
public class QuatlibForge extends QuatlibMc {
	public QuatlibForge() {
		super();
	}
	
	public static QuatlibForge inst() {
		return (QuatlibForge) QuatlibBase.INST;
	}
}

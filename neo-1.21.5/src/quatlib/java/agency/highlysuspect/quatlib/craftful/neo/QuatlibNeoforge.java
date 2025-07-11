package agency.highlysuspect.quatlib.craftful.neo;

import agency.highlysuspect.quatlib.craftful.QuatlibMc;
import agency.highlysuspect.quatlib.craftless.QuatlibBase;
import net.neoforged.fml.common.Mod;

@Mod(QuatlibBase.MODID)
public class QuatlibNeoforge extends QuatlibMc {
	public QuatlibNeoforge() {
		super();
	}
	
	public static QuatlibNeoforge inst() {
		return (QuatlibNeoforge) QuatlibBase.INST;
	}
}

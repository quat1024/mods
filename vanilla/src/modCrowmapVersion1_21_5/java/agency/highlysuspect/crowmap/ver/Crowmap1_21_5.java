package agency.highlysuspect.crowmap.ver;

import agency.highlysuspect.crowmap.any.CrowmapBase;
import agency.highlysuspect.quatlib.any.util.LogFacade;
import agency.highlysuspect.quatlib.any.ver.Slf4jLogFacade;

public abstract class Crowmap1_21_5 extends CrowmapBase {
	public Crowmap1_21_5() {
		super();
	}
	
	public final LogFacade LOG = new Slf4jLogFacade(MODID);
	
	//singleton
	public static Crowmap1_21_5 inst() {
		return (Crowmap1_21_5) CrowmapBase.INST;
	}
}

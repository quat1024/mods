package agency.highlysuspect.crowmap.craftful;

import agency.highlysuspect.crowmap.craftless.CrowmapBase;
import agency.highlysuspect.quatlib.craftless.failure.FailureLogger;
import agency.highlysuspect.quatlib.craftless.util.LogFacade;
import agency.highlysuspect.quatlib.craftful.Slf4jLogFacade;

public abstract class Crowmap1_21_5 extends CrowmapBase {
	public Crowmap1_21_5() {
		super();
		failures.addListener(new FailureLogger(LOG));
	}
	
	public final LogFacade LOG = new Slf4jLogFacade(MODID);
	
	//singleton
	public static Crowmap1_21_5 inst() {
		return (Crowmap1_21_5) CrowmapBase.INST;
	}
}

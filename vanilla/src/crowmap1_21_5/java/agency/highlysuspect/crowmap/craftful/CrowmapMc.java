package agency.highlysuspect.crowmap.craftful;

import agency.highlysuspect.crowmap.craftless.CrowmapBase;
import agency.highlysuspect.quatlib.craftless.failure.FailureLogger;
import agency.highlysuspect.quatlib.craftless.util.LogFacade;
import agency.highlysuspect.quatlib.craftful.Slf4jLogFacade;

public abstract class CrowmapMc extends CrowmapBase {
	public CrowmapMc() {
		super();
		failures.addListener(new FailureLogger(LOG));
	}
	
	public final LogFacade LOG = new Slf4jLogFacade(NAME);
	
	public static CrowmapMc inst() {
		return (CrowmapMc) CrowmapBase.INST;
	}
}

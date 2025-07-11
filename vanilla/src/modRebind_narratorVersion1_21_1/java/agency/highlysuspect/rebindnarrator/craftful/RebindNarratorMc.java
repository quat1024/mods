package agency.highlysuspect.rebindnarrator.craftful;

import agency.highlysuspect.quatlib.craftful.Slf4jLogFacade;
import agency.highlysuspect.quatlib.craftless.failure.FailureLogger;
import agency.highlysuspect.quatlib.craftless.util.LogFacade;
import agency.highlysuspect.rebindnarrator.craftless.RebindNarratorBase;

public abstract class RebindNarratorMc extends RebindNarratorBase {
	public RebindNarratorMc() {
		super();
		failures.addListener(new FailureLogger(LOG));
	}
	
	public final LogFacade LOG = new Slf4jLogFacade(NAME);
	
	public static RebindNarratorMc inst() {
		return (RebindNarratorMc) RebindNarratorBase.INST;
	}
}

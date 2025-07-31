package agency.highlysuspect.quatlib.craftful;

import agency.highlysuspect.quatlib.craftless.QuatlibBase;
import agency.highlysuspect.quatlib.craftless.failure.FailureLogger;
import agency.highlysuspect.quatlib.craftless.util.LogFacade;
import agency.highlysuspect.quatlib.craftless.util.SharedConfigFileWatcher;

public class QuatlibMc extends QuatlibBase {
	public QuatlibMc() {
		super();
		failures.addListener(new FailureLogger(LOG));
	}
	
	public static final LogFacade LOG = new Slf4jLogFacade(NAME);
	
	@Override
	protected SharedConfigFileWatcher makeSharedConfigFileWatcher() {
		return new SharedConfigFileWatcher(LOG);
	}
	
	public static QuatlibMc inst() {
		return (QuatlibMc) QuatlibBase.INST;
	}
}

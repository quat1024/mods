package agency.highlysuspect.quatlib.craftless;

import agency.highlysuspect.quatlib.craftless.failure.FailureLogger;
import agency.highlysuspect.quatlib.craftless.failure.FailureRoot;
import agency.highlysuspect.quatlib.craftless.util.LogFacade;
import agency.highlysuspect.quatlib.craftless.util.SharedConfigFileWatcher;

public abstract class QuatlibBase {
	public QuatlibBase() {
		INST = this;
	}
	
	public static final String MODID = "modder_name_lib";
	public static final String NAME = "ModderNameLib";
	public static final LogFacade LOG = new Slf4jLogFacade(NAME);
	protected static QuatlibBase INST;
	
	public FailureRoot failures = new FailureRoot(NAME).addListener(new FailureLogger(LOG));
	public SharedConfigFileWatcher watcher = makeSharedConfigFileWatcher();
	
	protected abstract SharedConfigFileWatcher makeSharedConfigFileWatcher();
	
	public static QuatlibBase inst() {
		return INST;
	}
}

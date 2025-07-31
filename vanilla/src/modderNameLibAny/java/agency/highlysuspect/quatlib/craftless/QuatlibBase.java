package agency.highlysuspect.quatlib.craftless;

import agency.highlysuspect.quatlib.craftless.failure.FailureRoot;
import agency.highlysuspect.quatlib.craftless.util.SharedConfigFileWatcher;

public abstract class QuatlibBase {
	public QuatlibBase() {
		INST = this;
	}
	
	public static final String MODID = "modder_name_lib";
	public static final String NAME = "ModderNameLib";
	protected static QuatlibBase INST;
	
	public FailureRoot failures = new FailureRoot(NAME);
	public SharedConfigFileWatcher watcher = makeSharedConfigFileWatcher();
	
	protected abstract SharedConfigFileWatcher makeSharedConfigFileWatcher();
	
	public static QuatlibBase inst() {
		return INST;
	}
}

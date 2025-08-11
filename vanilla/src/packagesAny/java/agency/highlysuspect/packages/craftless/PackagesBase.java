package agency.highlysuspect.packages.craftless;

import agency.highlysuspect.quatlib.craftless.Slf4jLogFacade;
import agency.highlysuspect.quatlib.craftless.config.ConfigSection;
import agency.highlysuspect.quatlib.craftless.config.WritableConfig;
import agency.highlysuspect.quatlib.craftless.failure.FailureLogger;
import agency.highlysuspect.quatlib.craftless.failure.FailureRoot;
import agency.highlysuspect.quatlib.craftless.util.LogFacade;

public abstract class PackagesBase {
	public PackagesBase() {
		INST = this;
	}
	
	public static final String MODID = "packages";
	public static final String NAME = "Packages";
	public static final LogFacade LOG = new Slf4jLogFacade(NAME);
	
	public FailureRoot failures = new FailureRoot(NAME).addListener(new FailureLogger(LOG));
	public ConfigSection configSchema;
	public WritableConfig config;
	
	public void earlySetup() {
		configSchema = visitConfigSchema(new ConfigSection(NAME, "Options for Packages."));
		config = makeConfig(configSchema);
	}
	
	public abstract ConfigSection visitConfigSchema(ConfigSection root);
	public abstract WritableConfig makeConfig(ConfigSection schema);
	
	protected static PackagesBase INST;
	public static PackagesBase inst() {
		return INST;
	}
}

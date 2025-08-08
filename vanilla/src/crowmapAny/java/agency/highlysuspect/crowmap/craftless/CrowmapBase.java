package agency.highlysuspect.crowmap.craftless;

import agency.highlysuspect.quatlib.craftless.Slf4jLogFacade;
import agency.highlysuspect.quatlib.craftless.config.ConfigSection;
import agency.highlysuspect.quatlib.craftless.config.WritableConfig;
import agency.highlysuspect.quatlib.craftless.failure.FailureLogger;
import agency.highlysuspect.quatlib.craftless.failure.FailureRoot;
import agency.highlysuspect.quatlib.craftless.util.LogFacade;

public abstract class CrowmapBase {
	public CrowmapBase() {
		INST = this;
	}
	
	public static final String MODID = "crowmap";
	public static final String NAME = "Crowmap";
	public static final LogFacade LOG = new Slf4jLogFacade(NAME);
	protected static CrowmapBase INST;
	
	public FailureRoot failures = new FailureRoot(NAME).addListener(new FailureLogger(LOG));
	public ConfigSection configSchema;
	public WritableConfig config;
	
	public static CrowmapBase inst() {
		return INST;
	}
	
	public void init() {
		configSchema = visitConfigSchema(new ConfigSection(NAME, "Options for Crowmap."));
		config = makeConfig(configSchema);
	}
	
	public ConfigSection visitConfigSchema(ConfigSection root) {
		return CrowmapBaseOpts.visit(root);
	}
	
	public abstract WritableConfig makeConfig(ConfigSection schema);
}

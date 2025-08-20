package agency.highlysuspect.stairdown.craftless;

import agency.highlysuspect.quatlib.craftless.Slf4jLogFacade;
import agency.highlysuspect.quatlib.craftless.config.ConfigSection;
import agency.highlysuspect.quatlib.craftless.config.WritableConfig;
import agency.highlysuspect.quatlib.craftless.failure.FailureLogger;
import agency.highlysuspect.quatlib.craftless.failure.FailureRoot;
import agency.highlysuspect.quatlib.craftless.util.LogFacade;

import java.util.Objects;

public abstract class StairdownBase {
	public StairdownBase() {
		INST = this;
	}
	
	public static final String MODID = "stairdown";
	public static final String NAME = "Stairdown";
	public static final LogFacade LOG = new Slf4jLogFacade(NAME);
	
	public FailureRoot failures = new FailureRoot(NAME).addListener(new FailureLogger(LOG));
	public ConfigSection configSchema;
	public WritableConfig config;
	
	public void init() {
		configSchema = visitSchema(new ConfigSection("Stairdown", "Options for Stairdown."));
		config = makeConfig(configSchema);
	}
	
	protected ConfigSection visitSchema(ConfigSection root) {
		return StairdownOpts.visit(root);
	}
	
	protected abstract WritableConfig makeConfig(ConfigSection schema);
	
	protected static StairdownBase INST;
	
	public static StairdownBase inst() {
		return Objects.requireNonNull(INST, "StairdownBase is null");
	}
}

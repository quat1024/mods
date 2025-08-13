package agency.highlysuspect.packages.craftless;

import agency.highlysuspect.quatlib.craftless.QuatlibBase;
import agency.highlysuspect.quatlib.craftless.Slf4jLogFacade;
import agency.highlysuspect.quatlib.craftless.config.ConfigSection;
import agency.highlysuspect.quatlib.craftless.config.WritableConfig;
import agency.highlysuspect.quatlib.craftless.facet.Id;
import agency.highlysuspect.quatlib.craftless.facet.Reg;
import agency.highlysuspect.quatlib.craftless.facet.RegType;
import agency.highlysuspect.quatlib.craftless.facet.facets.RegFacet;
import agency.highlysuspect.quatlib.craftless.failure.FailureLogger;
import agency.highlysuspect.quatlib.craftless.failure.FailureRoot;
import agency.highlysuspect.quatlib.craftless.util.LogFacade;

import java.util.IdentityHashMap;
import java.util.Map;

public abstract class PackagesBase implements RegFacet.RegistryGetter {
	public PackagesBase() {
		INST = this;
	}
	
	public static final String MODID = "packages";
	public static final String NAME = "Packages";
	public static final LogFacade LOG = new Slf4jLogFacade(NAME);
	
	public static Id id(String path) {
		return new Id(MODID, path);
	}
	
	public FailureRoot failures = new FailureRoot(NAME).addListener(new FailureLogger(LOG));
	public ConfigSection configSchema;
	public WritableConfig config;
	
	//registry stuff (TODO put in its own class?)
	private final Map<RegType<?>, Reg<?>> regs = new IdentityHashMap<>();
	
	public void earlySetup() {
		configSchema = visitConfigSchema(new ConfigSection(NAME, "Options for Packages."));
		config = makeConfig(configSchema);
	}
	
	@Override
	public Reg<?> getReg(RegType<?> type) {
		return regs.computeIfAbsent(type, QuatlibBase.inst()::createReg);
	}
	
	public abstract ConfigSection visitConfigSchema(ConfigSection root);
	public abstract WritableConfig makeConfig(ConfigSection schema);
	
	protected static PackagesBase INST;
	public static PackagesBase inst() {
		return INST;
	}
}

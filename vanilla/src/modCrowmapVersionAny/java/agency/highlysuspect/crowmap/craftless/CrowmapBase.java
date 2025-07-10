package agency.highlysuspect.crowmap.craftless;

import agency.highlysuspect.quatlib.craftless.config.ConfigSection;
import agency.highlysuspect.quatlib.craftless.config.WritableConfig;
import org.jetbrains.annotations.Nullable;

public abstract class CrowmapBase {
	public CrowmapBase() {
		INST = this;
	}
	
	public static final String MODID = "crowmap";
	
	//singleton
	public static @Nullable CrowmapBase INST;
	public static CrowmapBase inst() {
		return INST;
	}
	
	//init
	public void init() {
		configSchema = visitConfigSchema(new ConfigSection("crowmap", "Options for Crowmap."));
		config = makeConfig(configSchema);
	}
	
	//config
	public ConfigSection configSchema;
	public WritableConfig config;
	public ConfigSection visitConfigSchema(ConfigSection root) {
		return CrowmapBaseOpts.visit(root);
	}
	
	public abstract WritableConfig makeConfig(ConfigSection schema);
}

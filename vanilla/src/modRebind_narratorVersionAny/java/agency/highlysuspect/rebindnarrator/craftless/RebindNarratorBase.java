package agency.highlysuspect.rebindnarrator.craftless;

import agency.highlysuspect.quatlib.craftless.config.ConfigSection;
import agency.highlysuspect.quatlib.craftless.config.DefaultConfig;
import agency.highlysuspect.quatlib.craftless.config.WritableConfig;
import agency.highlysuspect.quatlib.craftless.failure.FailureRoot;
import org.jetbrains.annotations.Nullable;

public abstract class RebindNarratorBase {
	public RebindNarratorBase() {
		INST = this;
	}
	
	public static final String MODID = "rebind_narrator";
	public static final String NAME = "Rebind Narrator";
	protected static RebindNarratorBase INST;
	
	public FailureRoot failures = new FailureRoot(NAME);
	public ConfigSection configSchema;
	public WritableConfig config;
	
	public @Nullable NarratorKeyPredicate impl;
	
	public static RebindNarratorBase inst() {
		return INST;
	}
	
	public void init() {
		configSchema = visitConfigSchema(new ConfigSection(NAME, "Options for Rebind Narrator."));
		config = makeConfig(configSchema);
		
		impl = makeKeyPredicate();
	}
	
	public ConfigSection visitConfigSchema(ConfigSection root) {
		return root;
	}
	
	public WritableConfig makeConfig(ConfigSection schema) {
		return new DefaultConfig();
	}
	
	public abstract NarratorKeyPredicate makeKeyPredicate();
	
	public static @Nullable NarratorKeyPredicate getImpl() {
		if(INST == null) return null;
		else return INST.impl;
	}
}

package agency.highlysuspect.packages.craftless.client;

import agency.highlysuspect.packages.craftless.PackagesBase;
import agency.highlysuspect.quatlib.craftless.config.ConfigSection;
import agency.highlysuspect.quatlib.craftless.config.WritableConfig;
import agency.highlysuspect.quatlib.craftless.failure.FailureRoot;

public abstract class PackagesBaseClient {
	public PackagesBaseClient() {
		INST = this;
	}
	
	public FailureRoot failures = PackagesBase.inst().failures;
	
	public ConfigSection configSchema;
	public WritableConfig config;
	
	public void earlySetup() {
		configSchema = visitClientConfigSchema(new ConfigSection(PackagesBase.NAME, "Options for Packages on your client."));
		config = makeClientConfig(configSchema);
	}
	
	public abstract ConfigSection visitClientConfigSchema(ConfigSection root);
	public abstract WritableConfig makeClientConfig(ConfigSection schema);
	
	protected static PackagesBaseClient INST;
	public static PackagesBaseClient inst() {
		return INST;
	}
}

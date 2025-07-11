package agency.highlysuspect.quatlib.craftless.config;

public class DefaultConfig implements ReadableConfig {
	public static final DefaultConfig INSTANCE = new DefaultConfig();
	
	@Override
	public <T> T get(ConfigOpt<T> opt) {
		return opt.getDefaultValue();
	}
}

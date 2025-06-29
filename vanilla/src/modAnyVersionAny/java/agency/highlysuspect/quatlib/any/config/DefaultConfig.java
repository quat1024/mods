package agency.highlysuspect.quatlib.any.config;

public class DefaultConfig implements ReadableConfig {
	public static final DefaultConfig INSTANCE = new DefaultConfig();
	
	@Override
	public <T> T get(ConfigOpt<? extends T> opt) {
		return opt.getDefaultValue();
	}
}

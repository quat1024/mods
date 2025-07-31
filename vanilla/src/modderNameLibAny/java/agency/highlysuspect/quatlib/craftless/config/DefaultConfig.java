package agency.highlysuspect.quatlib.craftless.config;

import java.util.function.Consumer;

public class DefaultConfig implements ReadableConfig, WritableConfig {
	public static final DefaultConfig INSTANCE = new DefaultConfig();
	
	@Override
	public <T> T get(ConfigOpt<T> opt) {
		return opt.getDefaultValue();
	}
	
	@Override
	public void modify(Consumer<Handle> modifier) {
		//no-op
	}
}

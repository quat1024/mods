package agency.highlysuspect.quatlib.craftless.config;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class MutableMapConfig implements ReadableConfig, WritableConfig {
	public MutableMapConfig(Map<ConfigOpt<?>, Object> state) {
		this.state = new IdentityHashMap<>(state);
	}
	
	public MutableMapConfig() {
		this(new IdentityHashMap<>());
	}
	
	public IdentityHashMap<ConfigOpt<?>, Object> state;
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(ConfigOpt<T> opt) {
		T val = (T) state.get(opt);
		return val == null ? opt.getDefaultValue() : val;
	}
	
	@Override
	public void modify(Consumer<Handle> modifier) {
		modifier.accept(new Handle() {
			@Override
			public <T> Handle set(ConfigOpt<T> opt, T value) {
				state.put(opt, value);
				return this;
			}
		});
	}
}

package agency.highlysuspect.quatlib.any.config;

import java.util.Map;

public interface ReadableConfig {
	<T> T get(ConfigOpt<? extends T> opt);
	
	/**
	 * a ReadableConfig representing the default state of the config
	 */
	class Default implements ReadableConfig {
		public static final Default INSTANCE = new Default();
		
		@Override
		public <T> T get(ConfigOpt<? extends T> opt) {
			return opt.getDefaultValue();
		}
	}
	
	class Mapped implements ReadableConfig {
		public Mapped(Map<ConfigOpt<?>, ?> opts) {
			this.opts = opts;
		}
		
		private final Map<ConfigOpt<?>, ?> opts;
		
		@SuppressWarnings("unchecked")
		@Override
		public <T> T get(ConfigOpt<? extends T> opt) {
			T val = (T) opts.get(opt);
			return val == null ? opt.getDefaultValue() : val;
		}
	}
}

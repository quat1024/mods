package agency.highlysuspect.quatlib.any.config;

import java.util.Map;
import java.util.function.Consumer;

public interface ConfigState {
	<T> T get(ConfigOpt<T> opt);
	void refresh();
	
	class Default implements ConfigState {
		public static final Default INSTANCE = new Default();
		
		@Override
		public <T> T get(ConfigOpt<T> opt) {
			return opt.getDefaultValue();
		}
		
		@Override
		public void refresh() {
		
		}
	}
	
	class Unset extends Default {
		public Unset(Consumer<String> log) {
			this.log = log;
		}
		
		private final Consumer<String> log;
		
		@Override
		public <T> T get(ConfigOpt<T> opt) {
			log.accept("Reading " + opt.getName() + " on an unset config. This is a bug.");
			return super.get(opt);
		}
	}
	
	class Mapped implements ConfigState {
		public Mapped(Map<ConfigOpt<?>, ?> opts) {
			this.opts = opts;
		}
		
		private final Map<ConfigOpt<?>, ?> opts;
		
		@SuppressWarnings("unchecked")
		@Override
		public <T> T get(ConfigOpt<T> opt) {
			T val = (T) opts.get(opt);
			return val == null ? opt.getDefaultValue() : val;
		}
		
		@Override
		public void refresh() {
		
		}
		
		@Override
		public String toString() {
			StringBuilder bob = new StringBuilder();
			opts.forEach((key, val) -> {
				bob.append(key.getName()).append(" -> ").append(val).append('\n');
			});
			return bob.toString();
		}
	}
}

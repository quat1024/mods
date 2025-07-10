package agency.highlysuspect.quatlib.craftless.config;

public interface ReadableConfig {
	<T> T get(ConfigOpt<? extends T> opt);
}

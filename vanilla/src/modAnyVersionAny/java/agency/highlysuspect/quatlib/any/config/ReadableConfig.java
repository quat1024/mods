package agency.highlysuspect.quatlib.any.config;

public interface ReadableConfig {
	<T> T get(ConfigOpt<? extends T> opt);
}

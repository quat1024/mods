package agency.highlysuspect.quatlib.craftless.config;

import java.util.function.Consumer;

public interface WritableConfig extends ReadableConfig {
	//you get a callback which can be used to modify the config.
	//why a callback? so the config knows when you're about to
	//add items, and so it knows when you're done (time to flush the file)
	void modify(Consumer<Handle> modifier);
	
	interface Handle {
		<T> Handle set(ConfigOpt<T> opt, T value);
	}
}

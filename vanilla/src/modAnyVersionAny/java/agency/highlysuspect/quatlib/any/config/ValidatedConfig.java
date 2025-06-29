package agency.highlysuspect.quatlib.any.config;

import java.util.IdentityHashMap;

/**
 * @see agency.highlysuspect.quatlib.any.config.MatchedUnparsedConfig
 */
public class ValidatedConfig extends IdentityHashMap<ConfigOpt<?>, Object> {
	//constructible through MatchedUnparsedConfig only...
	protected ValidatedConfig() {
	
	}
	
	/**
	 * exists since i might change ValidatedConfig from publicly extending IdentityHashMap to a different type...?
	 * this will always return a fresh mutable map
	 */
	public IdentityHashMap<ConfigOpt<?>, Object> toMap() {
		return new IdentityHashMap<>(this);
	}
}

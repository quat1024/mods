package agency.highlysuspect.quatlib.any.config;

public abstract class ConfigException extends Exception {
	public ConfigException(String message) {
		super(message);
	}
	
	public ConfigException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public static class OptionParseException extends ConfigException {
		public OptionParseException(String message, ConfigOpt<?> problematicOption) {
			super(message);
			this.problematicOption = problematicOption;
		}
		
		public OptionParseException(String message, ConfigOpt<?> problematicOption, Throwable cause) {
			super(message, cause);
			this.problematicOption = problematicOption;
		}
		
		public final ConfigOpt<?> problematicOption;
	}
	
	public static class ValidationException extends ConfigException {
		public ValidationException(String message, ConfigOpt<?> problematicOption) {
			super(message);
			this.problematicOption = problematicOption;
		}
		
		public ValidationException(String message, ConfigOpt<?> problematicOption, Throwable cause) {
			super(message, cause);
			this.problematicOption = problematicOption;
		}
		
		public final ConfigOpt<?> problematicOption;
	}
}

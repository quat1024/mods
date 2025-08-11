package agency.highlysuspect.packages.craftful.config;

import agency.highlysuspect.packages.craftful.Packages;

public interface CookedConfig {
	<T> T get(ConfigProperty<T> prop);
	boolean refresh();
	
	class Unset implements CookedConfig {
		public static final Unset INSTANCE = new Unset();
		
		@Override
		public <T> T get(ConfigProperty<T> prop) {
			Packages.LOGGER.fatal("Reading '" + prop.name() + "' before setting config!!!!!!!!! This is a bug!!!!!!");
			return prop.defaultValue();
		}
		
		@Override
		public boolean refresh() {
			return false;
		}
	}
}

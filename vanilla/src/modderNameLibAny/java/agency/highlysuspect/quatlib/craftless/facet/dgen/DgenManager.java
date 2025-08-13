package agency.highlysuspect.quatlib.craftless.facet.dgen;

import org.jetbrains.annotations.Nullable;

public interface DgenManager {
	boolean active();
	@Nullable DgenHelper createHelper(String modid);
	
	static DgenManager create() {
		return Boolean.parseBoolean(System.getProperty("quatlib.dgen", "false")) ? new Active() : new Inactive();
	}
	
	class Active implements DgenManager {
		@Override
		public boolean active() {
			return true;
		}
		
		@Override
		public @Nullable DgenHelper createHelper(String modid) {
			return new DgenHelper(modid);
		}
	}
	
	class Inactive implements DgenManager {
		@Override
		public boolean active() {
			return false;
		}
		
		@Override
		public @Nullable DgenHelper createHelper(String modid) {
			return null;
		}
	}
}

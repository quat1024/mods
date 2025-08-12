package agency.highlysuspect.quatlib.craftless.facet;

import agency.highlysuspect.quatlib.craftless.util.LogFacade;
import agency.highlysuspect.quatlib.craftless.util.QuatUtil;
import com.google.gson.JsonElement;

public interface FileGenner {
	void writeFile(Id path, String toWrite);
	
	default void writeJson(Id id, JsonElement toWrite) {
		//if the path has no file extension, default to .json
		if(id.path.indexOf('.') == -1) id = id.mapPath(p -> p + ".json");
		writeFile(id, QuatUtil.BASIC_PRETTY_GSON.toJson(toWrite));
	}
	
	class DebugGenner implements FileGenner {
		public DebugGenner(LogFacade log) {
			this.log = log;
		}
		
		private final LogFacade log;
		
		@Override
		public void writeFile(Id path, String toWrite) {
			log.info("Would write to {}: {}", path, toWrite);
		}
	}
}

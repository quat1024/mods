package agency.highlysuspect.quatlib.craftless.facet;

import agency.highlysuspect.quatlib.craftless.util.LogFacade;
import agency.highlysuspect.quatlib.craftless.util.PhysicalVersion;
import agency.highlysuspect.quatlib.craftless.util.QuatUtil;
import com.google.gson.JsonElement;
import org.jetbrains.annotations.Nullable;

public interface FileGenner {
	void writeFile(@Nullable PhysicalVersion ver, String path, String toWrite);
	
	default void writeJson(@Nullable PhysicalVersion ver, String path, JsonElement toWrite) {
		//if the path has no file extension, default to .json
		if(path.indexOf('.') == -1) path = path + ".json";
		writeFile(ver, path, QuatUtil.BASIC_PRETTY_GSON.toJson(toWrite));
	}
	
	class DebugGenner implements FileGenner {
		public DebugGenner(LogFacade log) {
			this.log = log;
		}
		
		private final LogFacade log;
		
		@Override
		public void writeFile(@Nullable PhysicalVersion ver, String path, String toWrite) {
			log.info("Would write to {}: {}", path, toWrite);
		}
	}
}

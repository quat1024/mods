package uploaderthing.rinth;

import uploaderthing.meta.Loader;
import uploaderthing.meta.PublishableProject;

import java.util.List;

public interface PublishableModrinthProject extends ModrinthProject, PublishableProject {
	default List<ModrinthProject> modrinthEmbeddedDeps(Loader loader, String minecraftVersion) {
		return List.of();
	}
	
	default List<ModrinthProject> modrinthRequiredDeps(Loader loader, String minecraftVersion) {
		return List.of();
	}
	
	//todo optional deps blah blah
}

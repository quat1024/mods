package uploaderthing.froge;

import uploaderthing.meta.Loader;
import uploaderthing.meta.PublishableProject;

import java.util.List;

public interface PublishableCurseforgeProject extends CurseforgeProject, PublishableProject {
	default List<CurseforgeProject> curseforgeEmbeddedDeps(Loader loader, String minecraftVersion) {
		return List.of();
	}
	
	default List<CurseforgeProject> curseforgeRequiredDeps(Loader loader, String minecraftVersion) {
		return List.of();
	}
}

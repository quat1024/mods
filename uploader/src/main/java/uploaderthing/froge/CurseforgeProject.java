package uploaderthing.froge;

import uploaderthing.meta.Project;

public interface CurseforgeProject extends Project {
	String curseforgeProjectId();
	String curseforgeSlug();
}

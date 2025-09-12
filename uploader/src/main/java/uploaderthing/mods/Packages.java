package uploaderthing.mods;

import uploaderthing.meta.Loader;
import uploaderthing.rinth.ModrinthProject;
import uploaderthing.rinth.PublishableModrinthProject;

import java.util.List;

public class Packages implements PublishableModrinthProject {
	public static final Packages INST = new Packages();
	private Packages() {}
	
	@Override
	public String modrinthProjectId() {
		return "10DZYVis";
	}
	
	@Override
	public List<ModrinthProject> modrinthEmbeddedDeps(Loader loader, String minecraftVersion) {
		return List.of(ModderNameLib.INST);
	}
}

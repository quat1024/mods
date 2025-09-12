package uploaderthing.mods;

import uploaderthing.meta.Loader;
import uploaderthing.rinth.ModrinthProject;
import uploaderthing.rinth.PublishableModrinthProject;

import java.util.List;

public class Crowmap implements PublishableModrinthProject {
	public static final Crowmap INST = new Crowmap();
	private Crowmap() {}
	
	@Override
	public String modrinthProjectId() {
		return "EAe3MQt5";
	}
	
	@Override
	public List<ModrinthProject> modrinthEmbeddedDeps(Loader loader, String minecraftVersion) {
		return List.of(ModderNameLib.INST);
	}
}

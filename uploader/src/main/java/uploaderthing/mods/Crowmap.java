package uploaderthing.mods;

import uploaderthing.froge.CurseforgeProject;
import uploaderthing.froge.PublishableCurseforgeProject;
import uploaderthing.meta.Loader;
import uploaderthing.rinth.ModrinthProject;
import uploaderthing.rinth.PublishableModrinthProject;

import java.util.List;

public class Crowmap implements PublishableModrinthProject, PublishableCurseforgeProject {
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
	
	@Override
	public List<CurseforgeProject> curseforgeEmbeddedDeps(Loader loader, String minecraftVersion) {
		return List.of(ModderNameLib.INST);
	}
	
	@Override
	public String curseforgeProjectId() {
		return "301943";
	}
	
	@Override
	public String curseforgeSlug() {
		return "crowmap";
	}
}

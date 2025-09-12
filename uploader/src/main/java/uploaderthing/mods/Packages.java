package uploaderthing.mods;

import uploaderthing.froge.CurseforgeProject;
import uploaderthing.froge.PublishableCurseforgeProject;
import uploaderthing.meta.Loader;
import uploaderthing.rinth.ModrinthProject;
import uploaderthing.rinth.PublishableModrinthProject;

import java.util.List;

public class Packages implements PublishableModrinthProject, PublishableCurseforgeProject {
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
	
	@Override
	public List<CurseforgeProject> curseforgeEmbeddedDeps(Loader loader, String minecraftVersion) {
		return List.of(ModderNameLib.INST);
	}
	
	@Override
	public String curseforgeProjectId() {
		return "383733";
	}
	
	@Override
	public String curseforgeSlug() {
		return "packages";
	}
}

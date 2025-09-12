package uploaderthing.mods;

import uploaderthing.froge.CurseforgeProject;
import uploaderthing.froge.PublishableCurseforgeProject;
import uploaderthing.meta.Loader;
import uploaderthing.rinth.ModrinthProject;
import uploaderthing.rinth.PublishableModrinthProject;

import java.util.List;

public class RebindNarrator implements PublishableModrinthProject, PublishableCurseforgeProject {
	public static final RebindNarrator INST = new RebindNarrator();
	private RebindNarrator() {}
	
	@Override
	public String modrinthProjectId() {
		return "qw2Ls89j";
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
		return "296996";
	}
	
	@Override
	public String curseforgeSlug() {
		return "rebind-narrator";
	}
}

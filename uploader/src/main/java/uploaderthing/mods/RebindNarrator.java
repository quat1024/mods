package uploaderthing.mods;

import uploaderthing.meta.Loader;
import uploaderthing.rinth.ModrinthProject;
import uploaderthing.rinth.PublishableModrinthProject;

import java.util.List;

public class RebindNarrator implements PublishableModrinthProject {
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
}

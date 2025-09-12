package uploaderthing.mods;

import uploaderthing.meta.Loader;
import uploaderthing.rinth.ModrinthProject;
import uploaderthing.rinth.PublishableModrinthProject;

import java.util.List;

public class ModrinthStagingTestMod implements PublishableModrinthProject {
	public static final ModrinthStagingTestMod INST = new ModrinthStagingTestMod();
	
	private ModrinthStagingTestMod() {}
	
	@Override
	public String modrinthProjectId() {
		return "nko9VfbY";
	}
	
	@Override
	public List<ModrinthProject> modrinthEmbeddedDeps(Loader loader, String minecraftVersion) {
		return List.of(ModrinthStagingTestMod2.INST);
	}
}

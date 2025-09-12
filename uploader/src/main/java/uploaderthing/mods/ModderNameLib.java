package uploaderthing.mods;

import uploaderthing.meta.Loader;
import uploaderthing.rinth.ModrinthProject;
import uploaderthing.rinth.PublishableModrinthProject;

import java.util.List;

public class ModderNameLib implements PublishableModrinthProject {
	public static final ModderNameLib INST = new ModderNameLib();
	private ModderNameLib() {}
	
	@Override
	public String modrinthProjectId() {
		return "zSoTsKbP";
	}
	
	@Override
	public List<ModrinthProject> modrinthRequiredDeps(Loader loader, String minecraftVersion) {
		if(loader == Loader.FABRIC) return List.of(FabricApi.INST);
		else return List.of();
	}
}

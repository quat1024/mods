package uploaderthing.mods;

import uploaderthing.froge.CurseforgeProject;
import uploaderthing.froge.PublishableCurseforgeProject;
import uploaderthing.meta.Loader;
import uploaderthing.rinth.ModrinthProject;
import uploaderthing.rinth.PublishableModrinthProject;

import java.util.List;

public class ModderNameLib implements PublishableModrinthProject, PublishableCurseforgeProject {
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
	
	@Override
	public List<CurseforgeProject> curseforgeRequiredDeps(Loader loader, String minecraftVersion) {
		if(loader == Loader.FABRIC) return List.of(FabricApi.INST);
		else return List.of();
	}
	
	@Override
	public String curseforgeProjectId() {
		return "1332219";
	}
	
	@Override
	public String curseforgeSlug() {
		return "m-dder-name-lib"; //not a typo
	}
}

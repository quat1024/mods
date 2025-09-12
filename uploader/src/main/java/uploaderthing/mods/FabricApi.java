package uploaderthing.mods;

import uploaderthing.froge.CurseforgeProject;
import uploaderthing.rinth.ModrinthProject;

public class FabricApi implements ModrinthProject, CurseforgeProject {
	public static final FabricApi INST = new FabricApi();
	
	private FabricApi() {}
	
	@Override
	public String modrinthProjectId() {
		return "P7dR8mSH";
	}
	
	@Override
	public String curseforgeProjectId() {
		return "306612";
	}
	
	@Override
	public String curseforgeSlug() {
		return "fabric-api";
	}
}

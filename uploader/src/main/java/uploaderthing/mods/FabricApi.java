package uploaderthing.mods;

import uploaderthing.rinth.ModrinthProject;

public class FabricApi implements ModrinthProject {
	public static final FabricApi INST = new FabricApi();
	
	private FabricApi() {}
	
	@Override
	public String modrinthProjectId() {
		return "P7dR8mSH";
	}
}

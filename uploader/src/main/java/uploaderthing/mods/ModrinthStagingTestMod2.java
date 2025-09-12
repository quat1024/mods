package uploaderthing.mods;

import uploaderthing.rinth.PublishableModrinthProject;

//Stand-in for moddernamelib
public class ModrinthStagingTestMod2 implements PublishableModrinthProject {
	public static final ModrinthStagingTestMod2 INST = new ModrinthStagingTestMod2();
	
	private ModrinthStagingTestMod2() {}
	
	@Override
	public String modrinthProjectId() {
		return "BxhnTQax";
	}
}

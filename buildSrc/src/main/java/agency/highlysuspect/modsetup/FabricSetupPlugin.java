package agency.highlysuspect.modsetup;

import net.fabricmc.loom.LoomGradlePlugin;
import org.gradle.api.Project;

public class FabricSetupPlugin extends AbstractLoaderSetupPlugin {
	@Override
	public void apply(Project project) {
		//load-bearing, due to needing to reach across source-sets
		//for run config stuff
		project.evaluationDependsOn(":vanilla");
		
		//apply myself
		super.apply(project);
		
		//set loader to fabric in the ext
		getExt(project).loader = "fabric";
		
		//apply loom
		project.getPlugins().apply(LoomGradlePlugin.class);
	}
}

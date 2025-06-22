package agency.highlysuspect.modsetup;

import net.neoforged.moddevgradle.boot.ModDevPlugin;
import org.gradle.api.Project;

public class NeoforgeSetupPlugin extends AbstractLoaderSetupPlugin {
	@Override
	public void apply(Project project) {
		//load-bearing, due to neoforge needing to reach across source-sets
		//for run config stuff
		project.evaluationDependsOn(":vanilla");
		
		//apply myself
		super.apply(project);
		
		//set loader to neoforge in my extension
		getExt(project).loader = "neoforge";
		
		//apply ModDevGradle
		project.getPlugins().apply(ModDevPlugin.class);
	}
}

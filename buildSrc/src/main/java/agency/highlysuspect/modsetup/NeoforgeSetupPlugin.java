package agency.highlysuspect.modsetup;

import net.neoforged.moddevgradle.boot.ModDevPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class NeoforgeSetupPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		//load-bearing!
		project.evaluationDependsOn(":vanilla");
		
		new ModSetupPlugin().apply(project);
		new ModDevPlugin().apply(project);
		
		ModSetupExtension modSetup = project.getExtensions().getByType(ModSetupExtension.class);
		modSetup.loader = "neoforge";
	}
}

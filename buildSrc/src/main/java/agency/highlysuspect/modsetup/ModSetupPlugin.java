package agency.highlysuspect.modsetup;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class ModSetupPlugin implements Plugin<Project> {
	@Override
	public void apply(Project target) {
		target.getLogger().lifecycle("HELLO FROM ModSetupPlugin");
		
		//apply java while we're here
		target.getPlugins().apply("java");
		
		//ext
		target.getExtensions().create("modSetup", ModSetupExtension.class, target);
	}
}

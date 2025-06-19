package agency.highlysuspect.modsetup;

import org.gradle.api.Named;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.jvm.tasks.Jar;

public class Mod implements Named {
	public Mod(String modid) {
		this.modid = modid;
	}
	
	public final String modid;
	//TODO: mod friendly name, mod version, mod desciption and so on. for substituting into fmj and mods.toml
	
	public SourceSet set;
	public Configuration splat;
	public TaskProvider<Jar> depJar;
	
	@Override
	public String getName() {
		return modid;
	}
}

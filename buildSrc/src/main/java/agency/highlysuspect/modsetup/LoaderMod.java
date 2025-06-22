package agency.highlysuspect.modsetup;

import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.gradle.api.Named;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.jvm.tasks.Jar;

import java.util.HashMap;
import java.util.Map;

public class LoaderMod implements Named {
	public LoaderMod(String modid) {
		this.modid = modid;
		
		vars.put("modid", modid);
		vars.put("name", StringGroovyMethods.capitalize(modid));
	}
	
	public final String modid;
	public Map<String, Object> vars = new HashMap<>();
	public boolean quatlib = true;
	
	//"out params"
	public SourceSet set;
	public Configuration splat;
	public TaskProvider<Jar> depJar;
	
	@Override
	public String getName() {
		return modid;
	}
}

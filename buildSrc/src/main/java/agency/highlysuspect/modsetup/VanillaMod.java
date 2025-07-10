package agency.highlysuspect.modsetup;

import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.artifacts.ConsumableConfiguration;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.jvm.tasks.Jar;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

//it's a vanilla mod. it makes sense if you don't think about it
public class VanillaMod implements Named {
	public VanillaMod(String modid) {
		this.modid = modid;
		
		vars.put("modid", modid);
		vars.put("name", StringGroovyMethods.capitalize(modid));
	}
	
	// IF YOU ADD ANYTHING REMEMBER TO ADD IT TO THE LoaderMod COPY CONSTRUCTOR //
	
	String modid;
	Set<String> versions = new LinkedHashSet<>();
	Map<String, Object> vars = new HashMap<>();
	boolean quatlib = true;
	
	@Nullable String simpleRunMainClass;
	
	//set in afterEvaluate
	SourceSet versionAgnosticSourceSet;
	Map<String, SourceSet> perVersionSourceSets = new HashMap<>();
	
	//NamedDomainObjectProvider<ConsumableConfiguration> versionAgnosticElements;
	Map<String, NamedDomainObjectProvider<ConsumableConfiguration>> perVersionElements = new HashMap<>();
	TaskProvider<Jar> versionAgnosticJar;
	Map<String, TaskProvider<Jar>> perVersionJars = new HashMap<>();
	
	@Override
	public String getName() {
		return modid;
	}
}

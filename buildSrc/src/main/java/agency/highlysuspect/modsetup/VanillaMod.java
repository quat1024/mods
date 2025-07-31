package agency.highlysuspect.modsetup;

import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.artifacts.ConsumableConfiguration;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.jvm.tasks.Jar;
import org.jetbrains.annotations.NotNull;
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
		vars.put("name", Util.snakeToPretty(modid));
		
		dependOnQuatlib = !modid.equals("modder_name_lib");
	}
	
	// IF YOU ADD ANYTHING REMEMBER TO ADD IT TO THE LoaderMod COPY CONSTRUCTOR //
	
	String modid;
	Set<String> versions = new LinkedHashSet<>();
	Map<String, Object> vars = new HashMap<>();
	boolean dependOnQuatlib;
	
	@Nullable String simpleRunMainClass;
	
	//set in afterEvaluate
	SourceSet versionAgnosticSourceSet;
	Map<String, SourceSet> perVersionSourceSets = new HashMap<>();
	
	TaskProvider<Jar> versionAgnosticJar;
	Map<String, TaskProvider<Jar>> perVersionJars = new HashMap<>();
	
	Map<String, NamedDomainObjectProvider<ConsumableConfiguration>> perVersionElements = new HashMap<>();
	
	@Override
	public @NotNull String getName() {
		return modid;
	}
	
	public SourceSet getPerVersionSourceSet(String version) {
		SourceSet set = perVersionSourceSets.get(version);
		if(set == null) throw new NullPointerException("Can't get source-set for version " + version + " of mod " + modid + "; is it marked compatible with that version in :vanilla?");
		return set;
	}
	
	public NamedDomainObjectProvider<ConsumableConfiguration> getPerVersionElement(String version) {
		NamedDomainObjectProvider<ConsumableConfiguration> cfg = perVersionElements.get(version);
		if(cfg == null) throw new NullPointerException("Can't get consumable configuration for version " + version + " of mod " + modid + "; is it marked compatible with that version in :vanilla?");
		return cfg;
	}
}

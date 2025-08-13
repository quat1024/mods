package agency.highlysuspect.modsetup;

import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConsumableConfiguration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.jvm.tasks.Jar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

//it's a vanilla mod. it makes sense if you don't think about it
@SuppressWarnings("UnstableApiUsage")
public class VanillaMod implements Named {
	public VanillaMod(String modid) {
		this.modid = modid;
		
		vars.put("modid", modid);
		vars.put("name", Util.snakeToTitle(modid));
	}
	
	// IF YOU ADD ANYTHING REMEMBER TO ADD IT TO THE LoaderMod COPY CONSTRUCTOR //
	
	public final String modid;
	public Set<String> versions = new LinkedHashSet<>();
	public Map<String, Object> vars = new HashMap<>();
	public boolean dependOnQuatlib = true;
	
	@Nullable String simpleRunMainClass;
	
	public SourceSet versionAgnosticSourceSet;
	public Map<String, SourceSet> perVersionSourceSets = new HashMap<>();
	
	public File versionAgnosticGeneratedResources;
	public Map<String, File> perVersionGeneratedResources = new HashMap<>();
	
	public TaskProvider<Jar> versionAgnosticJar;
	public Map<String, TaskProvider<Jar>> perVersionJars = new HashMap<>();
	
	public Map<String, NamedDomainObjectProvider<ConsumableConfiguration>> perVersionElements = new HashMap<>();
	
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
	
	public Dependency getSplattedDep(Project project, String version) {
		NamedDomainObjectProvider<ConsumableConfiguration> cfg = getPerVersionElement(version);
		return project.getDependencies().project(Map.of("path", ":vanilla", "configuration", cfg.getName()));
	}
}

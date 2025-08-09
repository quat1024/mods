package agency.highlysuspect.modsetup;

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.jvm.tasks.Jar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

public class LoaderMod extends VanillaMod {
	public LoaderMod(VanillaMod base) {
		super(base.modid);
		
		versions = new TreeSet<>(base.versions);
		vars = new HashMap<>(base.vars);
		dependOnQuatlib = base.dependOnQuatlib;
		simpleRunMainClass = base.simpleRunMainClass;
		versionAgnosticSourceSet = base.versionAgnosticSourceSet;
		perVersionSourceSets = new HashMap<>(base.perVersionSourceSets);
		perVersionElements = new HashMap<>(base.perVersionElements);
		versionAgnosticJar = base.versionAgnosticJar;
		perVersionJars = new HashMap<>(base.perVersionJars);
	}
	
	//TODO actually use this in the run configs / plug it into meta-inf
	// wherever it's supposed to end up
	public List<String> legacyForgeMixinConfigs = new ArrayList<>();
	
	//"out params"
	public SourceSet set;
	public Configuration splat;
	public TaskProvider<Jar> depJar;
	public TaskProvider<? extends Jar> depJarNamed;
	
	public List<RefmapJob> refmapJobs = new ArrayList<>();
}

package agency.highlysuspect.modsetup;

import com.unascribed.flexver.FlexVerComparator;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.language.jvm.tasks.ProcessResources;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

//many of these are for working with Proejcts, which is why this is an abstract class, just to
//avoid manually passing the Project argument around everywhere. there are some static ones at
//the bottom which don't work on a specific project
public abstract class Util {
	public Util(Project project) {
		this.project = project;
		this.sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
	}
	
	public final Project project;
	public final SourceSetContainer sourceSets;
	
	public void addSpongeRepo() {
		project.getRepositories().maven(mvn -> {
			mvn.setUrl("https://repo.spongepowered.org/repository/maven-public/");
			mvn.content(it ->
				it.includeGroup("org.spongepowered")
			);
		});
	}
	
	public Dependency floaderOnlyDep() {
		return project.getDependencies().project(Map.of(
			"path", ":floader-only",
			"configuration", "floaderOnlyElements"
		));
	}
	
	public Map<String, Object> broadlyApplicableProps() {
		return Map.of(
			"version", project.getVersion().toString(),
			"group", project.getGroup().toString(),
			"author", "quaternary",
			"license", "LGPL-3.0-or-later",
			"homepage", "https://highlysuspect.agency/",
			"sources", "https://github.com/quat1024/mods/",
			"issues", "https://github.com/quat1024/mods/issues"
		);
	}
	
	public SourceSet makeSourceSetWithCommonDeps(String name) {
		SourceSet set = sourceSets.create(name);
		withImplementation(set, "org.jetbrains:annotations:16.0.2");
		return set;
	}
	
	public void extendSourceSet2(SourceSet target, SourceSet... bases) {
		for(SourceSet base : bases) {
			target.setCompileClasspath(target.getCompileClasspath().plus(base.getCompileClasspath()));
			target.setRuntimeClasspath(target.getRuntimeClasspath().plus(base.getRuntimeClasspath()));
		}
		
		//and add the outputs of these sets to the source-set's implementation config
		//this makes IDEs pick up on it? i think? forgot what this is for
		project.getConfigurations().named(target.getImplementationConfigurationName(), cfg ->
			cfg.withDependencies(it -> {
				for(SourceSet base : bases)
					it.add(project.getDependencies().create(base.getOutput()));
			}));
	}
	
	//forgive my stupid dsl please
	//basically you can pass anything and/or arrays of anything and/or collections of anything into `deps`,
	//and it will flatten it all, feed it through project.dependencies.create, and add it to the configuration
	public void withDeps(String configurationName, Object... deps) {
		project.getConfigurations().named(configurationName, cfg -> withDeps(cfg, deps));
	}
	
	public void withDeps(Configuration configuration, Object... deps) {
		DependencyHandler dependencies = project.getDependencies();
		configuration.withDependencies(it ->
			flatten(deps, d ->
				it.add(dependencies.create(d))));
	}
	
	public void withImplementation(SourceSet set, Object... deps) {
		withDeps(set.getImplementationConfigurationName(), deps);
	}
	
	public void withCompileOnly(SourceSet set, Object... deps) {
		withDeps(set.getCompileOnlyConfigurationName(), deps);
	}
	
	protected void flatten(Object obj, Consumer<Object> act) {
		if(obj instanceof Object[] arr) for(Object elem : arr) flatten(elem, act);
		else if(obj instanceof Collection<?> c) for(Object elem : c) flatten(elem, act);
		else act.accept(obj);
	}
	
	public void configureProcessResources(SourceSet target, Map<String, Object> vars) {
		project.getTasks().named(target.getProcessResourcesTaskName(), ProcessResources.class, it -> {
			//h't to multiloader-template for this idea, implemented by MrAmericanMike
			//https://github.com/jaredlll08/MultiLoader-Template/blob/2450032d7e14b296df24c519d072310199a23f75/buildSrc/src/main/groovy/multiloader-common.gradle#L85
			Map<String, Object> jsonEscaped = new HashMap<>(vars);
			for(Map.Entry<String, Object> e : jsonEscaped.entrySet()) {
				String oString = e.getValue().toString();
				if(oString.contains("\"") || oString.contains("\n"))
					e.setValue(oString.replace("\"", "\\\"").replace("\n", "\\\n"));
			}
			
			it.filesMatching(List.of("pack.mcmeta", "fabric.mod.json", "*.mixins.json"), jsons -> jsons.expand(jsonEscaped));
			it.filesMatching(List.of("META-INF/neoforge.mods.toml", "META-INF/mods.toml"), tomls -> tomls.expand(vars));
			
			it.getInputs().properties(vars);
		});
	}
	
	public void setCompatLevel(SourceSet set, int compatLevel) {
		project.getTasks().named(set.getCompileJavaTaskName(), JavaCompile.class, it -> {
			it.getOptions().getRelease().set(compatLevel);
		});
	}
	
	//intended to be used from gradle api, like `modSetup.lib`
	@SuppressWarnings("unused")
	public Dependency lib(String filename) {
		return project.getDependencies().create(project.files("../lib/" + filename));
	}
	
	/// static stuff ///
	
	//Right-biased merge
	@SafeVarargs
	public static Map<String, Object> plus(Map<String, Object>... maps) {
		Map<String, Object> result = new HashMap<>();
		for(Map<String, Object> map : maps) result.putAll(map);
		return result;
	}
	
	// https://notes.highlysuspect.agency/versions.html
	public static int compatLevelForMinecraft(String minecraftVersion) {
		int result;
		if(FlexVerComparator.compare(minecraftVersion, "1.20.5") >= 0) result = 21;
		else if(FlexVerComparator.compare(minecraftVersion, "1.18.0") >= 0) result = 17;
		else if(FlexVerComparator.compare(minecraftVersion, "1.17.0") >= 0) result = 16;
		else result = 8;
		return result;
	}
	
	private static final Pattern underFollowedByLetter = Pattern.compile("_(.)");
	// my_cool_mod -> myCoolMod
	public static String snakeToCamel(String a) {
		return underFollowedByLetter.matcher(a)
			.replaceAll(r -> r.group(1).toUpperCase(Locale.ROOT));
	}
	// my_cool_mod -> My Cool Mod
	public static String snakeToTitle(String s) {
		return underFollowedByLetter.matcher(StringGroovyMethods.capitalize(s))
			.replaceAll(r -> " " + r.group(1).toUpperCase(Locale.ROOT));
	}
	
	public static boolean hasResource(SourceSet set, String filename) {
		for(File root : set.getResources().getSourceDirectories()) {
			if(new File(root, filename).exists()) return true;
		}
		return false;
	}
}

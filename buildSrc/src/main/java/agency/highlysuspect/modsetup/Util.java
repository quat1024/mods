package agency.highlysuspect.modsetup;

import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.language.jvm.tasks.ProcessResources;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Util {
	public static String modVersion(String mod, String version) {
		String mod2 = mod == null ? "Any" : StringGroovyMethods.capitalize(mod);
		String ver2 = version == null ? "Any" : version.replace('.', '_');
		return "mod" + mod2 + "Version" + ver2;
	}
	
	public static String modVersionLoader(String mod, String version, String loader) {
		return modVersion(mod, version) + "Loader" + StringGroovyMethods.capitalize(loader);
	}
	
	public static String modVersionConsumableConfigurationName(String mod, String version) {
		return modVersion(mod, version) + "Elements";
	}
	
	//dependency on a particular consumable configuration of :vanilla
	public static Dependency vanillaDep(Project project, String mod, String version) {
		return project.getDependencies().project(Map.of(
			"path", ":vanilla",
			"configuration", modVersionConsumableConfigurationName(mod, version)
		));
	}
	
	public static Dependency floaderOnlyDep(Project project) {
		return project.getDependencies().project(Map.of(
			"path", ":floader-only",
			"configuration", "floaderOnlyElements"
		));
	}
	
	//reference to a source set from :vanilla (somewhat brittle, relies on evaluationDependsOn)
	//only used by neoforge because of its janky run config system
	public static SourceSet vanillaSourceSet(Project project, String mod, String version) {
		Project vanilla = project.project(":vanilla");
		SourceSetContainer vanillaSourceSets = vanilla.getExtensions().getByType(SourceSetContainer.class);
		return vanillaSourceSets.getByName(modVersion(mod, version));
	}
	
	//forgive my stupid dsl please
	//basically you can pass anything and/or arrays of anything and/or collections of anything into `deps`,
	//and it will flatten it all, feed it through project.dependencies.create, and add it to the configuration
	public static void withDeps(Project project, String configurationName, Object... deps) {
		project.getConfigurations().named(configurationName, cfg -> withDeps(project, cfg, deps));
	}
	
	public static void withDeps(Project project, Configuration configuration, Object... deps) {
		DependencyHandler dependencies = project.getDependencies();
		configuration.withDependencies(it ->
			flatten(deps, d ->
				it.add(dependencies.create(d))));
	}
	
	public static void withImplementation(Project project, SourceSet set, Object... deps) {
		withDeps(project, set.getImplementationConfigurationName(), deps);
	}
	
	private static void flatten(Object obj, Consumer<Object> act) {
		if(obj instanceof Object[] arr) for(Object elem : arr) flatten(elem, act);
		else if(obj instanceof Collection<?> c) for(Object elem : c) flatten(elem, act);
		else act.accept(obj);
	}
	
	public static void extendSourceSetFrom(SourceSet ext, SourceSet... bases) {
		for(SourceSet base : bases) {
			ext.setCompileClasspath(ext.getCompileClasspath().plus(base.getCompileClasspath()));
			ext.setRuntimeClasspath(ext.getRuntimeClasspath().plus(base.getRuntimeClasspath()));
		}
	}
	
	public static List<Object> broadlyApplicableDeps(Project project) {
		return List.of(
			"org.jetbrains:annotations:16.0.2"
		);
	}
	
	public static Map<String, Object> broadlyApplicableProps(Project project) {
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
	
	@SafeVarargs
	public static Map<String, Object> plus(Map<String, Object>... maps) {
		Map<String, Object> result = new HashMap<>();
		for(Map<String, Object> map : maps) result.putAll(map);
		return result;
	}
	
	@SafeVarargs
	public static TaskProvider<ProcessResources> configureProcessResources(Project project, SourceSet set, Map<String, Object>... maps) {
		Map<String, Object> vars = plus(maps);
		
		return project.getTasks().named(set.getProcessResourcesTaskName(), ProcessResources.class, it -> {
			//h't to multiloader-template for this idea, implemented by MrAmericanMike
			//https://github.com/jaredlll08/MultiLoader-Template/blob/2450032d7e14b296df24c519d072310199a23f75/buildSrc/src/main/groovy/multiloader-common.gradle#L85
			
			Map<String, Object> jsonEscaped = new HashMap<>(vars);
			for(Map.Entry<String, Object> e : jsonEscaped.entrySet()) {
				String oString = e.getValue().toString();
				if(oString.contains("\"") || oString.contains("\n"))
					e.setValue(oString.replace("\"", "\\\"").replace("\n", "\\\n"));
			}
			
			it.filesMatching(List.of("pack.mcmeta", "fabric.mod.json", "*.mixins.json"), jsons -> jsons.expand(jsonEscaped));
			it.filesMatching(List.of("META-INF/neoforge.mods.toml"), tomls -> tomls.expand(vars));
			
			it.getInputs().properties(vars);
		});
	}
}

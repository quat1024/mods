package agency.highlysuspect.modsetup;

import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

import java.util.Collection;
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
}

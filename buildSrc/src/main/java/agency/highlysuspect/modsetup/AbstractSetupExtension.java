package agency.highlysuspect.modsetup;

import net.fabricmc.loom.api.LoomGradleExtensionAPI;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConsumableConfiguration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.jvm.tasks.Jar;
import org.gradle.language.jvm.tasks.ProcessResources;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class AbstractSetupExtension {
	public AbstractSetupExtension(Project project) {
		this.project = project;
		this.sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
	}
	
	public final Project project;
	public final SourceSetContainer sourceSets;
	
	public SourceSet makeSourceSetWithCommonDeps(String name) {
		SourceSet set = sourceSets.create(name);
		withImplementation(set, Util.broadlyApplicableDeps(project));
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
				for(SourceSet base : bases) {
					it.add(project.getDependencies().create(base.getOutput()));
				}
			}));
	}
	
	//really awesome api you have there gradle
	public Dependency depOnMyConfiguration(NamedDomainObjectProvider<ConsumableConfiguration> c) {
		return project.getDependencies().project(Map.of("path", project.getPath(), "configuration", c.getName()));
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
	
	protected void flatten(Object obj, Consumer<Object> act) {
		if(obj instanceof Object[] arr) for(Object elem : arr) flatten(elem, act);
		else if(obj instanceof Collection<?> c) for(Object elem : c) flatten(elem, act);
		else act.accept(obj);
	}
	
	public final void configureProcessResources(SourceSet target, Map<String, Object> vars) {
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
	
	public void devlibs(AbstractArchiveTask it, @Nullable LoomGradleExtensionAPI loom) {
		//loom changes `jar`'s dest dir to ./build/devlibs, and uses a separate RemapJarTask to create
		//the jar in ./build/libs. so this idiom will move a task to the devlibs folder iff `jar` was
		//moved by loom
		if(loom != null) {
			TaskProvider<Jar> mainJarTask = project.getTasks().named("jar", Jar.class);
			it.getDestinationDirectory().set(mainJarTask.flatMap(AbstractArchiveTask::getDestinationDirectory));
			it.getArchiveClassifier().set("dev");
		}
	}
	
	public void setCompatLevel(SourceSet set, int compatLevel) {
		project.getLogger().lifecycle("setting compat level for {} to {}", set.getCompileJavaTaskName(), compatLevel);
		project.getTasks().named(set.getCompileJavaTaskName(), JavaCompile.class, it -> {
			it.getOptions().getRelease().set(compatLevel);
		});
	}
	
	//intended to be used from gradle api
	public Dependency lib(String filename) {
		return project.getDependencies().create(project.files("../lib/" + filename));
	}
}

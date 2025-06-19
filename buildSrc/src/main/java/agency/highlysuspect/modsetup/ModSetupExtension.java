package agency.highlysuspect.modsetup;

import net.fabricmc.loom.api.LoomGradleExtensionAPI;
import net.fabricmc.loom.configuration.ide.RunConfigSettings;
import net.fabricmc.loom.task.AbstractRunTask;
import net.fabricmc.loom.task.RemapJarTask;
import net.neoforged.moddevgradle.dsl.NeoForgeExtension;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.jvm.tasks.Jar;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Map;

public class ModSetupExtension {
	public ModSetupExtension(Project project) {
		this.project = project;
		this.mods = project.getObjects().domainObjectContainer(Mod.class);
	}
	
	public final Project project;
	
	public String loader;
	public String ver;
	public NamedDomainObjectContainer<Mod> mods;
	
	//"out parameters"
	public TaskProvider<Jar> quatlibFatJar;
	public TaskProvider<RemapJarTask> quatlibFatJarNamedLoom;
	
	public String modVersion(String mod, String version) {
		String mod2 = mod == null ? "Any" : StringGroovyMethods.capitalize(mod);
		String ver2 = version == null ? "Any" : version.replace('.', '_');
		return "mod" + mod2 + "Version" + ver2;
	}
	
	public String modVersionLoader(String mod, String version) {
		return modVersion(mod, version) + "Loader" + StringGroovyMethods.capitalize(loader);
	}
	
	//dependency on a particular consumable configuration of :vanilla
	public Dependency vanillaDep(String mod, String version) {
		return project.getDependencies().project(Map.of(
			"path", ":vanilla",
			"configuration", modVersion(mod, version) + "Elements"
		));
	}
	
	//reference to a source set from :vanilla (somewhat brittle, relies on evaluationDependsOn)
	//only used by neoforge because of its janky run config system
	public SourceSet vanillaSourceSet(String mod, String version) {
		Project vanilla = project.project(":vanilla");
		SourceSetContainer vanillaSourceSets = vanilla.getExtensions().getByType(SourceSetContainer.class);
		return vanillaSourceSets.getByName(modVersion(mod, version));
	}
	
	public void go(Action<? super ModSetupExtension> act) {
		act.execute(this);
		doIt();
	}
	//where the sausage is made :tm:
	
	public void doIt() {
		if(ver == null) throw new IllegalStateException("version not set");
		if(loader == null) throw new IllegalStateException("loader not set");
		
		//abbreviations
		SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
		SourceSet main = sourceSets.getByName("main");
		DependencyHandler dependencies = project.getDependencies();
		ConfigurationContainer configurations = project.getConfigurations();
		TaskContainer tasks = project.getTasks();
		
		//is loom or neoforge installed?
		@Nullable LoomGradleExtensionAPI loom = project.getExtensions().findByType(LoomGradleExtensionAPI.class);
		@Nullable NeoForgeExtension neoforge = project.getExtensions().findByType(NeoForgeExtension.class);
		
		if(loom != null) {
			//loom has been applied by FabricSetupPlugin already
			//setup minecraft with official names
			dependencies.add("minecraft", "com.mojang:minecraft:" + ver);
			dependencies.add("mappings", loom.officialMojangMappings());
			
			//run config stuff would go here
			RunConfigSettings client = loom.getRuns().maybeCreate("client");
			client.client(); //client client
			client.setIdeConfigGenerated(true);
		}
		
		//neoforge: a basic run configuration
		if(neoforge != null) {
			neoforge.getRuns().create("client", it -> {
				//noinspection Convert2MethodRef
				it.client();
			});
		}
		
		//add configurations
		Configuration quatlibVanilla = configurations.create("quatlibVanilla");
		configurations.getByName("implementation", it -> it.extendsFrom(quatlibVanilla));
		
		//add dependencies
		dependencies.add(quatlibVanilla.getName(), vanillaDep(null, null));
		dependencies.add(quatlibVanilla.getName(), vanillaDep(null, ver));
		
		//produce quatlib jar
		quatlibFatJar = project.getTasks().register("quatlibFatJar", Jar.class, it -> {
			it.from(main.getOutput());
			//from configurations.quatlibVanilla.collect { zipTree it }
			for(File f : quatlibVanilla) {
				it.from(project.zipTree(f));
			}
			
			it.getArchiveBaseName().set("ModderNameLib-" + ver + "-" + loader);
			if(loom != null) { //needs remapping
				it.getArchiveClassifier().set("dev");
				devlibs(it);
			}
		});
		tasks.named("jar", it -> it.dependsOn(quatlibFatJar));
		
		//neoforge: put quatlib on a run config
		if(neoforge != null) {
			neoforge.getMods().create("quatlib", it -> {
				it.sourceSet(main);
				it.sourceSet(vanillaSourceSet(null, null));
				it.sourceSet(vanillaSourceSet(null, ver));
			});
		}
		
		//loom: remap quatlib
		if(loom != null) {
			quatlibFatJarNamedLoom = tasks.register("quatlibFatJarNamed", RemapJarTask.class, it -> {
				it.getArchiveBaseName().set("ModderNameLib-" + ver + "-" + loader);
				it.getInputFile().value(quatlibFatJar.flatMap(AbstractArchiveTask::getArchiveFile));
				it.dependsOn(quatlibFatJar);
			});
			tasks.named("jar", it -> it.dependsOn(quatlibFatJarNamedLoom));
		}
		
		//for each mod..
		for(Mod modOptions : mods) {
			String mod = modOptions.getName();
			
			//make a source-set for it
			SourceSet set = sourceSets.create(mod);
			modOptions.set = set;
			
			//inherit from main source-set (containing minecraft, the modloader, and code shared across all mods using the loader)
			set.setCompileClasspath(set.getCompileClasspath().plus(main.getCompileClasspath()));
			set.setRuntimeClasspath(set.getRuntimeClasspath().plus(main.getRuntimeClasspath()));
			
			//inherit from relevant :vanilla projects
			Configuration modSplat = project.getConfigurations().create(mod + "Splat");
			dependencies.add(modSplat.getName(), vanillaDep(mod, null));
			dependencies.add(modSplat.getName(), vanillaDep(mod, ver));
			modOptions.splat = modSplat;
			
			//plug this thing in... hmm
			dependencies.add(set.getImplementationConfigurationName(), main.getOutput());
			dependencies.add(set.getImplementationConfigurationName(), dependencies.create(quatlibVanilla));
			dependencies.add(set.getImplementationConfigurationName(), dependencies.create(modSplat));
			
			//TODO testing
			//Configuration modSplat1 = project.getConfigurations().create(mod + "Splat1");
//			Configuration modSplat2 = project.getConfigurations().create(mod + "Splat2");
			//dependencies.add(modSplat1.getName(), vanillaDep(mod, null));
//			dependencies.add(modSplat2.getName(), vanillaDep(mod, ver));
			//TODO: the following line appears to be responsible for IDEA showing too many
			// minecraft versions in autocomplete and such
//			dependencies.add(impl, dependencies.create(modSplat2));
			
			//loom: slap it on the classpath of all run configs
			if(loom != null) {
				tasks.withType(AbstractRunTask.class).configureEach(it ->
					it.classpath(set.getRuntimeClasspath()));
			}
			
			//a "dep jar". all mod-specific code is splatted into it, but
			//all non-mod-specific code is expected to be supplied via dependency
			TaskProvider<Jar> depJar = tasks.register(modVersionLoader(mod, ver) + "DepJar", Jar.class, it -> {
				it.from(set.getOutput());
				for(File splat : modSplat) it.from(project.zipTree(splat));
				it.getArchiveBaseName().set(mod + "-" + ver + "-" + loader);
				
				if(loom != null) { //needs remapping
					it.getArchiveClassifier().set("dev");
					devlibs(it);
				}
			});
			tasks.named("jar", it -> it.dependsOn(depJar));
			modOptions.depJar = depJar;
			
			//neoforge: put this mod on a run config
			if(neoforge != null) {
				neoforge.getMods().create(mod, it -> {
					it.sourceSet(set);
					it.sourceSet(vanillaSourceSet(mod, ver));
					it.sourceSet(vanillaSourceSet(mod, null));
				});
			}
			
			//loom: remap
			if(loom != null) {
				TaskProvider<RemapJarTask> depJarNamed = tasks.register(depJar.getName() + "Named", RemapJarTask.class, it -> {
					it.dependsOn(depJar, quatlibFatJar, quatlibFatJarNamedLoom);
					
					it.getArchiveBaseName().set(mod + "-" + ver + "-" + loader);
					it.getInputFile().set(depJar.flatMap(AbstractArchiveTask::getArchiveFile));
					
					//put quatlib-fat-dev on the remap classpath so tiny-remapper can see into it
					it.getClasspath().from(quatlibFatJar.get().getArchiveFile());
					
					//for some reason you can JiJ stuff from RemapJarTask?
					//not sure what that has to do with remapping but ok :thumbs_up: sure
					it.getNestedJars().from(quatlibFatJarNamedLoom.get().getArchiveFile());
					it.getAddNestedDependencies().set(true);
				});
				tasks.named("jar", it -> it.dependsOn(depJarNamed));
			}
		}
	}
	
	private void devlibs(AbstractArchiveTask it) {
		//loom changes `jar`'s dest dir to ./build/devlibs, and uses a separate RemapJarTask to create
		//the jar in ./build/libs. so this idiom will move a task to the devlibs folder iff `jar` was
		//moved by loom, and do nothing otherwise
		TaskProvider<Jar> mainJarTask = project.getTasks().named("jar", Jar.class);
		it.getDestinationDirectory().set(mainJarTask.flatMap(AbstractArchiveTask::getDestinationDirectory));
	}
}

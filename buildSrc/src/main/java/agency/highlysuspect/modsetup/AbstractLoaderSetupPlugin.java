package agency.highlysuspect.modsetup;

import net.fabricmc.loom.api.LoomGradleExtensionAPI;
import net.fabricmc.loom.configuration.ide.RunConfigSettings;
import net.fabricmc.loom.task.AbstractRunTask;
import net.fabricmc.loom.task.RemapJarTask;
import net.neoforged.moddevgradle.dsl.NeoForgeExtension;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
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

public abstract class AbstractLoaderSetupPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getLogger().lifecycle("HELLO FROM LoaderSetupPlugin");
		
		//apply java while we're here
		project.getPlugins().apply("java");
		
		//ext
		project.getExtensions().create("modSetup", Ext.class, project);
	}
	
	protected Ext getExt(Project project) {
		return project.getExtensions().getByType(Ext.class);
	}
	
	public static class Ext {
		public Ext(Project project) {
			this.project = project;
			this.mods = project.getObjects().domainObjectContainer(LoaderMod.class);
		}
		
		public final Project project;
		
		//TODO should these be the gradle "property" things lol
		public String loader;
		public String ver;
		public NamedDomainObjectContainer<LoaderMod> mods;
		
		//janky little "out parameters", exposed as fields in case other bits of the code need em...
		public TaskProvider<Jar> quatlibFatJar;
		public TaskProvider<RemapJarTask> quatlibFatJarNamedLoom;
		
		public void go(Action<? super Ext> act) {
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
			
			//we don't use the default jar task
			tasks.named("jar", it -> it.setEnabled(false));
			
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
				
				//also disable the default remapJar task since `jar` was disabled
				tasks.named("remapJar", it -> it.setEnabled(false));
			}
			
			//neoforge: a basic run configuration
			if(neoforge != null) {
				neoforge.getRuns().create("client", it -> {
					//noinspection Convert2MethodRef
					it.client();
				});
			}
			
			//a source-set for quatlib (code shared across all mods on this loader)
			SourceSet quatlib = sourceSets.create("quatlib");
			
			//it should see minecraft (which the mc ecosystem plugin has put in `main`)
			Util.extendSourceSetFrom(quatlib, main);
			
			//it should see the applicable-to-all-mods code from :vanilla
			Util.withImplementation(project, quatlib,
				Util.vanillaDep(project, null, null),
				Util.vanillaDep(project, null, ver)
			);
			//and on fabric it should also see :floader-only
			if(loom != null) {
				Util.withImplementation(project, quatlib, Util.floaderOnlyDep(project));
			}
			
			//process resources
			Util.configureProcessResources(project, quatlib,
				Util.broadlyApplicableProps(project),
				Map.of(
					"modid", "modder_name_lib",
					"name", "ModderNameLib",
					"loader", loader,
					"minecraft_version", ver
				)
			);
			
			//produce quatlib jar
			quatlibFatJar = project.getTasks().register("quatlibFatJar", Jar.class, it -> {
				it.from(quatlib.getOutput());
				
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
					it.sourceSet(quatlib);
					it.sourceSet(Util.vanillaSourceSet(project, null, null));
					it.sourceSet(Util.vanillaSourceSet(project, null, ver));
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
			for(LoaderMod loaderModOptions : mods) {
				String mod = loaderModOptions.getName();
				
				//make a source-set for it
				SourceSet set = sourceSets.create(mod);
				loaderModOptions.set = set;
				
				Util.configureProcessResources(project, set,
					Util.broadlyApplicableProps(project),
					Map.of("minecraft_version", ver, "loader", loader),
					loaderModOptions.vars
				);
				
				//inherit from main source-set (containing minecraft, the modloader, and code shared across all mods using the loader)
				Util.extendSourceSetFrom(set, main);
				
				//if quatlib is used, inherit from that too
				if(loaderModOptions.quatlib) {
					Util.extendSourceSetFrom(set, quatlib);
				}
				
				//inherit from relevant :vanilla projects
				Configuration modSplat = project.getConfigurations().create(mod + "Splat");
				Util.withDeps(project, modSplat,
					Util.vanillaDep(project, mod, null),
					Util.vanillaDep(project, mod, ver)
				);
				loaderModOptions.splat = modSplat;
				
				//plug this thing in... hmm
				Util.withImplementation(project, set,
					quatlib.getOutput(),
					modSplat,
					Util.broadlyApplicableDeps(project)
				);
				if(loaderModOptions.quatlib) {
					Util.withImplementation(project, set, quatlib.getOutput());
				}
//				dependencies.add(set.getImplementationConfigurationName(), main.getOutput());
//				dependencies.add(set.getImplementationConfigurationName(), dependencies.create(quatlibVanilla));
//				dependencies.add(set.getImplementationConfigurationName(), dependencies.create(modSplat));
				
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
				//all non-mod-specific code is expected to be supplied via quatlib dependency
				TaskProvider<Jar> depJar = tasks.register(Util.modVersionLoader(mod, ver, loader) + "DepJar", Jar.class, it -> {
					it.from(set.getOutput());
					for(File splat : modSplat) it.from(project.zipTree(splat));
					it.getArchiveBaseName().set(mod + "-" + ver + "-" + loader);
					
					if(loom != null) { //needs remapping
						it.getArchiveClassifier().set("dev");
						devlibs(it);
					}
				});
				tasks.named("jar", it -> it.dependsOn(depJar));
				loaderModOptions.depJar = depJar;
				
				//neoforge: put this mod on a run config
				if(neoforge != null) {
					neoforge.getMods().create(mod, it -> {
						it.sourceSet(set);
						it.sourceSet(Util.vanillaSourceSet(project, mod, ver));
						it.sourceSet(Util.vanillaSourceSet(project, mod, null));
					});
				}
				
				//loom: remap
				if(loom != null) {
					TaskProvider<RemapJarTask> depJarNamed = tasks.register(depJar.getName() + "Named", RemapJarTask.class, it -> {
						it.dependsOn(depJar, quatlibFatJar, quatlibFatJarNamedLoom);
						
						it.getArchiveBaseName().set(mod + "-" + ver + "-" + loader);
						it.getInputFile().set(depJar.flatMap(AbstractArchiveTask::getArchiveFile));
						
						if(loaderModOptions.quatlib) {
							//put quatlib-fat-dev on the remap classpath so tiny-remapper can see into it
							it.getClasspath().from(quatlibFatJar.get().getArchiveFile());
							
							//for some reason you can JiJ stuff from RemapJarTask?
							//not sure what that has to do with remapping but ok :thumbs_up: sure
							it.getNestedJars().from(quatlibFatJarNamedLoom.get().getArchiveFile());
							it.getAddNestedDependencies().set(true);
						}
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
}

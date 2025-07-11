package agency.highlysuspect.modsetup;

import net.fabricmc.loom.LoomGradlePlugin;
import net.fabricmc.loom.api.LoomGradleExtensionAPI;
import net.fabricmc.loom.configuration.ide.RunConfigSettings;
import net.fabricmc.loom.task.AbstractRunTask;
import net.fabricmc.loom.task.RemapJarTask;
import net.neoforged.moddevgradle.boot.ModDevPlugin;
import net.neoforged.moddevgradle.dsl.NeoForgeExtension;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.jvm.tasks.Jar;
import org.gradle.language.jvm.tasks.ProcessResources;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Map;

public abstract class AbstractLoaderSetupPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.evaluationDependsOn(":vanilla"); //make sure :vanilla is evalled first
		project.getPlugins().apply("java"); //apply java while we're here
		project.getExtensions().create("modSetup", Ext.class, project);
	}
	
	public static class NeoforgeSetupPlugin extends AbstractLoaderSetupPlugin {
		@Override
		public void apply(Project project) {
			super.apply(project);
			project.getPlugins().apply(ModDevPlugin.class); //apply ModDevGradle
			getExt(project).loader = "neoforge";
		}
	}
	
	public static class FabricSetupPlugin extends AbstractLoaderSetupPlugin {
		@Override
		public void apply(Project project) {
			super.apply(project);
			project.getPlugins().apply(LoomGradlePlugin.class);
			getExt(project).loader = "fabric";
		}
	}
	
	protected Ext getExt(Project project) {
		return project.getExtensions().getByType(Ext.class);
	}
	
	public static class Ext extends AbstractSetupExtension {
		public Ext(Project project) {
			super(project);
			
			//reach across and get vanilla (evaluationDependsOn was set up so this should be ok)
			Project vanilla = project.project(":vanilla");
			VanillaSetupPlugin.Ext vanillaExt = vanilla.getExtensions().getByType(VanillaSetupPlugin.Ext.class);
			NamedDomainObjectContainer<VanillaMod> vanillaMods = vanillaExt.getVanillaMods();
			
			this.mods = project.getObjects().domainObjectContainer(LoaderMod.class, modid ->
				new LoaderMod(vanillaMods.getByName(modid)));
		}
		
		//TODO should these be the gradle "property" things lol
		public String loader;
		public String ver;
		public NamedDomainObjectContainer<LoaderMod> mods;
		
		public void go(Action<? super Ext> act) {
			act.execute(this);
			doIt();
		}
		
		//where the sausage is made :tm:
		public void doIt() {
			if(ver == null) throw new IllegalStateException("version not set");
			if(loader == null) throw new IllegalStateException("loader not set");
			
			int javaCompatLevel = Util.compatLevelForMinecraft(ver);
			
			//reach across and get vanilla (evaluationDependsOn was set up so this should be ok)
			Project vanilla = project.project(":vanilla");
			VanillaSetupPlugin.Ext vanillaExt = vanilla.getExtensions().getByType(VanillaSetupPlugin.Ext.class);
			
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
				//set up official names on loom (just so i don't need to do it in the buildscript)
				dependencies.add("minecraft", "com.mojang:minecraft:" + ver);
				dependencies.add("mappings", loom.officialMojangMappings());
				
				//disable the default remapJar since we don't use the default `jar` task
				tasks.named("remapJar", it -> it.setEnabled(false));
			}
			
			/// SOURCE SET SCAFFOLDING ///
			project.getLogger().lifecycle("making source sets");
			
			//code shared across all mods on this loader
			SourceSet quatlib = makeSourceSetWithCommonDeps("quatlib");
			extendSourceSet2(quatlib, main); //contains minecraft + loader code
			withImplementation(quatlib, vanillaExt.dependOnModAgnostic(ver)); //mod-agnostic code from :vanilla
			if(loom != null)
				withImplementation(quatlib, Util.floaderOnlyDep(project));
			//java version
			setCompatLevel(quatlib, javaCompatLevel);
			
			//quatlib code that gets put in the final quatlib jar...?
			//this duplicates the other thing, meh
			Configuration quatlibSplat = project.getConfigurations().create("quatlibSplat");
			withDeps(quatlibSplat, vanillaExt.dependOnModAgnostic(ver));
			if(loom != null)
				withDeps(quatlibSplat, Util.floaderOnlyDep(project));
			
			//one source-set per mod
			for(LoaderMod mod : mods) {
				project.getLogger().lifecycle("...for {}", mod.modid);
				mod.set = makeSourceSetWithCommonDeps(mod.modid);
				
				extendSourceSet2(mod.set, main);
				if(mod.quatlib)
					extendSourceSet2(mod.set, quatlib);
				
				setCompatLevel(mod.set, javaCompatLevel);
			}
			
			/// DEPENDENCIES ///
			project.getLogger().lifecycle("preparing deps");
			
			for(LoaderMod mod : mods) {
				//configuration containing code splatted into the mod jar
				mod.splat = project.getConfigurations().create(mod.modid + "Splat");
				withDeps(mod.splat, vanillaExt.dependOnVersionAndModSpecific(mod, ver));
				
				//TODO: whats this do
				withImplementation(mod.set, mod.splat);
			}
			
			/// PROCESS RESOURCES ///
			project.getLogger().lifecycle("configuring processResources");
			
			Map<String, Object> allVars = Util.plus(Util.broadlyApplicableProps(project), Map.of(
				"minecraft_version", ver,
				"loader", loader
			));
			
			//quatlib
			configureProcessResources(quatlib, allVars, Map.of(
				"modid", "modder_name_lib",
				"name", "ModderNameLib"
			));
			
			//mods
			for(LoaderMod mod : mods) {
				configureProcessResources(mod.set, allVars, mod.vars);
				
				//include the resources from vanilla too
				tasks.named(mod.set.getProcessResourcesTaskName(), ProcessResources.class, it -> {
					it.from(
						mod.versionAgnosticSourceSet.getResources(),
						mod.perVersionSourceSets.get(ver).getResources()
					);
				});
			}
			
			/// JARS ///
			project.getLogger().lifecycle("preparing jars");
			
			//produce quatlib fatjar
			TaskProvider<Jar> quatlibFatJar = project.getTasks().register("quatlibFatJar", Jar.class, it -> {
				it.from(quatlib.getOutput());
				for(File splat : quatlibSplat) it.from(project.zipTree(splat));
				
				it.getArchiveBaseName().set("ModderNameLib-" + ver + "-" + loader);
				devlibs(it, loom);
			});
			tasks.named("jar", it -> it.dependsOn(quatlibFatJar));
			
			for(LoaderMod mod : mods) {
				//contains all mod-specific code, including some splatted from other projects, but none of the quatlib code
				mod.depJar = tasks.register(mod.modid + "DepJar", Jar.class, it -> {
					it.from(mod.set.getOutput()); //code for this version of this mod
					for(File splat : mod.splat) it.from(project.zipTree(splat)); //code for all versions of this mod, basically
//					it.from(
//						mod.versionAgnosticSourceSet.getResources(),
//						mod.perVersionSourceSets.get(ver).getResources()
//					); //already done in processResources
					
					it.getArchiveBaseName().set(mod.modid + "-" + ver + "-" + loader);
					
					devlibs(it, loom);
					
					//TODO: kludge, i'm picking up dupe resources from somewhere...
					it.setDuplicatesStrategy(DuplicatesStrategy.INCLUDE);
				});
				tasks.named("jar", it -> it.dependsOn(mod.depJar));
			}
			
			/// REMAPPED JARS ///
			
			if(loom != null) {
				project.getLogger().lifecycle("preparing loom remapped jars");
				
				TaskProvider<RemapJarTask> quatlibFatJarNamedLoom = tasks.register("quatlibFatJarNamed", RemapJarTask.class, it -> {
					it.getArchiveBaseName().set("ModderNameLib-" + ver + "-" + loader);
					it.getInputFile().value(quatlibFatJar.flatMap(AbstractArchiveTask::getArchiveFile));
					it.dependsOn(quatlibFatJar);
				});
				tasks.named("jar", it -> it.dependsOn(quatlibFatJarNamedLoom));
				
				for(LoaderMod mod : mods) {
					TaskProvider<RemapJarTask> depJarNamed = tasks.register(mod.depJar.getName() + "Named", RemapJarTask.class, it -> {
						it.dependsOn(mod.depJar, quatlibFatJar, quatlibFatJarNamedLoom);
						
						it.getArchiveBaseName().set(mod.modid + "-" + ver + "-" + loader);
						it.getInputFile().set(mod.depJar.flatMap(AbstractArchiveTask::getArchiveFile));
						
						if(mod.quatlib) {
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
			
			/// RUN CONFIGS ///
			project.getLogger().lifecycle("setting up run configs");
			
			if(loom != null) {
				RunConfigSettings client = loom.getRuns().maybeCreate("client");
				client.client(); //client client
				client.setIdeConfigGenerated(true);
				
				//put all mods on the runtime classpath
				tasks.withType(AbstractRunTask.class).configureEach(it -> {
					for(LoaderMod mod : mods) {
						it.classpath(mod.set.getRuntimeClasspath());
					}
				});
			}
			
			if(neoforge != null) {
				neoforge.getRuns().create("client", it -> {
					it.client();
				});
				
				//quatlib
				neoforge.getMods().create("modder_name_lib", it -> {
					it.sourceSet(quatlib);
					it.sourceSet(vanillaExt.modAnyVersionAny);
					it.sourceSet(vanillaExt.modAgnosticSourceSets.get(ver));
				});
				
				//each mod
				for(LoaderMod mod : mods) {
					neoforge.getMods().create(mod.modid, it -> {
						it.sourceSet(mod.set);
						it.sourceSet(mod.versionAgnosticSourceSet);
						it.sourceSet(mod.perVersionSourceSets.get(ver));
					});
				}
			}
		}
	}
}

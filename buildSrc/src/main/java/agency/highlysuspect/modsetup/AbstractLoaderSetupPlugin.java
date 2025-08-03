package agency.highlysuspect.modsetup;

import net.fabricmc.loom.LoomGradlePlugin;
import net.fabricmc.loom.api.LoomGradleExtensionAPI;
import net.fabricmc.loom.configuration.ide.RunConfigSettings;
import net.fabricmc.loom.task.AbstractRunTask;
import net.fabricmc.loom.task.RemapJarTask;
import net.neoforged.moddevgradle.boot.LegacyForgeModDevPlugin;
import net.neoforged.moddevgradle.boot.ModDevPlugin;
import net.neoforged.moddevgradle.dsl.ModDevExtension;
import net.neoforged.moddevgradle.dsl.NeoForgeExtension;
import net.neoforged.moddevgradle.legacyforge.dsl.LegacyForgeExtension;
import net.neoforged.moddevgradle.legacyforge.dsl.ObfuscationExtension;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.component.AdhocComponentWithVariants;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.jvm.tasks.Jar;
import org.gradle.language.jvm.tasks.ProcessResources;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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
			project.getPlugins().apply(ModDevPlugin.class); //from ModDevGradle
			getExt(project).loader = "neoforge";
		}
	}
	
	public static class ForgeViaMdgSetupPlugin extends AbstractLoaderSetupPlugin {
		@Override
		public void apply(Project project) {
			super.apply(project);
			project.getPlugins().apply(LegacyForgeModDevPlugin.class); //from ModDevGradle
			getExt(project).loader = "forge";
		}
	}
	
	public static class FabricSetupPlugin extends AbstractLoaderSetupPlugin {
		@Override
		public void apply(Project project) {
			super.apply(project);
			project.getPlugins().apply(LoomGradlePlugin.class); //from Loom
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
		
		@SuppressWarnings("unused")
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
			
			LoaderMod quatlib = mods.create("modder_name_lib", it -> {
				it.vars.put("name", "ModderNameLib");
				it.vars.put("description", "ModderNameLib for " + loader + " " + ver);
			});
			
			//abbreviations
			SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
			SourceSet main = sourceSets.getByName("main");
			DependencyHandler dependencies = project.getDependencies();
			ConfigurationContainer configurations = project.getConfigurations();
			TaskContainer tasks = project.getTasks();
			
			//we don't use the default jar task
			tasks.named("jar", it -> it.setEnabled(false));
			
			//just in case we need mixin
			addSpongeRepo();
			
			//which ecosystem plugin to talk with
			@Nullable LoomGradleExtensionAPI loom2 = null;
			@Nullable NeoForgeExtension neoforge2 = null;
			@Nullable LegacyForgeExtension legacyForge2 = null;
			@Nullable ModDevExtension mdg2 = null; //neoforge or mdg's legacy forge
			if("fabric".equals(loader)) {
				loom2 = project.getExtensions().getByType(LoomGradleExtensionAPI.class);
			}
			if("neoforge".equals(loader)) {
				neoforge2 = project.getExtensions().getByType(NeoForgeExtension.class);
				mdg2 = neoforge2;
			}
			if("forge".equals(loader)) {
				legacyForge2 = project.getExtensions().findByType(LegacyForgeExtension.class);
				mdg2 = legacyForge2;
			}
			//dontcha love lambdas
			@Nullable LoomGradleExtensionAPI loom = loom2;
			@Nullable NeoForgeExtension neoforge = neoforge2;
			@Nullable LegacyForgeExtension legacyForge = legacyForge2;
			@Nullable ModDevExtension mdg = mdg2;
			
			if(loom != null) {
				//set up official names on loom (just so i don't need to do it in the buildscript)
				dependencies.add("minecraft", "com.mojang:minecraft:" + ver);
				dependencies.add("mappings", loom.officialMojangMappings());
				
				//disable the default remapJar since we don't use the default `jar` task
				tasks.named("remapJar", it -> it.setEnabled(false));
			}
			
			if(legacyForge != null) {
				//ditto
				tasks.named("reobfJar", it -> it.setEnabled(false));
			}
			
			/// SOURCE SET SCAFFOLDING ///
			project.getLogger().lifecycle("making source sets");
			
			for(LoaderMod mod : mods) {
				mod.set = makeSourceSetWithCommonDeps(mod.modid);
				extendSourceSet2(mod.set, main);
				setCompatLevel(mod.set, javaCompatLevel);
				if(loom != null) loom.createRemapConfigurations(mod.set);
			}
			
			for(LoaderMod mod : mods) {
				if(mod.dependOnQuatlib)
					extendSourceSet2(mod.set, quatlib.set);
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
			//add :floader-only if i'm quatlib
			if(loom != null)
				withImplementation(quatlib.set, Util.floaderOnlyDep(project));
			
			/// PROCESS RESOURCES ///
			project.getLogger().lifecycle("configuring processResources");
			
			Map<String, Object> allVars = Util.plus(Util.broadlyApplicableProps(project), Map.of(
				"minecraft_version", ver,
				"loader", loader
			));
			for(LoaderMod mod : mods) {
				configureProcessResources(mod.set, Util.plus(allVars, mod.vars));
				
				//include the resources from things living in :vanilla too
				tasks.named(mod.set.getProcessResourcesTaskName(), ProcessResources.class, it -> {
					it.from(
						mod.versionAgnosticSourceSet.getResources(),
						mod.getPerVersionSourceSet(ver).getResources()
					);
				});
			}
			
			/// JARS ///
			project.getLogger().lifecycle("preparing jars");
			
			for(LoaderMod mod : mods) {
				//contains all mod-specific code, including some splatted from other projects
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
					it.setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE);
				});
				tasks.named("jar", it -> it.dependsOn(mod.depJar));
			}
			
			/// REMAPPED JARS ///
			if(loom != null) {
				project.getLogger().lifecycle("preparing loom remapped jars");
				
				//do quatlib first
				for(LoaderMod mod : mods) {
					mod.depJarNamedLoom = tasks.register(mod.depJar.getName() + "Named", RemapJarTask.class, it -> {
						it.dependsOn(mod.depJar);
						it.getArchiveBaseName().set(mod.modid + "-" + ver + "-" + loader);
						it.getInputFile().set(mod.depJar.flatMap(AbstractArchiveTask::getArchiveFile));
					});
					tasks.named("jar", it -> it.dependsOn(mod.depJarNamedLoom));
				}
				
				for(LoaderMod mod : mods) {
					if(mod != quatlib && mod.dependOnQuatlib) {
						mod.depJarNamedLoom.configure(it -> {
							it.dependsOn(quatlib.depJar, quatlib.depJarNamedLoom);
							//put quatlib devjar on the remap classpath so tiny-remapper can see into it
							it.getClasspath().from(quatlib.depJar.get().getArchiveFile());
							
							//for some reason you can JiJ stuff from RemapJarTask?
							//not sure what that has to do with remapping but ok :thumbs_up: sure
							it.getNestedJars().from(quatlib.depJarNamedLoom.get().getArchiveFile());
							it.getAddNestedDependencies().set(true);
						});
					}
				}
			}
			
			if(legacyForge != null) {
				project.getLogger().lifecycle("preparing legacyforge remapped jars");
				ObfuscationExtension obf = project.getExtensions().getByType(ObfuscationExtension.class);
				
				List<SourceSet> quatlibAndModSourceSets =
					Stream.concat(Stream.of(quatlib.set), mods.stream().map(it -> it.set)).toList();
				
				//the "obf.reobfuscate" function looks these configurations up by name, and they have to exist.
				//TODO, can i avoid this headache by actually using these configuration names? lol
				quatlibAndModSourceSets.forEach(set -> {
					Configuration runtimeElements = configurations.maybeCreate(set.getRuntimeElementsConfigurationName());
					Configuration apiElements = configurations.maybeCreate(set.getApiElementsConfigurationName());
					withDeps(runtimeElements, set.getOutput());
					withDeps(apiElements, set.getOutput());
					
					//mdg calls withVariantsForConfiguration before addVariantsForConfiguration
					//and withVars requires the thing to be registered via addVars first, apparently
					//"Variant for configuration ... does not exist in ..."
					//I think this qualifies as an mdg bug honestly, TODO repro in a more normal environment
					AdhocComponentWithVariants umm = (AdhocComponentWithVariants) project.getComponents().getByName("java");
					umm.addVariantsFromConfiguration(runtimeElements, it -> {});
					umm.addVariantsFromConfiguration(apiElements, it -> {});
				});
				
				//reobf jar tasks
				for(LoaderMod mod : mods) {
					obf.reobfuscate(mod.depJar, mod.set, it -> {
						it.getArchiveBaseName().set(mod.modid + "-" + ver + "-" + loader);
					});
				}
			}
			
			/// REFMAPS ///
			//TODO: doesn't work on fabric, untested on legacyforge
			if(legacyForge != null || loom != null) {
				File scratchDir = project.getLayout().getBuildDirectory().dir("mixin2").get().getAsFile();
				scratchDir.mkdirs();
				
				project.afterEvaluate(__ -> {
					if(loom != null) project.getLogger().lifecycle("bbbbbbbLOOM MAPPINGS FILE: {}", loom.getMappingsFile());
				});
				
				//grab the mixin ap and slap it in a configuration so i can refer to it later
				Configuration mixinAp = configurations.maybeCreate("mixinAp");
				if(neoforge != null) {
					//mixin ap supports ObfuscationServiceMCP out of the box
					withDeps(mixinAp, "org.spongepowered:mixin:0.8.5:processor");
				} else { //loom
					//this version adds ObfuscationServiceFabric which supports the "named:intermediary" obfuscation type
					//also it depends on its own copy of the sponge mixin ap, so that gets pulled in
					withDeps(mixinAp, "net.fabricmc:fabric-mixin-compile-extensions:0.6.0");
				}
				
				for(LoaderMod mod : mods) {
					File scratch2 = new File(scratchDir, "scratch-" + mod.modid);
					
					//all sources which touch a specific version of minecraft and might contain
					//mixins which need refmaps
					FileCollection sources = mod.set.getAllSource()
						.plus(mod.perVersionSourceSets.get(ver).getAllSource());
					
					FileCollection classpath = mod.set.getCompileClasspath()
						.plus(mod.perVersionSourceSets.get(ver).getCompileClasspath())
						.plus(mod.versionAgnosticSourceSet.getCompileClasspath());
					
					//no need to make a classpath for the mixin AP itself;
					//it shadows all its dependencies
					
					TaskProvider<JavaCompile> generateRefmaps = project.getTasks().register("generate" + StringGroovyMethods.capitalize(mod.modid) + "Refmap", JavaCompile.class, it -> {
						it.setGroup("build");
						
						//TODO: really we depend on the *classes* tasks, not the jars
						it.dependsOn(mod.versionAgnosticJar, mod.perVersionJars.get(ver));
						
						it.setSource(sources);
						it.setClasspath(classpath);
						it.getOptions().setAnnotationProcessorPath(mixinAp);
						it.getDestinationDirectory().set(scratch2);
						//it.getOptions().setVerbose(true);
					});
					
					if(legacyForge != null) {
						iLoveRefmapArgs(project, scratchDir, legacyForge, loom, mod, generateRefmaps);
					} else { //loom != null
						//Has to be done in afterEvaluate so loom.getMappingsFile() will work
						project.afterEvaluate(__ -> iLoveRefmapArgs(project, scratchDir, legacyForge, loom, mod, generateRefmaps));
					}
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
			
			if(mdg != null) {
				mdg.getRuns().create("client", it -> {
					it.client();
				});
				
				for(LoaderMod mod : mods) {
					mdg.getMods().create(mod.modid, it -> {
						it.sourceSet(mod.set);
						it.sourceSet(mod.versionAgnosticSourceSet);
						it.sourceSet(mod.getPerVersionSourceSet(ver));
					});
				}
			}
		}
	}
	
	
	protected static void iLoveRefmapArgs(Project project, File scratchDir, LegacyForgeExtension legacyForge, LoomGradleExtensionAPI loom, LoaderMod mod, TaskProvider<JavaCompile> generateRefmaps) {
		File mappingsIn;
		if(legacyForge != null)
			mappingsIn = project.getExtensions().getByType(ObfuscationExtension.class).getNamedToSrgMappings().getAsFile().get();
		else mappingsIn = loom.getMappingsFile();
		
		//forge uses .tsrg, fabric uses .tiny, dont think the extension matters either way
		//TODO: what actually ends up in this file?
		// MDG feeds its contents back into ObfuscationExtension, but so far i've only seen empty files
		File mappingsOut = new File(scratchDir, mod.modid + ".out.txt");
		File refmapOut = new File(scratchDir, mod.modid + ".refmap.json");
		
		//configure mixin AP args
		List<String> mixinArgs = new ArrayList<>();
		mixinArgs.add("-proc:only"); //don't do any compilation, just do APs
		
		if(legacyForge != null)
			Util.legacyForgeMixinArgs(mixinArgs, mappingsIn, mappingsOut, refmapOut);
		else
			Util.fabricMixinArgs(mixinArgs, mappingsIn, mappingsOut, refmapOut);
		
		BoringCommandLineArgumentProvider clap = project.getObjects().newInstance(BoringCommandLineArgumentProvider.class);
		clap.args.set(mixinArgs);
		
		project.getLogger().lifecycle("MIXIN AP ARGS:");
		project.getLogger().lifecycle("{}", mixinArgs);
		
		generateRefmaps.configure(it -> {
			//tell gradle this task creates these files
			it.getOutputs().file(refmapOut);
			it.getOutputs().file(mappingsOut);
			
			//add mixin args
			//it.getOptions().getCompilerArgs().addAll(mixinArgs);
			it.getOptions().getCompilerArgumentProviders().add(clap);
		});
		
		//include refmap in jar
		mod.depJar.configure(it -> {
			it.dependsOn(generateRefmaps);
			it.from(refmapOut);
		});
	}
}

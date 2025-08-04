package agency.highlysuspect.modsetup;

import agency.highlysuspect.modsetup.liason.FabricLiason;
import agency.highlysuspect.modsetup.liason.ForgeViaMdgLiason;
import agency.highlysuspect.modsetup.liason.Liason;
import agency.highlysuspect.modsetup.liason.NeoforgeLiason;
import net.fabricmc.loom.LoomGradlePlugin;
import net.neoforged.moddevgradle.boot.LegacyForgeModDevPlugin;
import net.neoforged.moddevgradle.boot.ModDevPlugin;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.jvm.tasks.Jar;
import org.gradle.language.jvm.tasks.ProcessResources;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractLoaderSetupPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.evaluationDependsOn(":vanilla"); //make sure :vanilla is evalled first
		project.getPlugins().apply("java"); //apply java while we're here
		project.getExtensions().create("modSetup", Ext.class, project, this);
	}
	
	protected Ext getExt(Project project) {
		return project.getExtensions().getByType(Ext.class);
	}
	
	protected abstract Liason createLiason(Project project, String ver, NamedDomainObjectContainer<LoaderMod> mods);
	
	public static class NeoforgeSetupPlugin extends AbstractLoaderSetupPlugin {
		@Override
		public void apply(Project project) {
			super.apply(project);
			project.getPlugins().apply(ModDevPlugin.class); //from ModDevGradle
		}
		
		@Override
		protected Liason createLiason(Project project, String ver, NamedDomainObjectContainer<LoaderMod> mods) {
			return new NeoforgeLiason(project, ver, mods);
		}
	}
	
	public static class ForgeViaMdgSetupPlugin extends AbstractLoaderSetupPlugin {
		@Override
		public void apply(Project project) {
			super.apply(project);
			project.getPlugins().apply(LegacyForgeModDevPlugin.class); //from ModDevGradle
		}
		
		@Override
		protected Liason createLiason(Project project, String ver, NamedDomainObjectContainer<LoaderMod> mods) {
			return new ForgeViaMdgLiason(project, ver, mods);
		}
	}
	
	public static class FabricSetupPlugin extends AbstractLoaderSetupPlugin {
		@Override
		public void apply(Project project) {
			super.apply(project);
			project.getPlugins().apply(LoomGradlePlugin.class); //from Loom
		}
		
		@Override
		protected Liason createLiason(Project project, String ver, NamedDomainObjectContainer<LoaderMod> mods) {
			return new FabricLiason(project, ver, mods);
		}
	}
	
	public static class Ext extends Util {
		public Ext(Project project, AbstractLoaderSetupPlugin self) {
			super(project);
			
			//reach across and get vanilla (evaluationDependsOn was set up so this should be ok)
			Project vanilla = project.project(":vanilla");
			VanillaSetupPlugin.Ext vanillaExt = vanilla.getExtensions().getByType(VanillaSetupPlugin.Ext.class);
			NamedDomainObjectContainer<VanillaMod> vanillaMods = vanillaExt.getVanillaMods();
			
			this.mods = project.getObjects().domainObjectContainer(LoaderMod.class, modid ->
				new LoaderMod(vanillaMods.getByName(modid)));
			
			this.plugin = self; //for creating liason, later
		}
		
		//TODO should these be the gradle "property" things lol
		public String loader;
		public String ver;
		public NamedDomainObjectContainer<LoaderMod> mods;
		
		protected final AbstractLoaderSetupPlugin plugin;
		protected Liason liason;
		
		@SuppressWarnings("unused")
		public void go(Action<? super Ext> act) {
			act.execute(this);
			doIt();
		}
		
		//where the sausage is made :tm:
		public void doIt() {
			if(ver == null) throw new IllegalStateException("version not set");
			int javaCompatLevel = compatLevelForMinecraft(ver);
			
			//liason
			this.liason = plugin.createLiason(project, ver, mods);
			this.loader = liason.getLoaderIdentifier();
			
			LoaderMod quatlib = mods.create("modder_name_lib", it -> {
				it.vars.put("name", "ModderNameLib");
				it.vars.put("description", "ModderNameLib for " + loader + " " + ver);
			});
			
			//abbreviations
			SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
			SourceSet main = sourceSets.getByName("main");
			ConfigurationContainer configurations = project.getConfigurations();
			TaskContainer tasks = project.getTasks();
			
			//disable default tasks that we don't use
			tasks.named("jar", it -> it.setEnabled(false));
			liason.disableUnusedDefaultTasks();
			
			//set up official names
			liason.setupOfficialNames();
			
			/// SOURCE SET SCAFFOLDING ///
			project.getLogger().lifecycle("making source sets");
			for(LoaderMod mod : mods) {
				mod.set = makeSourceSetWithCommonDeps(mod.modid);
				extendSourceSet2(mod.set, main);
				setCompatLevel(mod.set, javaCompatLevel);
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
				withDeps(mod.splat, mod.getSplattedDep(project, ver));
				withImplementation(mod.set, mod.splat); //TODO: whats this do again
			}
			liason.addFloaderOnlyDep(quatlib.set);
			
			/// PROCESS RESOURCES ///
			project.getLogger().lifecycle("configuring processResources");
			Map<String, Object> allVars = plus(broadlyApplicableProps(), Map.of(
				"minecraft_version", ver,
				"loader", loader
			));
			for(LoaderMod mod : mods) {
				configureProcessResources(mod.set, plus(allVars, mod.vars));
				
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
					//TODO: kludge, i'm picking up dupe resources from somewhere...
					it.setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE);
				});
				tasks.named("jar", it -> it.dependsOn(mod.depJar));
			}
			
			/// REMAPPED JARS ///
			liason.remaps(remhelper -> {
				project.getLogger().lifecycle("preparing remapped jars");
				
				for(LoaderMod mod : mods) {
					//stuff like modXxxxxImplementation
					remhelper.createIncomingRemapConfigurations(mod.set);
					
					//put the regular jar task in devlibs
					mod.depJar.configure(remhelper::producesUnobfuscatedResults);
				}
				
				remhelper.remapMods();
			});
			
			/// REFMAPS ///
			liason.refmaps(refhelper -> {
				project.getLogger().lifecycle("preparing refmaps");
				File scratchDir = project.getLayout().getBuildDirectory().dir("mixin2").get().getAsFile();
				scratchDir.mkdirs();
				
				//the annotation processor used to generate refmaps
				Configuration mixinAp = configurations.maybeCreate("mixinAp");
				refhelper.refmapAddMixinAp(mixinAp);
				
				for(LoaderMod mod : mods) {
					File scratch2 = new File(scratchDir, "scratch-" + mod.modid);
					
					//all sources which touch a specific version of minecraft, and might contain
					//mixins which need refmaps
					FileCollection sources = mod.set.getAllSource()
						.plus(mod.perVersionSourceSets.get(ver).getAllSource());
					
					FileCollection classpath = mod.set.getCompileClasspath()
						.plus(mod.perVersionSourceSets.get(ver).getCompileClasspath())
						.plus(mod.versionAgnosticSourceSet.getCompileClasspath());
					
					//no need to make a classpath for the mixin AP itself;
					//it shadows all its dependencies(?)
					
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
					
					//fabric needs refmaps aregs to be added later, since loom.getMappingsFile can
					//only be called after loom evaluation
					refhelper.refmapAddArgsNowOrLater(() -> {
						File mappingsIn = refhelper.refmapGetMappingsIn();
						File mappingsOut = new File(scratchDir, mod.modid + ".out.txt");
						File refmapOut = new File(scratchDir, mod.modid + ".refmap.json");
						//TODO: what actually ends up in mappingsOut?
						// MDG feeds its contents back into ObfuscationExtension, but so far i've only seen empty files
						
						//configure mixin AP args
						List<String> mixinArgs = new ArrayList<>(refhelper.refmapArgs(mappingsIn, mappingsOut, refmapOut));
						mixinArgs.add("-proc:only"); //don't do any compilation, just do APs
						
						BoringCommandLineArgumentProvider clap = project.getObjects().newInstance(BoringCommandLineArgumentProvider.class);
						clap.args.set(mixinArgs);
						
						generateRefmaps.configure(it -> {
							//tell gradle this task creates these files
							it.getOutputs().file(refmapOut);
							it.getOutputs().file(mappingsOut);
							
							//add mixin args
							it.getOptions().getCompilerArgumentProviders().add(clap);
						});
						
						//include refmap in jar
						mod.depJar.configure(it -> {
							it.dependsOn(generateRefmaps);
							it.from(refmapOut);
						});
					});
				}
			});
			
			/// JIJ ///
			liason.jijQuatlib();
			
			/// RUN CONFIGS ///
			project.getLogger().lifecycle("setting up run configs");
			liason.setupRuns();
		}
	}
}

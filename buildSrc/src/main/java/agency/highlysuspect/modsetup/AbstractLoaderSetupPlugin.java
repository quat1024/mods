package agency.highlysuspect.modsetup;

import agency.highlysuspect.modsetup.liason.FabricLiason;
import agency.highlysuspect.modsetup.liason.ForgeViaMdgLiason;
import agency.highlysuspect.modsetup.liason.Liason;
import agency.highlysuspect.modsetup.liason.NeoforgeLiason;
import com.google.gson.FormattingStyle;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.loom.LoomGradlePlugin;
import net.neoforged.moddevgradle.boot.LegacyForgeModDevPlugin;
import net.neoforged.moddevgradle.boot.ModDevPlugin;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.logging.Logger;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.jvm.tasks.Jar;
import org.gradle.language.jvm.tasks.ProcessResources;

import java.io.*;
import java.nio.charset.StandardCharsets;
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
			this.loader = liason.loader;
			Liason.RefmapLiason refmapHelper = liason.getRefmapLiason();
			Liason.RemapLiason remapHelper = liason.getRemapLiason();
			
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
				
				//TODO leaky abstraction
				if(mod == quatlib && liason instanceof FabricLiason) withDeps(mod.splat, floaderOnlyDep());
				
				withImplementation(mod.set, mod.splat);
				
				//Intellij is dumdum and can't resolve intra-IDEA module dependencies when they're behind a configuration.
				//Ideally I would only tell IDEA about this dependency edge, Gradle works fine without it, but idk how to do that
				withCompileOnly(mod.set, mod.getSplattedDep(project, ver));
				if(mod == quatlib && liason instanceof FabricLiason) withCompileOnly(mod.set, floaderOnlyDep());
			}
			
			//"modXxxxxxImplementation"-style configurations, for depending on mapped artifacts
			if(remapHelper != null) for(LoaderMod mod : mods) remapHelper.createIncomingRemapConfigurations(mod);
			
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
			}
			
			///legacy forge mixin configs go in META-INF with this key. From neoforge discord:
			//[5:13 PM] IMB11: if i have multiple mixin files, is the MixinConfigs attribute comma separated? (legacy moddevgradle)
			//[5:46 PM] Bawnorton: yes
			for(LoaderMod mod : mods) {
				if(mod.legacyForgeMixinConfigs.isEmpty()) continue;
				mod.depJar.configure(jar -> {
					jar.manifest(man -> {
						man.attributes(Map.of("MixinConfigs", String.join(",", mod.legacyForgeMixinConfigs)));
					});
				});
			}
			
			/// REFMAPS ///
			if(refmapHelper != null) {
				project.getLogger().lifecycle("preparing refmaps");
				
				Provider<Directory> scratchDir2 = project.getLayout().getBuildDirectory().dir("mixin2");
				scratchDir2.get().getAsFile().mkdirs();
				
				//the annotation processor used to generate refmaps
				Configuration mixinAp = configurations.maybeCreate("mixinAp");
				refmapHelper.refmapAddMixinAp(mixinAp);
				
				for(LoaderMod mod : mods) {
					//n.b. this only produces refmaps with the correct name because of the naming convention.
					//it would be nice to iterate through, look for mixin jsons, and write refmaps with the same name
					//with "mixins.json" replaced with "refmap.json"
					
					List<RefmapJob> refmapJobs = new ArrayList<>();
					
					String loaderMixinsJsonName = mod.modid + "." + liason.loader + ".mixins.json";
					String loaderRefmapJsonName = mod.modid + "." + liason.loader + ".refmap.json";
					
					RefmapJob loaderRefmap = null;
					if(Util.hasResource(mod.set, loaderMixinsJsonName)) {
						project.getLogger().lifecycle("Found mixin json {} in {}", loaderMixinsJsonName, mod.modid);
						
						loaderRefmap = project.getObjects().newInstance(RefmapJob.class, mod.modid + "." + liason.loader);
						loaderRefmap.addSources(mod.set.getAllSource());
						loaderRefmap.addClasspath(mod.set.getCompileClasspath()
							.plus(mod.perVersionSourceSets.get(ver).getCompileClasspath())
							.plus(mod.versionAgnosticSourceSet.getCompileClasspath()));
						
						loaderRefmap.inputMixinJsons.add(loaderMixinsJsonName);
						//it's slapped in the output jar with just "jar.from" stuff, so it keeps the filename
						loaderRefmap.refmapOut.set(scratchDir2.map(d -> d.file(loaderRefmapJsonName)));
						
						refmapJobs.add(loaderRefmap);
					}
					
					String vanillaMixinsJsonName = mod.modid + ".mixins.json";
					String vanillaRefmapJsonName = mod.modid + ".refmap.json";
					
					RefmapJob vanillaRefmap = null;
					if(Util.hasResource(mod.perVersionSourceSets.get(ver), vanillaMixinsJsonName)) {
						project.getLogger().lifecycle("Found mixin json {} in {} (vanilla project)", vanillaMixinsJsonName, mod.modid);
						
						vanillaRefmap = project.getObjects().newInstance(RefmapJob.class, mod.modid);
						vanillaRefmap.addSources(mod.perVersionSourceSets.get(ver).getAllSource());
						vanillaRefmap.addClasspath(mod.perVersionSourceSets.get(ver).getCompileClasspath()
							.plus(mod.versionAgnosticSourceSet.getCompileClasspath()));
						
						vanillaRefmap.inputMixinJsons.add(vanillaMixinsJsonName);
						vanillaRefmap.refmapOut.set(scratchDir2.map(d -> d.file(vanillaRefmapJsonName)));
						
						refmapJobs.add(vanillaRefmap);
					}
					
					//they are otherwise configured the same way
					for(RefmapJob rj : refmapJobs) {
						rj.addAp(mixinAp);
						
						//must be lazy, this can't be read until loom's afterEvaluate block
						//loom initalizes "loom.getMappingsFile()" in afterEvaluate
						rj.mappingsIn.set(() -> {
							File mappingsIn = refmapHelper.refmapGetMappingsIn();
							project.getLogger().lifecycle("just called getMappingsIn and got {}", mappingsIn);
							return mappingsIn;
						});
						rj.mappingsOut.set(scratchDir2.map(d -> d.file(rj.name + "_mappings-out.txt")));
						rj.refmapOut.set(scratchDir2.map(d -> d.file(rj.name + ".refmap.json")));
						//same laziness concerns here
						rj.args.addAll(() -> {
							List<String> args = refmapHelper.refmapArgs(rj.mappingsIn, rj.mappingsOut, rj.refmapOut);
							project.getLogger().lifecycle("GOT ARGS: {}", args);
							return args.iterator();
						});
						
						TaskProvider<JavaCompile> refmapGenTask = rj.makeTask(project);
						refmapHelper.configureRefmapTask(refmapGenTask);
						
						tasks.named(mod.set.getProcessResourcesTaskName(), ProcessResources.class, it -> {
							it.dependsOn(refmapGenTask);
							
							//include refmap in the jar's resources
							it.from(rj.refmapOut);
							
							//amend the mixin json with a "refmap" field. just overwrite the mixin json from doLast.
							//n.b. there's no path-math, just assumes refmap is in the root of the jar.
							List<String> inputMixinJsons = rj.inputMixinJsons.get();
							String refmapFilename = rj.refmapOut.getLocationOnly().get().getAsFile().getName();
							it.doLast(t -> {
								for(String mixinJson : inputMixinJsons)
									amendMixinJson(t.getLogger(), new File(it.getDestinationDir(), mixinJson), refmapFilename);
							});
						});
						
						mod.refmapJobs.add(rj);
					}
				}
			};
			
			/// REMAPPED JARS ///
			if(remapHelper != null) {
				project.getLogger().lifecycle("preparing remapped jars");
				
				for(LoaderMod mod : mods) {
					//reclassify the original build output as a dev jar
					Provider<Directory> devlibs = project.getLayout().getBuildDirectory().dir("devlibs");
					mod.depJar.configure(it -> {
						it.getArchiveClassifier().set("dev");
						it.getDestinationDirectory().set(devlibs);
					});
				}
				
				remapHelper.remapMods();
			} else {
				//remapHelper == null -> no remapping is needed
				for(LoaderMod mod : mods) {
					mod.depJarNamed = mod.depJar; //the mod is already named correctly
				}
			}
			
			//hang these tasks off a default gradle task so it's easy to build every mod
			TaskProvider<?> jar = tasks.named("jar");
			for(LoaderMod mod : mods) jar.configure(it -> it.dependsOn(mod.depJarNamed));
			
			/// JIJ ///
			liason.jijQuatlib();
			
			/// RUN CONFIGS ///
			project.getLogger().lifecycle("setting up run configs");
			liason.setupRuns();
		}
	}
	
	private static final Gson GSON = new GsonBuilder()
		.disableHtmlEscaping()
		.setFormattingStyle(FormattingStyle.PRETTY.withIndent("\t"))
		.create();
	
	private static void amendMixinJson(Logger log, File mixinJsonFile, String refmapName) {
		try {
			if(mixinJsonFile.exists()) {
				log.lifecycle("Amending mixin json at {} to contain refmap {}", mixinJsonFile, refmapName);
				
				JsonObject mixinJson;
				try(InputStreamReader in = new InputStreamReader(new FileInputStream(mixinJsonFile))) {
					mixinJson = GSON.fromJson(in, JsonObject.class);
				}
				
				mixinJson.addProperty("refmap", refmapName);
				
				try(OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(mixinJsonFile), StandardCharsets.UTF_8)) {
					out.append(GSON.toJson(mixinJson));
					out.flush();
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to amend mixin json at " + mixinJsonFile, e);
		}
	}
}

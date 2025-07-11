package agency.highlysuspect.modsetup;

import agency.highlysuspect.minivan.MinivanExt;
import agency.highlysuspect.minivan.MinivanPlugin;
import agency.highlysuspect.minivan.prov.MinecraftProvider;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.ConsumableConfiguration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.ArtifactHandler;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.internal.Pair;
import org.gradle.jvm.tasks.Jar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class VanillaSetupPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getLogger().lifecycle("HELLO FROM VanillaSetupPlugin");
		
		//apply java while we're here
		project.getPlugins().apply("java");
		
		//apply minivan
		project.getPlugins().apply(MinivanPlugin.class);
		
		//ext
		project.getExtensions().create("vanillaSetup", Ext.class, project);
	}
	
	public static class Ext extends AbstractSetupExtension {
		public Ext(Project project) {
			super(project);
			this.mods = project.getObjects().domainObjectContainer(VanillaMod.class);
		}
		
		public final NamedDomainObjectContainer<VanillaMod> mods;
		
		//set in afterEvaluate
		public SourceSet modAnyVersionAny;
		public Map<String, SourceSet> modAgnosticSourceSets = new HashMap<>();
		
		public NamedDomainObjectProvider<ConsumableConfiguration> modAnyVersionAnyElements;
		public Map<String, NamedDomainObjectProvider<ConsumableConfiguration>> modAgnosticElements = new HashMap<>();
		
		public TaskProvider<Jar> modAnyVersionAnyThinJar;
		public Map<String, TaskProvider<Jar>> modAgnosticJars = new HashMap<>();
		
		protected TaskProvider<Jar> jarTask(@Nullable String mod, @Nullable String version, SourceSet set) {
			TaskContainer tasks = project.getTasks();
			
			//make a thinjar task
			TaskProvider<Jar> thinJar = tasks.register(set.getName() + "ThinJar", Jar.class, it -> {
				it.setGroup("build");
				it.from(set.getOutput());
				it.getArchiveClassifier().set(Util.modVersion(mod, version) + "-thin");
			});
			
			//hang this task off the regular jar task (for ease of execution)
			tasks.named("jar", it -> it.dependsOn(thinJar));
			
			return thinJar;
		}
		
		public void go(Action<? super Ext> act) {
			act.execute(this);
			doIt();
		}
		
		public void doIt() {
			TaskContainer tasks = project.getTasks();
			DependencyHandler dependencies = project.getDependencies();
			RepositoryHandler repositories = project.getRepositories();
			ConfigurationContainer configurations = project.getConfigurations();
			ArtifactHandler artifacts = project.getArtifacts();
			
			MinivanExt minivanExt = project.getExtensions().findByType(MinivanExt.class);
			if(minivanExt == null) throw new IllegalStateException("minivanext is null");
			
			//add sponge (because i dep on mixin)
			repositories.maven(mvn -> {
				mvn.setUrl("https://repo.spongepowered.org/repository/maven-public/");
				mvn.content(it ->
					it.includeGroup("org.spongepowered")
				);
			});
			
			//we're not using the 'main' source set at all
			tasks.named("jar", Jar.class, it -> it.setEnabled(false));
			
			/// MINECRAFT ///
			project.getLogger().lifecycle("setting up minecraft");
			
			//what minecraft versions are needed?
			Set<String> versionsToMake = new TreeSet<>();
			for(VanillaMod mod : mods) versionsToMake.addAll(mod.versions);
			
			//build all the minecrafts (in parallel, why not)
			Map<String, MinecraftProvider.Result> minecrafts = versionsToMake.parallelStream().map(minecraftVersion -> {
				//TODO: fabric access wideners can go here...
				MinecraftProvider.Result result = minivanExt.minecraftBuilder()
					.version(minecraftVersion)
					.build()
					.tryGetMinecraft();
				return Pair.of(minecraftVersion, result);
			}).collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
			
			/// SOURCE SET SCAFFOLDING ///
			project.getLogger().lifecycle("making source sets");
			
			//modAnyVersionAny contains code shared across everything in the ecosystem
			//it should be compiled against the minimum java version used across all mods with quatlib enabled
			modAnyVersionAny = makeSourceSetWithCommonDeps(Util.modVersion(null, null));
			setCompatLevel(modAnyVersionAny, mods.stream()
				.filter(mod -> mod.quatlib)
				.flatMap(mod -> mod.versions.stream())
				.mapToInt(Util::compatLevelForMinecraft)
				.min().orElse(8)
			);
			
			for(String minecraftVersion : versionsToMake) {
				project.getLogger().lifecycle("minecraft {}", minecraftVersion);
				//modAnyVersion1_21_1 and such
				SourceSet modAgnosticSourceSet = makeSourceSetWithCommonDeps(Util.modVersion(null, minecraftVersion));
				modAgnosticSourceSets.put(minecraftVersion, modAgnosticSourceSet);
				//it can see modAnyVersionAny
				extendSourceSet2(modAgnosticSourceSet, modAnyVersionAny);
				//it should be compiled against the java version used for this version
				setCompatLevel(modAgnosticSourceSet, Util.compatLevelForMinecraft(minecraftVersion));
			}
			
			for(VanillaMod mod : mods) {
				project.getLogger().lifecycle("mod {}", mod.modid);
				//modSomethingVersionAny and such
				mod.versionAgnosticSourceSet = makeSourceSetWithCommonDeps(Util.modVersion(mod.modid, null));
				//it can see modAnyVersionAny if quatlib is enabled
				if(mod.quatlib)
					extendSourceSet2(mod.versionAgnosticSourceSet, modAnyVersionAny);
				//it should be compiled against the lowest java version used in supported mods
				setCompatLevel(mod.versionAgnosticSourceSet, mod.versions.stream().mapToInt(Util::compatLevelForMinecraft).min().orElse(8));
				
				for(String minecraftVersion : mod.versions) {
					project.getLogger().lifecycle("...for {}", minecraftVersion);
					//modSomethingVersion1_21_1 and such
					SourceSet modAndVersionSpecificSourceSet = makeSourceSetWithCommonDeps(Util.modVersion(mod.modid, minecraftVersion));
					mod.perVersionSourceSets.put(minecraftVersion, modAndVersionSpecificSourceSet);
					//it can see the version-agnostic set always, and mod-agnostic sets if quatlib is enabled
					extendSourceSet2(modAndVersionSpecificSourceSet, mod.versionAgnosticSourceSet);
					if(mod.quatlib)
						extendSourceSet2(modAndVersionSpecificSourceSet, modAnyVersionAny, modAgnosticSourceSets.get(minecraftVersion));
					//it should be compiled against the java version used for this version
					setCompatLevel(modAndVersionSpecificSourceSet, Util.compatLevelForMinecraft(minecraftVersion));
				}
			}
			
			/// DEPENDENCIES ///
			project.getLogger().lifecycle("preparing deps");
			
			//put minecraft in all the mod-agnostic but version-specific sets
			modAgnosticSourceSets.forEach((minecraftVersion, modAgnosticSourceSet) -> {
				MinecraftProvider.Result mc = minecrafts.get(minecraftVersion);
				withImplementation(modAgnosticSourceSet,
					project.files(mc.minecraft), //minecraft itself
					mc.dependencies.stream().map(dependencies::create).toList(), //all the mc deps
					"com.mojang:logging:1.1.1", //TODO, why isn't this one showing up? minivan bug?
					"org.spongepowered:mixin:0.8.5"
				);
			});
			
			/// PROCESS RESOURCES ///
			project.getLogger().lifecycle("configuring processResources");
			
			Map<String, Object> totallyCommonProps = Util.broadlyApplicableProps(project);
			Map<String, Map<String, Object>> perVersionProps = new HashMap<>();
			for(String mcVersion : versionsToMake)
				perVersionProps.put(mcVersion, Map.of("minecraft_version", mcVersion));
			
			configureProcessResources(modAnyVersionAny, totallyCommonProps);
			modAgnosticSourceSets.forEach((minecraftVersion, modAgnosticSourceSet) ->
				configureProcessResources(modAgnosticSourceSet, totallyCommonProps, perVersionProps.get(minecraftVersion)));
			for(VanillaMod mod : mods) {
				configureProcessResources(mod.versionAgnosticSourceSet, totallyCommonProps, mod.vars);
				mod.perVersionSourceSets.forEach((minecraftVersion, perVersionSourceSet) ->
					configureProcessResources(perVersionSourceSet, totallyCommonProps, perVersionProps.get(minecraftVersion), mod.vars));
			}
			
			/// JARS AND CONSUMABLE CONFIGURATIONS ///
			project.getLogger().lifecycle("preparing jars and consumable configurations");
			
			modAnyVersionAnyThinJar = jarTask(null, null, modAnyVersionAny);
			modAnyVersionAnyElements = configurations.consumable("modAnyVersionAnyElements");
			artifacts.add(modAnyVersionAnyElements.getName(), modAnyVersionAnyThinJar);
			
			modAgnosticSourceSets.forEach((minecraftVersion, modAgnosticSourceSet) -> {
				TaskProvider<Jar> modAgnosticThinJar = jarTask(null, minecraftVersion, modAgnosticSourceSet);
				modAgnosticJars.put(minecraftVersion, modAgnosticThinJar);
				
				//fill out the mod-agnostic configuration
				NamedDomainObjectProvider<ConsumableConfiguration> elements = configurations.consumable("modAgnosticElements" + minecraftVersion);
				modAgnosticElements.put(minecraftVersion, elements);
				artifacts.add(elements.getName(), modAnyVersionAnyThinJar);
				artifacts.add(elements.getName(), modAgnosticThinJar);
			});
			
			for(VanillaMod mod : mods) {
				//thinjar
				mod.versionAgnosticJar = jarTask(mod.modid, null, mod.versionAgnosticSourceSet);
				
				mod.perVersionSourceSets.forEach((minecraftVersion, perVersionSourceSet) -> {
					TaskProvider<Jar> versionSpecificJar = jarTask(mod.modid, minecraftVersion, perVersionSourceSet);
					mod.perVersionJars.put(minecraftVersion, versionSpecificJar);
					
					//fill out its consumable configuration
					NamedDomainObjectProvider<ConsumableConfiguration> elements = configurations.consumable(Util.modVersion(mod.modid, minecraftVersion) + "Elements");
					mod.perVersionElements.put(minecraftVersion, elements);
					artifacts.add(elements.getName(), mod.versionAgnosticJar);
					artifacts.add(elements.getName(), versionSpecificJar);
				});
			}
			
			/// SIMPLE RUN ///
			project.getLogger().lifecycle("setting up simpleruns");
			
			for(VanillaMod mod : mods) {
				if(mod.simpleRunMainClass != null) {
					mod.perVersionSourceSets.forEach((version, sourceSet) -> {
						tasks.register(sourceSet.getTaskName("simpleRun", ""), JavaExec.class, it -> {
							it.setGroup("simpleRun");
							it.getMainClass().set(mod.simpleRunMainClass);
							it.setClasspath(sourceSet.getRuntimeClasspath());
							
							Path workingDir = project.getLayout().getProjectDirectory().getAsFile().toPath()
								.resolve("run").resolve(mod.modid).resolve(version);
							it.setWorkingDir(workingDir.toFile());
							it.doFirst(__ -> workingDir.toFile().mkdirs());
						});
					});
				}
			}
		}
		
		/// for calling from loader setup plugins ///
		
		//grab the list of vanillamods
		public NamedDomainObjectContainer<VanillaMod> getVanillaMods() {
			return mods;
		}
		
		//consumable configurations containing code for all mods
		public Dependency dependOnModAgnostic(@NotNull String minecraftVersion) {
			return depOnMyConfiguration(modAgnosticElements.get(minecraftVersion));
		}
		
		//consumable configurations containing code for this mod
		public Dependency dependOnVersionAndModSpecific(VanillaMod mod, String minecraftVersion) {
			return depOnMyConfiguration(mod.perVersionElements.get(minecraftVersion));
		}
	}
}

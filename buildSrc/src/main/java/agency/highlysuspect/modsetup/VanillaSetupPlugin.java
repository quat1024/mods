package agency.highlysuspect.modsetup;

import agency.highlysuspect.minivan.MinivanExt;
import agency.highlysuspect.minivan.MinivanPlugin;
import agency.highlysuspect.minivan.prov.MinecraftProvider;
import org.codehaus.groovy.runtime.StringGroovyMethods;
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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
	
	@SuppressWarnings("UnstableApiUsage")
	public static class Ext extends AbstractSetupExtension {
		public Ext(Project project) {
			super(project);
			this.mods = project.getObjects().domainObjectContainer(VanillaMod.class);
			
			//add quatlib! supported mc versions will be filled in later.
			quatlib = this.mods.create("modder_name_lib", quat -> {
				//obviously this breaks things, lol
				quat.dependOnQuatlib = false;
			});
		}
		
		public final NamedDomainObjectContainer<VanillaMod> mods;
		public final VanillaMod quatlib;
		
		protected String modVersion(@NotNull String mod, @Nullable String version) {
			return Util.snakeToCamel(mod) + (version == null ? "Any" : version.replace('.', '_'));
		}
		
		protected TaskProvider<Jar> jarTask(String mod, @Nullable String version, SourceSet set) {
			TaskContainer tasks = project.getTasks();
			
			//make a thinjar task
			TaskProvider<Jar> thinJar = tasks.register(set.getName() + "ThinJar", Jar.class, it -> {
				it.setGroup("build");
				it.from(set.getOutput());
				it.getArchiveClassifier().set(modVersion(mod, version) + "-thin");
			});
			
			//hang this task off the regular jar task (for ease of execution)
			tasks.named("jar", it -> it.dependsOn(thinJar));
			
			return thinJar;
		}
		
		@SuppressWarnings("unused")
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
			
			//dep on sponge (for mixin)
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
			
			/// QUATLIB ///
			quatlib.versions.addAll(versionsToMake);
			
			/// SOURCE SET SCAFFOLDING ///
			project.getLogger().lifecycle("making source sets");
			
			for(VanillaMod mod : mods) {
				project.getLogger().lifecycle("mod {}", mod.modid);
				//modSomethingVersionAny and such
				mod.versionAgnosticSourceSet = makeSourceSetWithCommonDeps(modVersion(mod.modid, null));
				//it should be compiled against the lowest java version used
				setCompatLevel(mod.versionAgnosticSourceSet, mod.versions.stream().mapToInt(Util::compatLevelForMinecraft).min().orElse(8));
				
				for(String minecraftVersion : mod.versions) {
					project.getLogger().lifecycle("...for {}", minecraftVersion);
					//modSomethingVersion1_21_1 and such
					SourceSet modAndVersionSpecificSourceSet = makeSourceSetWithCommonDeps(modVersion(mod.modid, minecraftVersion));
					mod.perVersionSourceSets.put(minecraftVersion, modAndVersionSpecificSourceSet);
					//it can see the version-agnostic set
					extendSourceSet2(modAndVersionSpecificSourceSet, mod.versionAgnosticSourceSet);
					//it should be compiled against the java version used for this version
					setCompatLevel(modAndVersionSpecificSourceSet, Util.compatLevelForMinecraft(minecraftVersion));
				}
			}
			//connect up quatlib
			for(VanillaMod mod : mods) {
				if(mod.dependOnQuatlib) {
					extendSourceSet2(mod.versionAgnosticSourceSet, quatlib.versionAgnosticSourceSet);
					mod.perVersionSourceSets.forEach((mcVer, perVersionSrc) ->
						extendSourceSet2(perVersionSrc, quatlib.versionAgnosticSourceSet, quatlib.getPerVersionSourceSet(mcVer)));
				}
			}
			
			/// DEPENDENCIES ///
			project.getLogger().lifecycle("preparing deps");
			
			//put minecraft in all the mod-agnostic but version-specific sets
			minecrafts.forEach((minecraftVersion, mc) -> {
				withImplementation(quatlib.getPerVersionSourceSet(minecraftVersion),
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
			
			for(VanillaMod mod : mods) {
				configureProcessResources(mod.versionAgnosticSourceSet, Util.plus(totallyCommonProps, mod.vars));
				mod.perVersionSourceSets.forEach((minecraftVersion, perVersionSourceSet) ->
					configureProcessResources(perVersionSourceSet, Util.plus(totallyCommonProps, perVersionProps.get(minecraftVersion), mod.vars)));
			}
			
			/// JARS AND CONSUMABLE CONFIGURATIONS ///
			project.getLogger().lifecycle("preparing jars and consumable configurations");
			
			for(VanillaMod mod : mods) {
				//thinjar
				mod.versionAgnosticJar = jarTask(mod.modid, null, mod.versionAgnosticSourceSet);
				
				mod.perVersionSourceSets.forEach((minecraftVersion, perVersionSourceSet) -> {
					TaskProvider<Jar> versionSpecificJar = jarTask(mod.modid, minecraftVersion, perVersionSourceSet);
					mod.perVersionJars.put(minecraftVersion, versionSpecificJar);
					
					//fill out its consumable configuration
					NamedDomainObjectProvider<ConsumableConfiguration> elements = configurations.consumable(modVersion(mod.modid, minecraftVersion) + "Elements");
					mod.perVersionElements.put(minecraftVersion, elements);
					artifacts.add(elements.getName(), mod.versionAgnosticJar);
					artifacts.add(elements.getName(), versionSpecificJar);
				});
			}
			//quatlib additionally publishes a version-agnostic-only configuration
			NamedDomainObjectProvider<ConsumableConfiguration> quatlibAnyElements = configurations.consumable("quatlibAnyElements");
			artifacts.add(quatlibAnyElements.getName(), quatlib.versionAgnosticJar);
			
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
		@Deprecated
		public Dependency dependOnModAgnostic(@NotNull String minecraftVersion) {
			return dependOnVersionAndModSpecific(quatlib, minecraftVersion);
		}
		
		//consumable configurations containing code for this mod
		public Dependency dependOnVersionAndModSpecific(VanillaMod mod, @NotNull String minecraftVersion) {
			return depOnMyConfiguration(mod.getPerVersionElement(minecraftVersion));
		}
	}
}

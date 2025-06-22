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
import org.gradle.api.artifacts.dsl.ArtifactHandler;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.jvm.tasks.Jar;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;

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
	
	public static class Ext {
		public Ext(Project project) {
			this.project = project;
			this.mods = project.getObjects().domainObjectContainer(VanillaMod.class);
		}
		
		public final Project project;
		public final NamedDomainObjectContainer<VanillaMod> mods;
		
		@SuppressWarnings("UnstableApiUsage") //the new consumable configuration dsl
		protected TaskProvider<Jar> jarMeUpBoys(@Nullable String mod, @Nullable String version, SourceSet set) {
			TaskContainer tasks = project.getTasks();
			ConfigurationContainer configurations = project.getConfigurations();
			ArtifactHandler artifacts = project.getArtifacts();
			
			//make a thinjar task
			TaskProvider<Jar> thinJar = tasks.register(set.getName() + "ThinJar", Jar.class, it -> {
				it.setGroup("build");
				it.from(set.getOutput());
				it.getArchiveClassifier().set(Util.modVersion(mod, version) + "-thin");
			});
			
			//make a modXxxVersionYyyElements consumable configuration
			NamedDomainObjectProvider<ConsumableConfiguration> elements =
				configurations.consumable(Util.modVersionConsumableConfigurationName(mod, version));
			
			//publish this jar under that configuration
			artifacts.add(elements.getName(), thinJar);
			
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
			SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
			RepositoryHandler repositories = project.getRepositories();
			
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
			
			//the modAnyVersionAny source-set contains code shared across literally
			//every project in the ecosystem
			SourceSet modAnyVersionAny = sourceSets.create("modAnyVersionAny");
			
			//the modAnyVersionAny thinjar
			TaskProvider<Jar> modAnyVersionAnyThinJar = jarMeUpBoys(null, null, modAnyVersionAny);
			
			//collect all versions requested by the mods...
			Set<String> versionsToMake = new TreeSet<>();
			for(VanillaMod mod : mods) versionsToMake.addAll(mod.versions);
			
			//for each minecraft version
			for(String version : versionsToMake) {
				project.getLogger().lifecycle("setting up minecraft {}", version);
				
				//make a source-set for this version of minecraft
				SourceSet modAnyVersionThis = sourceSets.create(Util.modVersion(null, version));
				
				//things which should go in this source-set: the modAnyVersionAny source set
				Util.extendSourceSetFrom(modAnyVersionThis, modAnyVersionAny);
				
				//and minecraft. we will create minecraft using minivan
				//TODO: fabric access wideners can go here...
				MinecraftProvider.Result mc = minivanExt.minecraftBuilder()
					.version(version)
					.build()
					.tryGetMinecraft();
				
				Util.withImplementation(project, modAnyVersionThis,
					//minecraft and its libraries
					project.files(mc.minecraft),
					mc.dependencies.stream().map(dependencies::create).toList(),
					//shared code
					modAnyVersionAny.getOutput(),
					//TODO, why isn't this one showing up? minivan bug?
					"com.mojang:logging:1.1.1",
					//for fun
					"org.spongepowered:mixin:0.8.5",
					"org.jetbrains:annotations:16.0.2"
				);
				
				//thinjar
				TaskProvider<Jar> mcThinJar = jarMeUpBoys(null, version, modAnyVersionThis);
			}
			
			//for each mod...
			for(VanillaMod modSettings : mods) {
				String mod = modSettings.modid;
				project.getLogger().lifecycle("setting up mod {}", mod);
				
				//source set for all minecraft versions of this mod
				SourceSet modThisVersionAny = sourceSets.create(Util.modVersion(mod, null));
				
				//compile against modAnyVersionAny
				Util.extendSourceSetFrom(modThisVersionAny, modAnyVersionAny);
				Util.withImplementation(project, modThisVersionAny, modAnyVersionAny.getOutput());
				
				//thinjar
				TaskProvider<Jar> modThisVersionAnyThinJar = jarMeUpBoys(mod, null, modThisVersionAny);
				
				//for each minecraft version that this mod uses
				for(String version : modSettings.versions) {
					project.getLogger().lifecycle("...for {}", version);
					//source set for *this* minecraft version of *this* mod
					SourceSet modThisVersionThis = sourceSets.create(Util.modVersion(mod, version));
					
					//compile against modAnyVersionAny, modThisVersionAny, and modAnyVersionThis
					SourceSet modAnyVersionThis = sourceSets.getByName(Util.modVersion(null, version)); //was just created above
					Util.extendSourceSetFrom(modThisVersionThis,
						modAnyVersionAny, modThisVersionAny, modAnyVersionThis);
					Util.withImplementation(project, modThisVersionThis,
						modAnyVersionAny.getOutput(), modThisVersionAny.getOutput(), modAnyVersionThis.getOutput());
					
					//thinjar
					TaskProvider<Jar> modThisVersionThisThinJar = jarMeUpBoys(mod, version, modThisVersionThis);
					
					//a simple "run config", if one was requested
					if(modSettings.simpleRunMainClass != null) {
						tasks.register(modThisVersionThis.getTaskName("simpleRun", ""), JavaExec.class, it -> {
							it.setGroup("simpleRun");
							it.getMainClass().set(modSettings.simpleRunMainClass);
							it.setClasspath(modThisVersionThis.getRuntimeClasspath());
							
							Path workingDir = project.getLayout().getProjectDirectory().getAsFile().toPath()
								.resolve("run").resolve(mod).resolve(version);
							it.setWorkingDir(workingDir.toFile());
							it.doFirst(__ -> workingDir.toFile().mkdirs());
						});
					}
				}
			}
		}
	}
}

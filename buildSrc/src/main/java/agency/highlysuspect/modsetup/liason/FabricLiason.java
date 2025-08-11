package agency.highlysuspect.modsetup.liason;

import agency.highlysuspect.modsetup.*;
import net.fabricmc.loom.api.LoomGradleExtensionAPI;
import net.fabricmc.loom.configuration.ide.RunConfigSettings;
import net.fabricmc.loom.extension.LoomGradleExtensionImpl;
import net.fabricmc.loom.extension.MixinExtension;
import net.fabricmc.loom.task.AbstractRunTask;
import net.fabricmc.loom.task.RemapJarTask;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.api.tasks.compile.JavaCompile;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

public class FabricLiason extends Liason implements Liason.RemapLiason, Liason.RefmapLiason, Liason.JarInJarLiason {
	public FabricLiason(Project project, String ver, NamedDomainObjectContainer<LoaderMod> mods) {
		super(project, ver, "fabric", mods);
		this.loom = project.getExtensions().getByType(LoomGradleExtensionAPI.class);
	}
	
	protected final LoomGradleExtensionAPI loom;
	
	@Override
	public void setupOfficialNames() {
		project.getDependencies().add("minecraft", "com.mojang:minecraft:" + ver);
		project.getDependencies().add("mappings", loom.officialMojangMappings());
	}
	
	@Override
	public void disableUnusedDefaultTasks() {
		//we don't use the "jar" task, so disable this task too
		project.getTasks().named("remapJar", it -> it.setEnabled(false));
		
		//make my own run configs
		project.getTasks().named("runClient").configure(it -> it.setEnabled(false));
		project.getTasks().named("runServer").configure(it -> it.setEnabled(false));
		
		//Orbital nuke Loom's builtin refmap handling since there's no way to turn it off
		try {
			Field mixinApExtensionField = LoomGradleExtensionImpl.class.getDeclaredField("mixinApExtension");
			MixinExtension fake = project.getObjects().newInstance(FakeLoomMixinExtension.class, project);
			NothingToSeeHere.theFinalModifierIsAMereSuggestion(loom, mixinApExtensionField, fake);
		} catch (Exception e) {
			throw new RuntimeException("Can't nuke Loom mixin", e);
		}
	}
	
	/// REFMAPS ///
	@Override
	public @Nullable RefmapLiason getRefmapLiason() {
		return this;
	}
	
	@Override
	public void refmapAddMixinAp(Configuration mixinAp) {
		//this AP has ObfuscationServiceFabric which supports the "named:intermediary" obfuscation type.
		//It's available on the Fabric maven which is already added to repositories() by Loom.
		withDeps(mixinAp, "net.fabricmc:fabric-mixin-compile-extensions:0.6.0");
	}
	
	@Override
	public File refmapGetMappingsIn() {
		return loom.getMappingsFile(); //NOTE: Kabooms if this is called before Loom afterEvaluate
	}
	
	@Override
	public List<String> refmapArgs(RegularFileProperty mappingsIn, RegularFileProperty mappingsOut, RegularFileProperty refmapOut) {
		return List.of(
			"-AinMapFileNamedIntermediary=" + mappingsIn.get().getAsFile().getAbsolutePath(),
			"-AoutMapFileNamedIntermediary=" + mappingsOut.get().getAsFile().getAbsolutePath(),
			"-AoutRefMapFile=" + refmapOut.get().getAsFile().getAbsolutePath(),
			"-AdefaultObfuscationEnv=named:intermediary"
		);
	}
	
	@Override
	public void configureRefmapTask(TaskProvider<JavaCompile> task) {
		//nothing else to do
	}
	
	/// REMAPS ///
	@Override
	public @Nullable RemapLiason getRemapLiason() {
		return this;
	}
	
	@Override
	public void createIncomingRemapConfigurations(LoaderMod mod) {
		loom.createRemapConfigurations(mod.set);
	}
	
	@Override
	public void remapMods() {
		//make all the remap tasks
		for(LoaderMod mod : mods) {
			mod.depJarNamed = project.getTasks().register(mod.depJar.getName() + "Named", RemapJarTaskWithExtraMixinMappings.class, it -> {
				it.dependsOn(mod.depJar);
				it.getArchiveBaseName().set(mod.modid + "-" + ver + "-" + loader);
				it.getInputFile().set(mod.depJar.flatMap(AbstractArchiveTask::getArchiveFile));
				
				for(RefmapJob job : mod.refmapJobs) {
					it.dependsOn(job.task);
					it.getMixinApExtraMappings_QUAT().from(job.mappingsOut);
				}
				
				if(mod.dependOnQuatlib) {
					LoaderMod quatlib = mods.getByName("modder_name_lib");
					it.dependsOn(quatlib.depJar);
					//put quatlib devjar on the remap classpath so tiny-remapper can see into it
					it.getClasspath().from(quatlib.depJar.get().getArchiveFile());
				}
			});
		}
	}
	
	@Override
	public @Nullable JarInJarLiason getJarInJarLiason() {
		return this;
	}
	
	@Override
	public void setupJarInJars() {
		LoaderMod quatlib = mods.getByName("modder_name_lib");
		
		for(LoaderMod mod : mods) {
			if(mod != quatlib && mod.dependOnQuatlib && mod.depJarNamed != null) {
				mod.depJarNamed.configure(it -> {
					//for some reason you can JiJ stuff from RemapJarTask?
					//not sure what that has to do with remapping but ok :thumbs_up: sure
					it.dependsOn(quatlib.depJarNamed);
					((RemapJarTask) it).getNestedJars().from(((RemapJarTask) quatlib.depJarNamed.get()).getArchiveFile());
					((RemapJarTask) it).getAddNestedDependencies().set(true);
				});
			}
			
			mod.depJarNamedJarjarred = mod.depJarNamed; //accomplished in the same task
		}
	}
	
	@Override
	public void setupRuns() {
		loom.getRuns().clear();
		
		RunConfigSettings client = loom.getRuns().maybeCreate("client-" + ver.replace('.', '-') + "-" + loader);
		client.client(); //client client
		client.setIdeConfigGenerated(false); //Doesn't work anyway since i have to amend the classpath
		
		//put all mods on the runtime classpath
		project.getTasks().withType(AbstractRunTask.class).configureEach(it -> {
			for(LoaderMod mod : mods) {
				//PLEASE WORK
				//it.classpath(mod.set.getOutput());
				//it.classpath(mod.getPerVersionSourceSet(ver).getOutput());
				//it.classpath(mod.versionAgnosticSourceSet.getOutput());
				//(ommitted 50 other things i tried)
				//OK WHATEVER RESOURCES DONT WANNA LOAD just load the fuckin Jar then, sure whatever.
				it.classpath(mod.depJar.flatMap(AbstractArchiveTask::getArchiveFile));
			}
			
			//Not sure how to get rid of the default runClient/runServer tasks so i'll at least leave them in the default folder
			if(it.getName().contains("-")) {
				it.setGroup("runs");
			}
			
//			it.doFirst(__ -> {
//				System.out.println("pppppppppppppppppppppppp");
//				System.out.println(it.getClasspath().getFiles());
//			});
		});
	}
}

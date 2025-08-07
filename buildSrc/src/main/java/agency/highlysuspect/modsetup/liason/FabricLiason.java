package agency.highlysuspect.modsetup.liason;

import agency.highlysuspect.modsetup.*;
import net.fabricmc.loom.api.LoomGradleExtensionAPI;
import net.fabricmc.loom.configuration.ide.RunConfigSettings;
import net.fabricmc.loom.extension.LoomGradleExtensionImpl;
import net.fabricmc.loom.extension.MixinExtension;
import net.fabricmc.loom.task.AbstractRunTask;
import net.fabricmc.loom.task.RemapJarTask;
import net.fabricmc.loom.task.service.MappingsService;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.api.tasks.compile.JavaCompile;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Consumer;

public class FabricLiason extends Liason implements Liason.RemapLiason, Liason.RefmapLiason {
	public FabricLiason(Project project, String ver, NamedDomainObjectContainer<LoaderMod> mods) {
		super(project, ver, mods);
		this.loom = extensions.getByType(LoomGradleExtensionAPI.class);
	}
	
	protected final LoomGradleExtensionAPI loom;
	
	@Override
	public String getLoaderIdentifier() {
		return "fabric";
	}
	
	@Override
	public void setupOfficialNames() {
		dependencies.add("minecraft", "com.mojang:minecraft:" + ver);
		dependencies.add("mappings", loom.officialMojangMappings());
	}
	
	@Override
	public void disableUnusedDefaultTasks() {
		//we don't use the "jar" task, so disable this task too
		tasks.named("remapJar", it -> it.setEnabled(false));
		
		//Orbital nuke Loom's builtin refmap handling since there's no way to turn it off
		try {
			Field mixinApExtensionField = LoomGradleExtensionImpl.class.getDeclaredField("mixinApExtension");
			MixinExtension fake = project.getObjects().newInstance(FakeLoomMixinExtension.class, project);
			NothingToSeeHere.stomp(loom, mixinApExtensionField, fake);
		} catch (Exception e) {
			throw new RuntimeException("Can't nuke Loom mixin", e);
		}
	}
	
	@Override
	public void addFloaderOnlyDep(SourceSet quatlib) {
		withImplementation(quatlib, floaderOnlyDep());
	}
	
	/// remaps ///
	@Override
	public void remaps(Consumer<RemapLiason> remaps) {
		remaps.accept(this);
	}
	
	@Override
	public void createIncomingRemapConfigurations(LoaderMod mod) {
		loom.createRemapConfigurations(mod.set);
	}
	
	@Override
	public void remapMods() {
		//make all the remap tasks
		for(LoaderMod mod : mods) {
			mod.depJarNamed = tasks.register(mod.depJar.getName() + "Named", RemapJarTaskWithExtraMixinMappings.class, it -> {
				it.dependsOn(mod.depJar);
				it.getArchiveBaseName().set(mod.modid + "-" + ver + "-" + loader);
				it.getInputFile().set(mod.depJar.flatMap(AbstractArchiveTask::getArchiveFile));
				
				for(RefmapJob job : mod.refmapJobs) {
					it.dependsOn(job.task);
					it.getMixinApExtraMappings_QUAT().from(job.mappingsOut);
				}
			});
			tasks.named("jar", it -> it.dependsOn(mod.depJarNamed));
		}
		
		LoaderMod quatlib = mods.getByName("modder_name_lib");
		
		for(LoaderMod mod : mods) {
			if(mod.dependOnQuatlib) {
				mod.depJarNamed.configure(it -> {
					it.dependsOn(quatlib.depJar);
					//put quatlib devjar on the remap classpath so tiny-remapper can see into it
					((RemapJarTask) it).getClasspath().from(quatlib.depJar.get().getArchiveFile());
				});
			}
		}
	}
	
	@Override
	public void jijQuatlib() {
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
		}
	}
	
	@Override
	public void refmaps(Consumer<RefmapLiason> refmaps) {
		refmaps.accept(this);
	}
	
	@Override
	public void refmapAddMixinAp(Configuration mixinAp) {
		//this version adds ObfuscationServiceFabric which supports the "named:intermediary" obfuscation type
		//also it depends on its own copy of the sponge mixin ap, so that gets pulled in
		withDeps(mixinAp, "net.fabricmc:fabric-mixin-compile-extensions:0.6.0");
	}
	
	@Override
	public File refmapGetMappingsIn() {
		return loom.getMappingsFile(); //Kabooms if this is called before fabric-loom inits some stuff
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
	
	@Override
	public void setupRuns() {
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
}

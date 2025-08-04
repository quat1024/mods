package agency.highlysuspect.modsetup.liason;

import agency.highlysuspect.modsetup.LoaderMod;
import net.fabricmc.loom.api.LoomGradleExtensionAPI;
import net.fabricmc.loom.configuration.ide.RunConfigSettings;
import net.fabricmc.loom.task.AbstractRunTask;
import net.fabricmc.loom.task.RemapJarTask;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.jvm.tasks.Jar;

import java.io.File;
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
	public void createIncomingRemapConfigurations(SourceSet set) {
		loom.createRemapConfigurations(set);
	}
	
	@Override
	public void remapMods() {
		//make all the remap tasks
		for(LoaderMod mod : mods) {
			mod.depJarNamedLoom = tasks.register(mod.depJar.getName() + "Named", RemapJarTask.class, it -> {
				it.dependsOn(mod.depJar);
				it.getArchiveBaseName().set(mod.modid + "-" + ver + "-" + loader);
				it.getInputFile().set(mod.depJar.flatMap(AbstractArchiveTask::getArchiveFile));
			});
			tasks.named("jar", it -> it.dependsOn(mod.depJarNamedLoom));
		}
		
		LoaderMod quatlib = mods.getByName("modder_name_lib");
		
		for(LoaderMod mod : mods) {
			if(mod != quatlib && mod.dependOnQuatlib) {
				mod.depJarNamedLoom.configure(it -> {
					it.dependsOn(quatlib.depJar);
					//put quatlib devjar on the remap classpath so tiny-remapper can see into it
					//TODO: is this still needed?
					it.getClasspath().from(quatlib.depJar.get().getArchiveFile());
				});
			}
		}
	}
	
	@Override
	public void producesUnobfuscatedResults(AbstractArchiveTask task) {
		//loom changes `jar`'s dest dir to ./build/devlibs, and uses a separate RemapJarTask to create
		//the jar in ./build/libs
		TaskProvider<Jar> mainJarTask = project.getTasks().named("jar", Jar.class);
		task.getDestinationDirectory().set(mainJarTask.flatMap(AbstractArchiveTask::getDestinationDirectory));
		task.getArchiveClassifier().set("dev");
	}
	
	@Override
	public void jijQuatlib() {
		LoaderMod quatlib = mods.getByName("modder_name_lib");
		
		for(LoaderMod mod : mods) {
			if(mod != quatlib && mod.dependOnQuatlib) {
				mod.depJarNamedLoom.configure(it -> {
					//for some reason you can JiJ stuff from RemapJarTask?
					//not sure what that has to do with remapping but ok :thumbs_up: sure
					it.dependsOn(quatlib.depJarNamedLoom);
					it.getNestedJars().from(quatlib.depJarNamedLoom.get().getArchiveFile());
					it.getAddNestedDependencies().set(true);
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
	public void refmapAddArgsNowOrLater(Runnable r) {
		project.afterEvaluate(__ -> r.run()); //has to be done after fabric-loom inits some stuff
	}
	
	@Override
	public File refmapGetMappingsIn() {
		return loom.getMappingsFile(); //Kabooms if this is called before fabric-loom inits some stuff
	}
	
	@Override
	public List<String> refmapArgs(File mappingsIn, File mappingsOut, File refmapOut) {
		return List.of(
			"-AinMapFileNamedIntermediary=" + mappingsIn.getAbsolutePath(),
			"-AoutMapFileNamedIntermediary=" + mappingsOut.getAbsolutePath(),
			"-AoutRefMapFile=" + refmapOut.getAbsolutePath(),
			"-AdefaultObfuscationEnv=named:intermediary"
		);
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

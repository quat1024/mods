package agency.highlysuspect.modsetup;

import net.fabricmc.loom.LoomGradlePlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class FabricSetupPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		new ModSetupPlugin().apply(project);
		new LoomGradlePlugin().apply(project);
		
		ModSetupExtension modSetup = project.getExtensions().getByType(ModSetupExtension.class);
		modSetup.loader = "fabric";
		
		if(true) return;
		
//		modSetup.after(ext -> {
//			LoomGradleExtensionAPI loom = project.getExtensions().getByType(LoomGradleExtensionAPI.class);
//			SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
//			SourceSet main = sourceSets.getByName("main");
//			DependencyHandler dependencies = project.getDependencies();
//			ConfigurationContainer configurations = project.getConfigurations();
//			TaskContainer tasks = project.getTasks();
//			String ver = ext.ver;
//			String loader = ext.loader;
//
//			//setup minecraft with official names
//			project.getDependencies().add("minecraft", "com.mojang:minecraft:" + ver);
//			project.getDependencies().add("mappings", loom.officialMojangMappings());
//
//			//create a remap task for quatlibFatJar, and append -dev
//			//to the name of the nonfat jar
//			if(true) return;
//			Property<String> currentArchiveNameProp = ext.quatlibFatJar.get().getArchiveBaseName();
//			String currentArchiveName = currentArchiveNameProp.get();
//			currentArchiveNameProp.set(currentArchiveName + "-dev");
//
//
//			TaskProvider<RemapJarTask> quatlibFatJarNamed = tasks.register("quatlibFatJarNamed", RemapJarTask.class, it -> {
//				it.getArchiveBaseName().set(currentArchiveName);
//				it.getInputFile().value(ext.quatlibFatJar.flatMap(AbstractArchiveTask::getArchiveFile));
//				it.dependsOn(ext.quatlibFatJar);
//			});
//			tasks.named("jar", it -> it.dependsOn(quatlibFatJarNamed));
//
//			for(Mod modOptions : ext.mods) {
//				String mod = modOptions.modid;
//				project.getLogger().lifecycle("GOT MOD!!!! " + mod);
//
//				//slap this mod's runtime classpath into the run configs
//				tasks.withType(AbstractRunTask.class).configureEach(it -> {
//					it.classpath(modOptions.set.getRuntimeClasspath());
//				});
//
//				//create a remap task for this mod's depjar
//				TaskProvider<RemapJarTask> depJarNamed = tasks.register(ext.modVersion(mod, ver) + "Loader" + StringGroovyMethods.capitalize(loader) + "DepJarNamed", RemapJarTask.class, it -> {
//					it.dependsOn(modOptions.depJar, ext.quatlibFatJar, quatlibFatJarNamed);
//
//					it.getArchiveBaseName().set(mod + "-" + ver + "-" + loader);
//					it.getInputFile().set(modOptions.depJar.flatMap(AbstractArchiveTask::getArchiveFile));
//
//					//put quatlib-fat-dev on the remap classpath so tiny-remapper can see into it
//					it.getClasspath().from(ext.quatlibFatJar.get().getArchiveFile());
//
//					//for some reason you can JiJ stuff from RemapJarTask?
//					//not sure what that has to do with remapping but ok :thumbs_up: han
//					it.getNestedJars().from(quatlibFatJarNamed.get().getArchiveFile());
//					it.getAddNestedDependencies().set(true);
//				});
//				tasks.named("jar", it -> it.dependsOn(depJarNamed));
//			}
//		});
	}
}

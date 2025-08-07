package agency.highlysuspect.modsetup.liason;

import agency.highlysuspect.modsetup.LoaderMod;
import net.neoforged.moddevgradle.legacyforge.dsl.LegacyForgeExtension;
import net.neoforged.moddevgradle.legacyforge.dsl.ObfuscationExtension;
import net.neoforged.moddevgradle.legacyforge.tasks.RemapJar;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.component.AdhocComponentWithVariants;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public class ForgeViaMdgLiason extends MdgLiason<LegacyForgeExtension> implements Liason.RemapLiason, Liason.RefmapLiason {
	public ForgeViaMdgLiason(Project project, String ver, NamedDomainObjectContainer<LoaderMod> mods) {
		super(project, ver, mods);
		this.obf = extensions.getByType(ObfuscationExtension.class);
	}
	
	protected final ObfuscationExtension obf;
	
	@Override
	public String getLoaderIdentifier() {
		return "forge";
	}
	
	@Override
	protected Class<LegacyForgeExtension> extClass() {
		return LegacyForgeExtension.class;
	}
	
	@Override
	public void disableUnusedDefaultTasks() {
		tasks.named("reobfJar", it -> it.setEnabled(false));
	}
	
	/// REMAPS ///
	@Override
	public void remaps(Consumer<RemapLiason> remaps) {
		remaps.accept(this);
	}
	
	@Override
	public void createIncomingRemapConfigurations(LoaderMod mod) {
		//creates 'modidImplementation' which will contain modid's deps remapped
		Configuration remappedIn = configurations.resolvable(snakeToCamel(mod.modid) + "Implementation").get();
		
		//put this configuration on the compile classpath of the regular source set (?)
		withImplementation(mod.set, remappedIn);
		
		//tell mdg to create modModidImplementation and remap its contents into regular modidImplementation
		obf.createRemappingConfiguration(remappedIn);
	}
	
	@Override
	public void remapMods() {
		//the "obf.reobfuscate" function looks these configurations up by name, and they have to exist.
		//TODO, can i avoid this headache by actually using these configuration names? lol
		for(LoaderMod mod : mods) {
			SourceSet set = mod.set;
			
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
		};
		
		//reobf jar tasks
		for(LoaderMod mod : mods) {
			TaskProvider<RemapJar> reobfTask = obf.reobfuscate(mod.depJar, mod.set, it -> {
				it.getArchiveBaseName().set(mod.modid + "-" + ver + "-" + loader);
				//forge is inheriting the classifier from the depJar task so remove the -dev suffix lol
				it.getArchiveClassifier().set("");
			});
			//hang off vanilla task
			tasks.named("jar").configure(it -> it.dependsOn(reobfTask));
		}
	}
	
	/// REFMAPS ///
	
	@Override
	public void refmaps(Consumer<RefmapLiason> refmaps) {
		refmaps.accept(this); //Now
	}
	
	@Override
	public void refmapAddMixinAp(Configuration mixinAp) {
		//the stock mixin ap is available on sponge's repo, and supports ObfuscationServiceMCP out of the box
		addSpongeRepo();
		withDeps(mixinAp, "org.spongepowered:mixin:0.8.5:processor");
	}
	
	@SuppressWarnings("UnstableApiUsage")
	@Override
	public File refmapGetMappingsIn() {
		return obf.getNamedToSrgMappings().getAsFile().get();
	}
	
	@Override
	public List<String> refmapArgs(RegularFileProperty mappingsIn, RegularFileProperty mappingsOut, RegularFileProperty refmapOut) {
		return List.of(
			"-AreobfTsrgFile=" + mappingsIn.get().getAsFile().getAbsolutePath(),
			"-AoutTsrgFile=" + mappingsOut.get().getAsFile().getAbsolutePath(),
			"-AoutRefMapFile=" + refmapOut.get().getAsFile().getAbsolutePath(),
			"-AdefaultObfuscationEnv=searge",
			"-AmappingTypes=tsrg",
			"-ApluginVersion=0.9" //just for silencing a warning(?)
		);
	}
	
	@Override
	public void configureRefmapTask(TaskProvider<JavaCompile> task) {
		//createMinecraftArtifacts is what populates the obf.getNamedToSrgMappings() file
		//TODO: is this still needed now that i hang things off processResources
		task.configure(it -> it.dependsOn("createMinecraftArtifacts"));
	}
}

package agency.highlysuspect.modsetup.liason;

import agency.highlysuspect.modsetup.LoaderMod;
import net.neoforged.moddevgradle.legacyforge.dsl.LegacyForgeExtension;
import net.neoforged.moddevgradle.legacyforge.dsl.ObfuscationExtension;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.component.AdhocComponentWithVariants;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

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
	public void createIncomingRemapConfigurations(SourceSet set) {
		//TODO? (stuff like "modXxxxxImplementation")
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
			obf.reobfuscate(mod.depJar, mod.set, it -> {
				it.getArchiveBaseName().set(mod.modid + "-" + ver + "-" + loader);
			});
		}
	}
	
	@Override
	public void producesUnobfuscatedResults(AbstractArchiveTask task) {
		//TODO? (put in a devlibs folder like fabric)
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
	
	@Override
	public void refmapAddArgsNowOrLater(Runnable r) {
		r.run(); //now
	}
	
	@SuppressWarnings("UnstableApiUsage")
	@Override
	public File refmapGetMappingsIn() {
		return obf.getNamedToSrgMappings().getAsFile().get();
	}
	
	@Override
	public List<String> refmapArgs(File mappingsIn, File mappingsOut, File refmapOut) {
		return List.of(
			"-AreobfTsrgFile=" + mappingsIn.getAbsolutePath(),
			"-AoutTsrgFile=" + mappingsOut.getAbsolutePath(),
			"-AoutRefMapFile=" + refmapOut.getAbsolutePath(),
			"-AdefaultObfuscationEnv=searge",
			"-AmappingTypes=tsrg",
			"-ApluginVersion=0.9" //just for silencing a warning(?)
		);
	}
}

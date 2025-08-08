package agency.highlysuspect.modsetup.liason;

import agency.highlysuspect.modsetup.LoaderMod;
import agency.highlysuspect.modsetup.RefmapJob;
import net.neoforged.moddevgradle.legacyforge.dsl.LegacyForgeExtension;
import net.neoforged.moddevgradle.legacyforge.dsl.ObfuscationExtension;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.component.AdhocComponentWithVariants;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ForgeViaMdgLiason extends MdgLiason<LegacyForgeExtension> implements Liason.RemapLiason, Liason.RefmapLiason {
	public ForgeViaMdgLiason(Project project, String ver, NamedDomainObjectContainer<LoaderMod> mods) {
		super(project, ver, "forge", mods, LegacyForgeExtension.class);
		this.obf = project.getExtensions().getByType(ObfuscationExtension.class);
	}
	
	protected final ObfuscationExtension obf;
	
	@Override
	public void disableUnusedDefaultTasks() {
		super.disableUnusedDefaultTasks();
		project.getTasks().named("reobfJar", it -> it.setEnabled(false));
	}
	
	/// REFMAPS ///
	@Override
	public @Nullable RefmapLiason getRefmapLiason() {
		return this;
	}
	
	@Override
	public void refmapAddMixinAp(Configuration mixinAp) {
		//The stock mixin AP supports ObfuscationServiceMCP and is available on sponge's repo.
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
		//Refmapping has to run after "createMinecraftArtifacts" since that task populates the obf.getNamedToSrgMappings() file
		task.configure(it -> it.dependsOn("createMinecraftArtifacts"));
	}
	
	/// REMAPS ///
	@Override
	public @Nullable RemapLiason getRemapLiason() {
		return this;
	}
	
	@Override
	public void createIncomingRemapConfigurations(LoaderMod mod) {
		Configuration remappedIn = project.getConfigurations().resolvable(snakeToCamel(mod.modid) + "Implementation").get();
		withImplementation(mod.set, remappedIn);
		obf.createRemappingConfiguration(remappedIn);
	}
	
	@Override
	public void remapMods() {
		//the "obf.reobfuscate" function looks these configurations up by name and they have to exist.
		//TODO, can i avoid this headache by actually using these configurations? lol
		for(LoaderMod mod : mods) {
			SourceSet set = mod.set;
			
			Configuration runtimeElements = project.getConfigurations().maybeCreate(set.getRuntimeElementsConfigurationName());
			Configuration apiElements = project.getConfigurations().maybeCreate(set.getApiElementsConfigurationName());
			withDeps(runtimeElements, set.getOutput());
			withDeps(apiElements, set.getOutput());
			
			//obf.reobfuscate calls withVariantsForConfiguration before addVariantsForConfiguration,
			//and withVars requires (whatever this is) to be registered via addVars first, apparently.
			//"Variant for configuration ... does not exist in ..."
			//I think this qualifies as an mdg bug honestly, it should call the functions in the other order
			//TODO repro in a more normal environment
			AdhocComponentWithVariants umm = (AdhocComponentWithVariants) project.getComponents().getByName("java");
			umm.addVariantsFromConfiguration(runtimeElements, it -> {});
			umm.addVariantsFromConfiguration(apiElements, it -> {});
		};
		
		for(LoaderMod mod : mods) {
			mod.depJarNamed = obf.reobfuscate(mod.depJar, mod.set, it -> {
				//They inherit the classifier from ^ that task, remove it to avoid a -dev suffix lol
				it.getArchiveClassifier().set("");
				it.getArchiveBaseName().set(mod.modid + "-" + ver + "-" + loader);
				
				//Add the mixin AP output to the list of mappings files to use
				for(RefmapJob job : mod.refmapJobs) {
					it.dependsOn(job.task);
					it.mustRunAfter(job.task);
					it.getRemapOperation().getMappings().from(job.mappingsOut);
				}
			});
		}
	}
	
	@Override
	public void setupRuns() {
		super.setupRuns();
		
		ext.getRuns().configureEach(run -> {
			//https://github.com/neoforged/ModDevGradle/blob/feb8afa7672da0bd01e293cfe2be69db0fd47668/src/legacy/java/net/neoforged/moddevgradle/legacyforge/internal/LegacyForgeModDevPlugin.java#L183
			
			List<String> mixinConfigArgs = new ArrayList<>();
			for(LoaderMod mod : mods) {
				for (String legacyMixinConfig : mod.legacyForgeMixinConfigs) {
					mixinConfigArgs.add("--mixin.config");
					mixinConfigArgs.add(legacyMixinConfig);
				}
			}
			
			run.getProgramArguments().addAll(mixinConfigArgs);
		});
	}
}

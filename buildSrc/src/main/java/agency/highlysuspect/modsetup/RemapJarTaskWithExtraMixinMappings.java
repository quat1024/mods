package agency.highlysuspect.modsetup;

import net.fabricmc.loom.configuration.mods.ArtifactMetadata;
import net.fabricmc.loom.task.RemapJarTask;
import net.fabricmc.loom.task.service.MappingsService;
import net.fabricmc.loom.task.service.TinyRemapperService;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

/**
 * RemapJarTask but you can submit additional mappings files.
 *
 * I tried using getTinyRemapperServiceOptions().get().getMappings().add() on the regular
 * RemapJarTask but it was silently failing? Just wasn't taking those mappings into consideration.
 * Feel like my changes were getting lost in the sea of Properties and Providers.
 * Whatever man, I'll just sledgehammer it
 */
public abstract class RemapJarTaskWithExtraMixinMappings extends RemapJarTask {
	@InputFiles
	public abstract ConfigurableFileCollection getMixinApExtraMappings_QUAT();
	
	@Override
	@TaskAction
	public void run() {
		this.submitWork(RemapAction.class, (params) -> {
			TinyRemapperService.Options trsOptions = this.getTinyRemapperServiceOptions().get();
			
			for(File extraMappingFile : getMixinApExtraMappings_QUAT()) {
				getLogger().lifecycle("Adding {} from mixinApExtraMappings_QUAT", extraMappingFile);
				//Can't use loom's ServiceType#create since talking about Project is verboten at this time
				MappingsService.Options opts = getObjectFactory().newInstance(MappingsService.Options.class);
				opts.getServiceClass().set(MappingsService.class.getName());
				
				opts.getMappingsFile().set(extraMappingFile);
				opts.getFrom().set("named");
				opts.getTo().set("intermediary");
				opts.getRemapLocals().set(false);
				trsOptions.getMappings().add(opts);
			}
			
			//<copypaste>
			if (this.getAddNestedDependencies().get()) {
				params.getNestedJars().from(this.getNestedJars());
			}
			
			if (!params.namespacesMatch()) {
				params.getTinyRemapperServiceOptions().set(this.getTinyRemapperServiceOptions());
				params.getMixinRefmapServiceOptions().set(this.getMixinRefmapServiceOptions());
				params.getRemapClasspath().from(this.getClasspath());
				boolean mixinAp = this.getUseMixinAP().get();
				params.getUseMixinExtension().set(!mixinAp);
				ArtifactMetadata.MixinRemapType refmapRemapType = mixinAp ? ArtifactMetadata.MixinRemapType.MIXIN : ArtifactMetadata.MixinRemapType.STATIC;
				params.getManifestAttributes().put("Fabric-Loom-Mixin-Remap-Type", refmapRemapType.manifestValue());
			}
			
			params.getOptimizeFmj().set(this.getOptimizeFabricModJson().get());
			//</copypaste>
		});
	}
}

package agency.highlysuspect.modsetup.liason;

import agency.highlysuspect.modsetup.LoaderMod;
import net.neoforged.moddevgradle.dsl.NeoForgeExtension;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

import java.util.function.Consumer;

public class NeoforgeLiason extends MdgLiason<NeoForgeExtension> {
	public NeoforgeLiason(Project project, String ver, NamedDomainObjectContainer<LoaderMod> mods) {
		super(project, ver, mods);
	}
	
	@Override
	public String getLoaderIdentifier() {
		return "neoforge";
	}
	
	@Override
	protected Class<NeoForgeExtension> extClass() {
		return NeoForgeExtension.class;
	}
	
	@Override
	public void disableUnusedDefaultTasks() {
		//no-op
	}
}

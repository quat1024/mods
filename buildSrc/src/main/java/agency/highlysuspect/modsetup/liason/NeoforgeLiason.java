package agency.highlysuspect.modsetup.liason;

import agency.highlysuspect.modsetup.LoaderMod;
import net.neoforged.moddevgradle.dsl.NeoForgeExtension;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.jetbrains.annotations.Nullable;

public class NeoforgeLiason extends MdgLiason<NeoForgeExtension> {
	public NeoforgeLiason(Project project, String ver, NamedDomainObjectContainer<LoaderMod> mods) {
		super(project, ver, "neoforge", mods, NeoForgeExtension.class);
	}
	
	@Override
	public @Nullable RefmapLiason getRefmapLiason() {
		//No need to work with refmaps on neoforge
		return null;
	}
	
	@Override
	public @Nullable RemapLiason getRemapLiason() {
		//No need to remap mods on neoforge
		return null;
	}
}

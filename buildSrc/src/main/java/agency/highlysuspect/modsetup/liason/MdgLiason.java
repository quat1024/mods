package agency.highlysuspect.modsetup.liason;

import agency.highlysuspect.modsetup.LoaderMod;
import net.neoforged.moddevgradle.dsl.ModDevExtension;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;

public abstract class MdgLiason<EXT extends ModDevExtension> extends Liason {
	public MdgLiason(Project project, String ver, String loader, NamedDomainObjectContainer<LoaderMod> mods, Class<EXT> extClass) {
		super(project, ver, loader, mods);
		ext = project.getExtensions().getByType(extClass);
	}
	
	protected final EXT ext;
	
	@Override
	public void setupOfficialNames() {
		//MDG already uses official names.
	}
	
	@Override
	public void jijQuatlib() {
		//TODO
	}
	
	@Override
	public void setupRuns() {
		ext.getRuns().create("client", it -> {
			it.client();
		});
		
		for(LoaderMod mod : mods) {
			ext.getMods().create(mod.modid, it -> {
				it.sourceSet(mod.set);
				it.sourceSet(mod.versionAgnosticSourceSet);
				it.sourceSet(mod.getPerVersionSourceSet(ver));
			});
		}
	}
}

package agency.highlysuspect.modsetup.liason;

import agency.highlysuspect.modsetup.LoaderMod;
import agency.highlysuspect.modsetup.NeoishJarJar;
import net.neoforged.moddevgradle.dsl.ModDevExtension;
import net.neoforged.moddevgradle.internal.RunGameTask;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class MdgLiason<EXT extends ModDevExtension> extends Liason implements Liason.JarInJarLiason {
	public MdgLiason(Project project, String ver, String loader, NamedDomainObjectContainer<LoaderMod> mods, Class<EXT> extClass) {
		super(project, ver, loader, mods);
		ext = project.getExtensions().getByType(extClass);
	}
	
	protected final EXT ext;
	
	@Override
	public void disableUnusedDefaultTasks() {
		//todo needed on mdg or are these from fabric
		//project.getTasks().named("runClient").configure(it -> it.setEnabled(false));
		//project.getTasks().named("runServer").configure(it -> it.setEnabled(false));
	}
	
	@Override
	public void setupOfficialNames() {
		//MDG already uses official names.
	}
	
	@Override
	public @Nullable JarInJarLiason getJarInJarLiason() {
		return this;
	}
	
	@Override
	public void setupJarInJars() {
		//TODO leaky abstraction i'm just lazy
		boolean obfuscated = this instanceof ForgeViaMdgLiason;
		List<NeoishJarJar.MetadataEntry> justQuatlib = List.of(new NeoishJarJar.MetadataEntry(mods.getByName("modder_name_lib"), obfuscated));
		
		NeoishJarJar.makeJarjarTasks(project, mods, mod -> mod.dependOnQuatlib ? justQuatlib : List.of());
	}
	
	@Override
	public void setupRuns() {
		ext.getRuns().clear();
		
		ext.getRuns().create("client-" + ver.replace('.', '-') + "-" + loader, it -> {
			it.client();
		});
		
		for(LoaderMod mod : mods) {
			ext.getMods().create(mod.modid, it -> {
				it.sourceSet(mod.set);
				it.sourceSet(mod.versionAgnosticSourceSet);
				it.sourceSet(mod.getPerVersionSourceSet(ver));
			});
		}
		
		project.getTasks().withType(RunGameTask.class, it -> it.setGroup("runs"));
	}
}

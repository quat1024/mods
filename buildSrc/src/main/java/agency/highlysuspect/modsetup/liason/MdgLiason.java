package agency.highlysuspect.modsetup.liason;

import agency.highlysuspect.modsetup.JarJarJarJarJar;
import agency.highlysuspect.modsetup.LoaderMod;
import net.neoforged.moddevgradle.dsl.ModDevExtension;
import net.neoforged.moddevgradle.internal.RunGameTask;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.jvm.tasks.Jar;

import java.util.List;

public abstract class MdgLiason<EXT extends ModDevExtension> extends Liason {
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
	public void jijQuatlib() {
		List<LoaderMod> toJarjar = List.of(mods.getByName("modder_name_lib"));
		
		for(LoaderMod mod : mods) {
			if(!mod.dependOnQuatlib) continue;
			
			TaskProvider<? extends Jar> oldJarNamed = mod.depJarNamed;
			oldJarNamed.configure(it -> {
				//todo move into dev directory
				it.getArchiveClassifier().set("NOJARJAR");
			});
			
			TaskProvider<Jar> asdf = project.getTasks().register(oldJarNamed.getName() + "_withJarjar", Jar.class, it -> {
				it.dependsOn(oldJarNamed);
				it.from(project.zipTree(oldJarNamed.flatMap(AbstractArchiveTask::getArchiveFile)));
				it.setManifest(oldJarNamed.get().getManifest());
				it.getArchiveBaseName().convention(oldJarNamed.get().getArchiveBaseName());
			});
			JarJarJarJarJar.jarJarJarJar(project, asdf, toJarjar, false);
			
			mod.depJarNamed = asdf;
		}
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

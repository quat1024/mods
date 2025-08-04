package agency.highlysuspect.modsetup.liason;

import agency.highlysuspect.modsetup.Util;
import agency.highlysuspect.modsetup.LoaderMod;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

//TODO: rm AbstractSetupExtension, push methods down into here
public abstract class Liason extends Util {
	public Liason(Project project, String ver, NamedDomainObjectContainer<LoaderMod> mods) {
		super(project);
		
		this.project = project;
		this.ver = ver;
		this.mods = mods;
		
		this.extensions = project.getExtensions();
		this.sourceSets = extensions.getByType(SourceSetContainer.class);
		this.main = sourceSets.getByName("main");
		this.dependencies = project.getDependencies();
		this.configurations = project.getConfigurations();
		this.tasks = project.getTasks();
	}
	
	
	public final String loader = getLoaderIdentifier();
	public final String ver;
	public final NamedDomainObjectContainer<LoaderMod> mods;
	
	protected final Project project;
	protected final ExtensionContainer extensions;
	protected final SourceSetContainer sourceSets;
	protected final SourceSet main;
	protected final DependencyHandler dependencies;
	protected final ConfigurationContainer configurations;
	protected final TaskContainer tasks;
	
	public abstract String getLoaderIdentifier();
	public abstract void setupOfficialNames();
	public abstract void disableUnusedDefaultTasks();
	public abstract void jijQuatlib();
	public abstract void addFloaderOnlyDep(SourceSet quatlib);
	public abstract void setupRuns();
	
	//remaps
	public void remaps(Consumer<RemapLiason> remaps) {
		//no-op by default
	}
	
	public interface RemapLiason {
		void createIncomingRemapConfigurations(SourceSet set);
		void remapMods();
		void producesUnobfuscatedResults(AbstractArchiveTask task);
	}
	
	//refmaps
	public void refmaps(Consumer<RefmapLiason> refmaps) {
		//no-op by default
	}
	
	public interface RefmapLiason {
		void refmapAddMixinAp(Configuration mixinAp);
		
		void refmapAddArgsNowOrLater(Runnable r);
		File refmapGetMappingsIn();
		List<String> refmapArgs(File mappingsIn, File mappingsOut, File refmapOut);
	}
}

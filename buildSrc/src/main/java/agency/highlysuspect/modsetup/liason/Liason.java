package agency.highlysuspect.modsetup.liason;

import agency.highlysuspect.modsetup.LoaderMod;
import agency.highlysuspect.modsetup.Util;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

public abstract class Liason extends Util {
	public Liason(Project project, String ver, String loader, NamedDomainObjectContainer<LoaderMod> mods) {
		super(project);
		
		this.loader = loader;
		this.ver = ver;
		this.mods = mods;
		
		this.project = project;
	}
	
	public final String loader;
	public final String ver;
	public final NamedDomainObjectContainer<LoaderMod> mods;
	
	protected final Project project;
	
	public abstract void setupOfficialNames();
	public abstract void disableUnusedDefaultTasks();
	public abstract void setupRuns();
	
	public abstract @Nullable RefmapLiason getRefmapLiason();
	public abstract @Nullable RemapLiason getRemapLiason();
	public abstract @Nullable JarInJarLiason getJarInJarLiason();
	
	public interface RemapLiason {
		void createIncomingRemapConfigurations(LoaderMod mod);
		void remapMods();
	}
	
	public interface RefmapLiason {
		void refmapAddMixinAp(Configuration mixinAp);
		
		File refmapGetMappingsIn();
		List<String> refmapArgs(RegularFileProperty mappingsIn, RegularFileProperty mappingsOut, RegularFileProperty refmapOut);
		void configureRefmapTask(TaskProvider<JavaCompile> task);
	}
	
	public interface JarInJarLiason {
		void setupJarInJars();
	}
}

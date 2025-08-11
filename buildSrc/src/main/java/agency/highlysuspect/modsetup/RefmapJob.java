package agency.highlysuspect.modsetup;

import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.gradle.api.Named;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.nio.file.Files;

public abstract class RefmapJob implements Named {
	@Inject
	public RefmapJob(@NotNull String modid) {
		this.modid = modid;
	}
	
	public final String modid;
	
	public ListProperty<String> inputMixinJsons = getObjects().listProperty(String.class);
	
	public FileCollection sources = getObjects().fileCollection();
	public FileCollection classpath = getObjects().fileCollection();
	public FileCollection apPath = getObjects().fileCollection();
	
	public RegularFileProperty mappingsIn = getObjects().fileProperty();
	public RegularFileProperty refmapOut = getObjects().fileProperty();
	public RegularFileProperty mappingsOut = getObjects().fileProperty();
	
	//todo: compute from mappingsIn, refmapOut, mappingsOut, and a loader enum instead?
	public ListProperty<String> args = getObjects().listProperty(String.class);
	
	public TaskProvider<JavaCompile> task;
	
	{
		mappingsIn.finalizeValueOnRead();
		refmapOut.finalizeValueOnRead();
		mappingsOut.finalizeValueOnRead();
		args.finalizeValueOnRead();
	}
	
	public TaskProvider<JavaCompile> makeTask(Project project) {
		//Nothing will be written here due to -proc:only, but Gradle requies you to pass a directory anyway.
		Provider<Directory> tmpDir = project.getLayout().getBuildDirectory().dir("mixin2/work");
		try {
			Files.createDirectories(tmpDir.get().getAsFile().toPath());
		} catch (Exception e) {
			throw new RuntimeException("failed mkdirs", e);
		}
		
		return task = project.getTasks().register("generateRefmaps" + StringGroovyMethods.capitalize(Util.snakeToCamel(modid)), JavaCompile.class, it -> {
			it.setSource(sources);
			it.setClasspath(classpath);
			it.getOptions().setAnnotationProcessorPath(apPath);
			it.getDestinationDirectory().set(tmpDir);
			it.getOptions().getCompilerArgs().add("-proc:only");
			it.getOptions().getCompilerArgumentProviders().add(args::get);
		});
	}
	
	public RefmapJob addSources(FileCollection sources) {
		this.sources = this.sources.plus(sources);
		return this;
	}
	
	public RefmapJob addClasspath(FileCollection classpath) {
		this.classpath = this.classpath.plus(classpath);
		return this;
	}
	
	public RefmapJob addAp(FileCollection ap) {
		this.apPath = this.apPath.plus(ap);
		return this;
	}
	
	@Override
	public @NotNull String getName() {
		return modid;
	}
	
	@Inject
	protected ObjectFactory getObjects() {
		throw new RuntimeException();
	}
}

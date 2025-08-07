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

public abstract class RefmapJob implements Named {
	@Inject
	public RefmapJob(@NotNull String name) {
		this.name = name;
	}
	
	public final String name;
	
	ListProperty<String> inputMixinJsons = getObjects().listProperty(String.class);
	
	FileCollection sources = getObjects().fileCollection();
	FileCollection classpath = getObjects().fileCollection();
	FileCollection apPath = getObjects().fileCollection();
	
	RegularFileProperty mappingsIn = getObjects().fileProperty();
	RegularFileProperty refmapOut = getObjects().fileProperty();
	RegularFileProperty mappingsOut = getObjects().fileProperty();
	
	//todo: compute from mappingsIn, refmapOut, mappingsOut, and a loader enum instead?
	ListProperty<String> args = getObjects().listProperty(String.class);
	
	{
		mappingsIn.finalizeValueOnRead();
		refmapOut.finalizeValueOnRead();
		mappingsOut.finalizeValueOnRead();
		args.finalizeValueOnRead();
	}
	
	public TaskProvider<JavaCompile> makeTask(Project project) {
		//Nothing will be written here due to -proc:only, but Gradle requies you to pass a directory anyway.
		Provider<Directory> tmpDir = project.getLayout().getBuildDirectory().dir("mixin2/work");
		
		return project.getTasks().register("generateRefmaps" + StringGroovyMethods.capitalize(name), JavaCompile.class, it -> {
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
		return name;
	}
	
	@Inject
	protected ObjectFactory getObjects() {
		throw new RuntimeException();
	}
}

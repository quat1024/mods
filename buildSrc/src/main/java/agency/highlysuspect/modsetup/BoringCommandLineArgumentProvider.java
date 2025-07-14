package agency.highlysuspect.modsetup;

import org.gradle.api.Project;
import org.gradle.api.provider.ListProperty;
import org.gradle.process.CommandLineArgumentProvider;

import javax.inject.Inject;

public abstract class BoringCommandLineArgumentProvider implements CommandLineArgumentProvider {
	@Inject
	public BoringCommandLineArgumentProvider(Project project) {
		args = project.getObjects().listProperty(String.class);
	}
	
	public final ListProperty<String> args;
	
	@Override
	public Iterable<String> asArguments() {
		return args.get();
	}
}

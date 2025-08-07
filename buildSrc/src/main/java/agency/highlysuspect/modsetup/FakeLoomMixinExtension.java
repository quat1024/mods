package agency.highlysuspect.modsetup;

import net.fabricmc.loom.extension.MixinExtension;
import net.fabricmc.loom.extension.MixinExtensionApiImpl;
import net.fabricmc.loom.extension.MixinExtensionImpl;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.util.PatternSet;

import javax.inject.Inject;

public class FakeLoomMixinExtension extends MixinExtensionImpl {
	@Inject
	public FakeLoomMixinExtension(Project project) {
		super(project);
	}
	
	@Override
	protected PatternSet add0(SourceSet sourceSet, Provider<String> provider) {
		//doesn't add any mixin information containers to the source-set and returns bunk
		return new PatternSet();
	}
}

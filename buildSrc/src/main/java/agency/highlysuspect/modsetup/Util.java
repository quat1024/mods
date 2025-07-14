package agency.highlysuspect.modsetup;

import com.unascribed.flexver.FlexVerComparator;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Util {
	public static String modVersion(String mod, String version) {
		String mod2 = mod == null ? "Any" : StringGroovyMethods.capitalize(mod);
		String ver2 = version == null ? "Any" : version.replace('.', '_');
		return "mod" + mod2 + "Version" + ver2;
	}
	
	public static Dependency floaderOnlyDep(Project project) {
		return project.getDependencies().project(Map.of(
			"path", ":floader-only",
			"configuration", "floaderOnlyElements"
		));
	}
	
	public static List<Object> broadlyApplicableDeps(Project project) {
		return List.of(
			"org.jetbrains:annotations:16.0.2"
		);
	}
	
	public static Map<String, Object> broadlyApplicableProps(Project project) {
		return Map.of(
			"version", project.getVersion().toString(),
			"group", project.getGroup().toString(),
			"author", "quaternary",
			"license", "LGPL-3.0-or-later",
			"homepage", "https://highlysuspect.agency/",
			"sources", "https://github.com/quat1024/mods/",
			"issues", "https://github.com/quat1024/mods/issues"
		);
	}
	
	@SafeVarargs
	public static Map<String, Object> plus(Map<String, Object>... maps) {
		Map<String, Object> result = new HashMap<>();
		for(Map<String, Object> map : maps) result.putAll(map);
		return result;
	}
	
	// https://notes.highlysuspect.agency/versions.html
	public static int compatLevelForMinecraft(String minecraftVersion) {
		int result;
		if(FlexVerComparator.compare(minecraftVersion, "1.20.5") >= 0) result = 21;
		else if(FlexVerComparator.compare(minecraftVersion, "1.18.0") >= 0) result = 17;
		else if(FlexVerComparator.compare(minecraftVersion, "1.17.0") >= 0) result = 16;
		else result = 8;
		return result;
	}
	
	//Copypasta from ModDefGradle's MixinCompilerArgs private class
	//but using java File objects instead of the fancy gradle stuff
	public static List<String> mixinArgs(File inMappings, File outMappings, File refmap) {
		return List.of(
			"-AreobfTsrgFile=" + inMappings.getAbsolutePath(),
			"-AoutTsrgFile=" + outMappings.getAbsolutePath(),
			"-AoutRefMapFile=" + refmap.getAbsolutePath(),
			"-AmappingTypes=tsrg",
			"-ApluginVersion=0.7",
			"-AdefaultObfuscationEnv=searge");
	}
}

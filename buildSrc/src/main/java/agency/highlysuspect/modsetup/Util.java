package agency.highlysuspect.modsetup;

import com.unascribed.flexver.FlexVerComparator;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class Util {
	private static final Pattern underFollowedByLetter = Pattern.compile("_(.)");
	// my_cool_mod -> myCoolMod
	public static String snakeToCamel(String a) {
		return underFollowedByLetter.matcher(a)
			.replaceAll(r -> r.group(1).toUpperCase(Locale.ROOT));
	}
	// my_cool_mod -> My Cool Mod
	public static String snakeToPretty(String s) {
		return underFollowedByLetter.matcher(StringGroovyMethods.capitalize(s))
			.replaceAll(r -> " " + r.group(1).toUpperCase(Locale.ROOT));
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
	
	//Right-biased merge
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
	public static List<String> legacyForgeMixinArgs(File inMappings, File outMappings, File refmap) {
		return List.of(
			"-AreobfTsrgFile=" + inMappings.getAbsolutePath(),
			"-AoutTsrgFile=" + outMappings.getAbsolutePath(),
			"-AoutRefMapFile=" + refmap.getAbsolutePath(),
			"-AdefaultObfuscationEnv=searge",
			"-AmappingTypes=tsrg",
			"-ApluginVersion=0.9" //just for silencing a warning
		);
	}
	
	//https://github.com/FabricMC/fabric-loom/blob/c7accc60a49b086655305597d08b4df87317dce6/src/main/java/net/fabricmc/loom/build/mixin/AnnotationProcessorInvoker.java#L105-L111
	//https://github.com/FabricMC/fabric-loom/blob/c7accc60a49b086655305597d08b4df87317dce6/src/main/java/net/fabricmc/loom/util/Constants.java#L98-L103
	public static List<String> fabricMixinArgs(File inMappings, File outMappings, File refmap, String defaultObfuscationEnv) {
		return List.of(
			"-AinMapFileNamedIntermediary=" + inMappings.getAbsolutePath(),
			"-AoutMapFileNamedIntermediary=" + outMappings.getAbsolutePath(),
			"-AoutRefMapFile=" + refmap.getAbsolutePath(),
			"-AdefaultObfuscationEnv=", "named:" + defaultObfuscationEnv
		);
	}
}

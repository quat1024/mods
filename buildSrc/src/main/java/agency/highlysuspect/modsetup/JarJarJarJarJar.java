package agency.highlysuspect.modsetup;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.jvm.tasks.Jar;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public abstract class JarJarJarJarJar {
	public static void jarJarJarJar(Project project, TaskProvider<Jar> jarTask, List<LoaderMod> toJar, boolean isObfuscated) {
		List<JarJarManifestEntry> manifestEntries = toJar.stream()
			.map(mod -> {
				JarJarManifestEntry j = new JarJarManifestEntry();
				j.group = "agency.highlysuspect";
				j.artifact = mod.modid;
				j.version = project.getVersion().toString();
				j.jar = mod.depJarNamed.flatMap(AbstractArchiveTask::getArchiveFile);
				j.obfuscated = isObfuscated;
				return j;
			}).sorted().toList();
		
		//write the manifest
		File tmp = project.getLayout().getBuildDirectory().file("tmp/jarjar-manifests/" + jarTask.getName())
			.get().getAsFile();
		String iLoveConfigCache = Util.GSON.toJson(writeManifest(manifestEntries));
		TaskProvider<?> gggg =project.getTasks().register(jarTask.getName() + "_manifest", DefaultTask.class, write -> {
			write.getOutputs().file(tmp);
			write.doFirst(__ -> {
				try {
					Path t = tmp.toPath();
					Files.createDirectories(t.getParent());
					Files.writeString(t, iLoveConfigCache);
				} catch (IOException e) {
					throw new RuntimeException("poot", e);
				}
			});
		});
		
		jarTask.configure(it -> {
			for(LoaderMod mod : toJar) {
				//todo, really it should depend on the jarjar task (jars inside jars inside jars)
				it.dependsOn(mod.depJarNamed);
			}
			it.dependsOn(gggg);
			
			//include jars in the copy task
			for (JarJarManifestEntry e : manifestEntries) {
				it.from(e.jar, aa -> aa.into("META-INF/jarjar/"));
			}
			
			//include manifest too
			it.from(tmp, aa -> aa.into("META-INF/jarjar/").rename(".*", "metadata.json"));
		});
	}
	
	protected static JsonObject writeManifest(List<JarJarManifestEntry> entries) {
		JsonObject manifest = new JsonObject();
		
		JsonArray jars = new JsonArray();
		for(JarJarManifestEntry e : entries) jars.add(e.toJson());
		manifest.add("jars", jars);
		
		return manifest;
	}
	
	public static class JarJarManifestEntry implements Comparable<JarJarManifestEntry> {
		String group, artifact, version;
		Provider<RegularFile> jar;
		boolean obfuscated;
		
		@Override
		public int compareTo(@NotNull JarJarJarJarJar.JarJarManifestEntry other) {
			return artifact.compareTo(other.artifact); //modids
		}
		
		public JsonObject toJson() {
			JsonObject j = new JsonObject();
			
			JsonObject identifier = new JsonObject();
			identifier.addProperty("group", this.group);
			identifier.addProperty("artifact", this.artifact);
			j.add("identifier", identifier);
			
			JsonObject version = new JsonObject();
			version.addProperty("artifactVersion", this.version);
			version.addProperty("range", "[" + this.version + ",)"); //this version or newer
			j.add("version", version);
			
			j.addProperty("path", "META-INF/jarjar/" + jar.get().getAsFile().getName());
			j.addProperty("isObfuscated", obfuscated);
			
			return j;
		}
	}
}

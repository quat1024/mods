package agency.highlysuspect.modsetup;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.jvm.tasks.Jar;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * shitty minimal impl of modernforge / neoforge style jar jar system
 */
public abstract class NeoishJarJar {
	public static void makeJarjarTasks(Project project, Iterable<LoaderMod> mods, Function<LoaderMod, List<MetadataEntry>> thingsToJarFunc) {
		for(LoaderMod mod : mods) {
			//0. first do we actually need to jarjar
			if(thingsToJarFunc.apply(mod).isEmpty()) {
				mod.depJarNamedJarjarred = mod.depJarNamed;
				continue;
			}
			
			//1. move the non-jarjarred task out of the way
			Provider<Directory> devlibs = project.getLayout().getBuildDirectory().dir("devlibs");
			mod.depJarNamed.configure(it -> {
				it.getArchiveClassifier().set("no-jarjar");
				it.getDestinationDirectory().set(devlibs);
			});
			
			//2. create tasks to hold jarjarred mods, by default just a copy of depJarNamed
			mod.depJarNamedJarjarred = project.getTasks().register(mod.depJarNamed + "_jarjar", Jar.class, it -> {
				it.dependsOn(mod.depJarNamed);
				it.from(project.zipTree(mod.depJarNamed.flatMap(AbstractArchiveTask::getArchiveFile)));
				it.getArchiveBaseName().convention(mod.depJarNamed.flatMap(AbstractArchiveTask::getArchiveBaseName));
				
				//trying to do this lazily
//				it.manifest(mani -> mani.from(mod.depJarNamed.map(Jar::getManifest))); //nope that needs to be a fuckikng file !!
				//break glass in case of emergency:
				it.setManifest(mod.depJarNamed.get().getManifest());
			});
		}
		
		//separate loop
		for(LoaderMod mod : mods) {
			List<MetadataEntry> thingsToJar = new ArrayList<>(thingsToJarFunc.apply(mod));
			if(thingsToJar.isEmpty()) continue;
			
			//3. build the metadata json
			//has to be done outside of task execution because (mumbles something about configuration cache)
			Collections.sort(thingsToJar);
			
			JsonObject metadataJsonObj = new JsonObject();
			JsonArray jars = new JsonArray();
			for(MetadataEntry e : thingsToJar) jars.add(e.toJson(project.getVersion()));
			metadataJsonObj.add("jars", jars);
			
			String metadataJson = Util.GSON.toJson(metadataJsonObj);
			
			//4. write that metadata json to a file because you have to do this in order to include it in a jar task.
			//isn't gradle nice???
			File tmpMetadata = project.getLayout().getBuildDirectory().file("tmp/jarjar-metadata/" + mod.modid)
				.get().getAsFile();
			TaskProvider<?> writeMetadataToFileTask = project.getTasks().register(mod.depJarNamedJarjarred.getName() + "_manifest", DefaultTask.class, write -> {
				write.getOutputs().file(tmpMetadata);
				write.doFirst(__ -> {
					try {
						Files.createDirectories(tmpMetadata.toPath().getParent());
						Files.writeString(tmpMetadata.toPath(), metadataJson);
					} catch (IOException e) {
						throw new RuntimeException("poot", e);
					}
				});
			});
			
			//5. yeah
			mod.depJarNamedJarjarred.configure(it -> {
				for(MetadataEntry dep : thingsToJar) {
					it.dependsOn(dep.mod.depJarNamedJarjarred);
					it.from(dep.mod.depJarNamedJarjarred, aa -> aa.into("META-INF/jarjar/"));
				}
				
				it.dependsOn(writeMetadataToFileTask);
				it.from(tmpMetadata, aa -> aa.into("META-INF/jarjar/").rename(".*", "metadata.json"));
			});
		}
	}
	
	public record MetadataEntry(LoaderMod mod, boolean obfuscated) implements Comparable<MetadataEntry>{
		public static MetadataEntry obfuscated(LoaderMod mod) {
			return new MetadataEntry(mod, true);
		}
		public static MetadataEntry deobfuscated(LoaderMod mod) {
			return new MetadataEntry(mod, false);
		}
		
		@Override
		public int compareTo(@NotNull NeoishJarJar.MetadataEntry other) {
			return this.mod.modid.compareTo(other.mod.modid);
		}
		
		public JsonObject toJson(Object projectVersion) {
			JsonObject j = new JsonObject();
			
			JsonObject identifier = new JsonObject();
			identifier.addProperty("group", "agency.highlysuspect");
			identifier.addProperty("artifact", mod.modid);
			j.add("identifier", identifier);
			
			JsonObject version = new JsonObject();
			String v = projectVersion.toString();
			version.addProperty("artifactVersion", v);
			version.addProperty("range", "[" + v + ",)"); //this version or newer
			j.add("version", version);
			
			//TODO check that this is the right property
			j.addProperty("path", "META-INF/jarjar/" + mod.depJarNamedJarjarred.flatMap(AbstractArchiveTask::getArchiveFileName).get());
			j.addProperty("isObfuscated", obfuscated);
			
			return j;
		}
	}
	
}

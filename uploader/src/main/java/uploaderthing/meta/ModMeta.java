package uploaderthing.meta;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ModMeta {
	public Loader loader;
	public String modid;
	public String name;
	public String version;
	public String minecraftVersion;
	
	@Override
	public String toString() {
		return "ModMeta{loader=%s, modid='%s', name='%s', version='%s', minecraftVersion='%s'}".formatted(loader, modid, name, version, minecraftVersion);
	}
	
	public ModMeta(Path mod) throws Exception {
		//guess initial values from the filename
		//TODO: if i set a proper minecraft dep in forgeish metadata, i won't need this, really...
		String[] filenameSplit = mod.getFileName().toString().split("-");
		modid = filenameSplit[0];
		minecraftVersion = filenameSplit[1];
		loader = Loader.from(filenameSplit[2]);
		version = filenameSplit[3].replace(".jar", "");
		
		try(ZipFile z = new ZipFile(mod.toFile())) {
			ZipEntry fmj = z.getEntry("fabric.mod.json");
			if(fmj != null) {
				loader = Loader.FABRIC;
				readFmj(z.getInputStream(fmj));
				return;
			}
			
			ZipEntry forgeToml = z.getEntry("META-INF/mods.toml");
			if(forgeToml != null) {
				loader = Loader.FORGE;
				readForgeish(z.getInputStream(forgeToml));
				return;
			}
			
			ZipEntry neoToml = z.getEntry("META-INF/neoforge.mods.toml");
			if(neoToml != null) {
				loader = Loader.NEOFORGE;
				readForgeish(z.getInputStream(neoToml));
			}
		}
	}
	
	private void readFmj(InputStream fmj) throws Exception {
		try(InputStreamReader isr = new InputStreamReader(fmj)) {
			JsonObject json = new Gson().fromJson(isr, JsonObject.class);
			
			loader = Loader.FABRIC;
			
			modid = Objects.requireNonNull(json.get("id").getAsString());
			name = Objects.requireNonNull(json.get("name").getAsString());
			version = Objects.requireNonNull(json.get("version").getAsString());
			
			JsonObject deps = json.get("depends").getAsJsonObject();
			//TODO: jankily filter out the greater-than-or-equal-to signs
			// honestly i should just use not use these and fix a single minecraft version
			minecraftVersion = deps.get("minecraft").getAsString().replaceAll("[^0-9.]", "");
		}
	}
	
	private void readForgeish(InputStream toml) throws Exception {
		try(Scanner scanner = new Scanner(new InputStreamReader(toml))) {
			//Get ready for the world's WORST toml parser!!!
			//TODO use a real toml parser if i ever clean this up for wider consumption
			
			int currentHeader = 0;
			final int MODS = 1, DEPS = 2;
			String currentDep = null;
			
			while(scanner.hasNextLine()) {
				String line = scanner.nextLine().trim();
				if(line.isEmpty()) continue;
				
				if(line.startsWith("[[mods")) {
					currentHeader = MODS;
					continue;
				} else if(line.startsWith("[[dependencies.")) {
					currentHeader = DEPS;
					continue;
				} else if(line.startsWith("[[")) {
					currentHeader = 0; //unknown subhead
					continue;
				}
				
				if(currentHeader == MODS) {
					String[] kv = tomlKv(line);
					if(kv == null) continue;
					
					if("modId".equals(kv[0])) modid = kv[1];
					if("version".equals(kv[0])) version = kv[1];
					if("displayName".equals(kv[0])) name = kv[1];
				}
				
				if(currentHeader == DEPS) {
					String[] kv = tomlKv(line);
					if(kv == null) continue;
					
					//yep this means the minecraft modid has to come before the version range
					//also TODO jankily stripping version range characters
					if("modId".equals(kv[0])) currentDep = kv[1];
					if("versionRange".equals(kv[0]) && "minecraft".equals(currentDep))
						minecraftVersion = kv[1].replaceAll("[^0-9.]", "");
					
				}
			}
		}
	}
	
	private String[] tomlKv(String line) {
		String[] split = line.split("=", 2);
		if(split.length != 2) return null;
		
		String key = split[0].trim();
		String value = split[1].trim();
		
		//todo actually unescape the quotes
		if(value.startsWith("\"") && value.endsWith("\""))
			value = value.substring(1, value.length() - 1);
		
		split[0] = key;
		split[1] = value;
		return split;
	}
}

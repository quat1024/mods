package uploaderthing;

import uploaderthing.froge.CurseforgeUploadResponse;
import uploaderthing.froge.CursefrogApi;
import uploaderthing.froge.GameVersionsMap;
import uploaderthing.froge.PublishableCurseforgeProject;
import uploaderthing.meta.ModMeta;
import uploaderthing.meta.PublishableProject;
import uploaderthing.mods.Crowmap;
import uploaderthing.mods.ModderNameLib;
import uploaderthing.mods.Packages;
import uploaderthing.mods.RebindNarrator;
import uploaderthing.rinth.LatestModrinthVersionCache;
import uploaderthing.rinth.ModrinthApi;
import uploaderthing.rinth.ModrinthUploadResponse;
import uploaderthing.rinth.PublishableModrinthProject;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Uploader {
	static boolean yesToAll = false;
	
	public static void main(String[] args) throws Exception {
		String a = System.getenv("COLLECT_DIR");
		System.out.println("COLLECT_DIR: " + a);
		if(a == null) throw new IllegalStateException("no COLLECT_DIR variable");
		Path collectDir = Paths.get(a);
		
		a = System.getenv("SECRETS_TXT");
		System.out.println("SECRETS_TXT: " + a);
		if(a == null) throw new IllegalStateException("no SECRETS_TXT variable");
		Properties secrets = new Properties();
		secrets.load(new FileInputStream(a));
		
		ChangelogFragment changelog = new ChangelogFragment(collectDir.resolve("CHANGELOG.md"));
		
		//sign in to modrinth
//		ModrinthApi modrinth = new ModrinthApi(ModrinthApi.PRODUCTION, secrets);
//		modrinth.getOauthCode();
//		modrinth.getAccessToken();
//		modrinth.dumpUserData();
		ModrinthApi modrinth = null;
		
		//sign in to curseforge
		CursefrogApi froge = new CursefrogApi(secrets);
		GameVersionsMap curseWeird = froge.getGameVersions();
//		froge.getVersionTypes();
//		froge.getGameDependencies();
		
//		System.exit(0);

//		String latestFabricapi = modrinth.getLatestVersion(new FabricApi(), Loader.FABRIC, "1.21.1");
//		System.out.println("Latest version of fabric api is: " + latestFabricapi);
		
		LatestModrinthVersionCache modrinthVersionCache = new LatestModrinthVersionCache(modrinth);
		
		for(Path jar : readDirSorted(collectDir, "*.jar")) {
			ModMeta meta = new ModMeta(jar);
			if(meta.modid == null) continue;
			
			List<String> thisChangelog = changelog.getChangelog(meta);
			System.out.println("Changelog:\n----");
			for(String line : thisChangelog) System.out.println(line);
			System.out.println("----");
			
			PublishableProject publishable = getPublishableProject(meta.modid);
			if(publishable == null) continue;
			
			System.out.println("Scanned metadata: " + meta);
			
			char response = ask("Upload " + jar.getFileName() + "? (y/n)");
			if(response == 'q') break;
			else if(response == 'Y') yesToAll = true;
			else if(response != 'y') continue;
			
			UploadBundle up = new UploadBundle();
			up.meta = meta;
			up.changelog = thisChangelog;
			up.jar = jar;
			
			//upload modrinth project
			if(modrinth != null && publishable instanceof PublishableModrinthProject pmp) {
				System.out.println("Uploading to modrinth.");
				up.proj = pmp;
				ModrinthUploadResponse resp = modrinth.uploadProject(up, modrinthVersionCache);
				modrinthVersionCache.put(pmp, up.meta.loader, up.meta.minecraftVersion, resp);
			}
			
			if(froge != null && publishable instanceof PublishableCurseforgeProject pcf) {
				System.out.println("Uploading to curseforge.");
				up.proj = pcf;
				CurseforgeUploadResponse resp = froge.uploadProject(up, curseWeird);
				System.out.println("Uploaded: " + resp);
			}
		}
	}
	
	public static PublishableProject getPublishableProject(String modid) {
		//if(true) return new ModrinthStagingTestMod();
		
		//if i was smart this would be loaded from a json file or something
		return switch(modid) {
			case "modder_name_lib" -> ModderNameLib.INST;
			case "crowmap" -> Crowmap.INST;
			case "packages" -> Packages.INST;
			case "rebind_narrator" -> RebindNarrator.INST;
			default -> null;
		};
	}
	
	public static List<Path> readDirSorted(Path dir, String glob) throws IOException {
		List<Path> paths = new ArrayList<>();
		try(DirectoryStream<Path> asdf = Files.newDirectoryStream(dir, glob)) {
			for(Path p : asdf) paths.add(p);
		}
		paths.sort(Comparator.comparing(Path::getFileName));
		
		//actually i need to sort moddernamelib jars first, because modrinth has a funny dependencies api.
		//i need to upload it in order to get its version ID so that the other projects can depend on
		//that specific version of moddernamelib
		List<Path> betterPaths = new ArrayList<>(paths.size());
		for(Path p : paths) if(p.getFileName().toString().startsWith("modder_name_lib")) betterPaths.add(p);
		for(Path p : paths) if(!p.getFileName().toString().startsWith("modder_name_lib")) betterPaths.add(p);
		
		return betterPaths;
	}
	
	public static char ask(String question) {
		if(yesToAll) return 'y';
		
		while(true) {
			System.out.print(question);
			String response = new Scanner(System.in).nextLine().trim();
			if(response.isEmpty()) continue;
			return response.charAt(0);
		}
	}
}

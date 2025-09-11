import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Uploader {
	public static void main(String[] args) throws Exception {
		for(String arg : args) {
			System.out.println("GOT ARG: " + arg);
		}
		
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
		ModrinthApi modrinth = new ModrinthApi(secrets);
		modrinth.getOauthCode();
		modrinth.getAccessToken();
		modrinth.dumpUserData();
		
		for(Path jar : readDirSorted(collectDir, "*.jar")) {
			char response = ask("Upload " + jar.getFileName() + "? (y/n) ");
			if(response == 'q') break;
			if(response != 'y') continue;
			
			//jankily parse everything from the filename
			String[] filenameSplit = jar.getFileName().toString().split("-");
			String modid = filenameSplit[0];
			String minecraftVersion = filenameSplit[1];
			String loader = filenameSplit[2];
			String version = filenameSplit[3].replace(".jar", "");
			
			System.out.println(modid);
			System.out.println(minecraftVersion);
			System.out.println(loader);
			System.out.println(version);
		}
	}
	
	public static List<Path> readDirSorted(Path dir, String glob) throws IOException {
		List<Path> paths = new ArrayList<>();
		try(DirectoryStream<Path> asdf = Files.newDirectoryStream(dir, glob)) {
			for(Path p : asdf) paths.add(p);
		}
		paths.sort(Comparator.comparing(Path::getFileName));
		return paths;
	}
	
	public static char ask(String question) {
		while(true) {
			System.out.print(question);
			String response = new Scanner(System.in).nextLine().trim().toLowerCase(Locale.ROOT);
			if(response.isEmpty()) continue;
			return response.charAt(0);
		}
	}
}

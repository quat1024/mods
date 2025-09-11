import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ChangelogFragment {
	private final List<String> unreleasedLines;
	
	public ChangelogFragment(Path changelog) throws Exception {
		try(Stream<String> lines = Files.lines(changelog)) {
			//find the "# Unreleased" header, and collect all the lines until the next h1
			//which would be all the lines corresponding to the unreleased portion of the changelog
			unreleasedLines = lines.dropWhile(line -> !line.startsWith("# Unreleased"))
				.skip(1)
				.takeWhile(line -> !line.startsWith("# v"))
				.toList();
		}
	}
	
	//asterisk, minecraft version number in parenthesis, and space
//	final Pattern VERSION_TAG = Pattern.compile("\\* \\((1\\.[0-9.]+)\\) ");
	
	public List<String> getChangelog(String modName, String minecraftVersion, String modVersion) {
		List<String> log = new ArrayList<>();
		
		log.add("# " + modName + " v" + modVersion + " for Minecraft " + minecraftVersion);
		log.add("");
		
		String currentHeader = null;
		for(String line : unreleasedLines) {
			//h2's
			if(line.startsWith("##")) {
				currentHeader = line.substring(2).trim();
				continue;
			}
			
			//not in an applicable section -> skip line
			if(currentHeader != null && !currentHeader.equalsIgnoreCase(modName))
				continue;
			
//			//contains a version tag for a different version -> skip line
//			Matcher tagFinder = VERSION_TAG.matcher(line);
//			if(tagFinder.find()) {
//				String foundTag = tagFinder.group(1);
//				if(!minecraftVersion.equals(foundTag))
//					continue;
//
//				//strip the version tag
//				line = tagFinder.replaceAll("");
//			}
			
			//prevent leading/doubled blank-lines
			if(line.isEmpty() && (log.isEmpty() || log.getLast().isEmpty())) continue;
			
			log.add(line);
		}
		
		return log;
	}
}

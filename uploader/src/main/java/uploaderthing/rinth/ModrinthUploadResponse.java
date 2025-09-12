package uploaderthing.rinth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModrinthUploadResponse {
	List<String> game_versions;
	List<String> loaders;
	String id;
	String project_id;
	String author_id;
	boolean featured;
	String name;
	String version_number;
	String changelog;
	String date_published;
	int downloads;
	String version_type;
	String status;
	
	public static class UploadedFile {
		Map<String, String> hashes = new HashMap<>();
		String url;
		String filename;
		boolean primary;
		int size;
	}
}

package uploaderthing.rinth;

import uploaderthing.meta.Loader;

import java.util.HashMap;
import java.util.Map;

//oh boy
public class LatestModrinthVersionCache {
	public LatestModrinthVersionCache(ModrinthApi api) {
		this.api = api;
	}
	
	record Key(ModrinthProject proj, Loader loader, String minecraftVersion) {}
	
	private Map<Key, String> keysToVersionIds = new HashMap<>();
	private final ModrinthApi api;
	
	public String getOrFetchLatestVersion(ModrinthProject proj, Loader loader, String minecraftVersion) throws Exception {
		Key k = new Key(proj, loader, minecraftVersion);
		String v = keysToVersionIds.get(k);
		if(v != null) return v;
		
		v = api.getLatestVersion(proj, loader, minecraftVersion);
		keysToVersionIds.put(k, v);
		return v;
	}
	
	public void put(ModrinthProject proj, Loader loader, String minecraftVersion, ModrinthUploadResponse up) {
		keysToVersionIds.put(new Key(proj, loader, minecraftVersion), up.id);
	}
}

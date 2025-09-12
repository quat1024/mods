package uploaderthing.froge;

import java.util.Map;
import java.util.TreeMap;

public class GameVersionsMap {
	private final Map<String, GameVersions> versionsByName = new TreeMap<>();
	
	public GameVersionsMap(Iterable<GameVersions> apiSludge) {
		for(GameVersions y : apiSludge) {
			// https://github.com/VazkiiMods/Botania/blob/507b2a81978c2eeca97678456a359f9144f1fad7/scripts/upload_releases.sh#L121-L124
			if(y.gameVersionTypeID == 1 || y.gameVersionTypeID == 615) continue;
			versionsByName.put(y.name, y);
		}
	}
	
	public GameVersions get(String name) {
		return versionsByName.get(name);
	}
	
	@Override
	public String toString() {
		return versionsByName.toString();
	}
}

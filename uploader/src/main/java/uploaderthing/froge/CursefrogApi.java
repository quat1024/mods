package uploaderthing.froge;

import com.github.mizosoft.methanol.MediaType;
import com.github.mizosoft.methanol.Methanol;
import com.github.mizosoft.methanol.MultipartBodyPublisher;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import uploaderthing.UploadBundle;
import uploaderthing.meta.Loader;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Properties;

// https://support.curseforge.com/en/support/solutions/articles/9000197321-curseforge-upload-api#Project-Upload-File-API
public class CursefrogApi {
	private final String apiKey;
	
	public static final String USER_AGENT = "quat's bulk uploader doohicky (github.com/quat1024/mods)";
	public static final String HOST = "https://minecraft.curseforge.com/api";
	
	public CursefrogApi(Properties secrets) {
		apiKey = secrets.getProperty("curseforge_api_key");
	}
	
	private Methanol.Builder newRequester() {
		return Methanol.newBuilder().userAgent(USER_AGENT);
	}
	
	private HttpRequest.Builder mkTokenedRequest(String uri) {
		if(apiKey == null) throw new IllegalStateException("no curseforge api key");
		
		return HttpRequest.newBuilder()
			.uri(URI.create(uri))
			.header("User-Agent", USER_AGENT)
			.header("X-Api-Token", apiKey);
	}
	
	public GameVersionsMap getGameVersions() throws Exception {
		try(Methanol c = newRequester().build()) {
			HttpRequest req = mkTokenedRequest(HOST + "/game/versions").build();
			HttpResponse<String> res = c.send(req, HttpResponse.BodyHandlers.ofString());
			if(res.statusCode() / 100 != 2) throw new IllegalStateException("bad response getting game versions " + res + ": " + res.body());
			String body = res.body();
			
			List<GameVersions> sludge = new Gson().fromJson(body, new TypeToken<List<GameVersions>>() {});
			GameVersionsMap result = new GameVersionsMap(sludge);
			System.out.println(result);
			return result;
		}
	}
	
	//totally undocumented, seems to exist in cursegradle
//	public void getVersionTypes() throws Exception {
//		try(Methanol c = newRequester().build()) {
//			HttpRequest req = mkTokenedRequest(HOST + "/game/version-types").build();
//			HttpResponse<String> res = c.send(req, HttpResponse.BodyHandlers.ofString());
//			if(res.statusCode() / 100 != 2) throw new IllegalStateException("bad response getting game versions " + res + ": " + res.body());
//			String body = res.body();
//			System.out.println(body);
//		}
//	}
	
	//not a real endpoint???
//	public List<GameDependencies> getGameDependencies() throws Exception {
//		try(Methanol c = newRequester().build()) {
//			HttpRequest req = mkTokenedRequest(HOST + "/game/dependencies").build();
//			HttpResponse<String> res = c.send(req, HttpResponse.BodyHandlers.ofString());
//			if(res.statusCode() / 100 != 2) throw new IllegalStateException("bad response getting game dependencies " + res + ": " + res.body());
//			String body = res.body();
//			System.out.println(body);
//			return new Gson().fromJson(body, new TypeToken<List<GameDependencies>>() {});
//		}
//	}
	
	public CurseforgeUploadResponse uploadProject(UploadBundle up, GameVersionsMap curseIsWeird) throws Exception {
		PublishableCurseforgeProject cf = (PublishableCurseforgeProject) up.proj;
		JsonObject j = new JsonObject();
		
		j.addProperty("changelog", String.join("\n", up.changelog));
		j.addProperty("changelogType", "markdown");
		
		//curse is so WEIRD dude
		JsonArray gameVersions = new JsonArray();
		gameVersions.add(curseIsWeird.get(up.meta.minecraftVersion).id);
		gameVersions.add(curseIsWeird.get(up.meta.loader.toCurseforgeGameVersionName()).id);
		if("1.20.1".equals(up.meta.minecraftVersion) && up.meta.loader == Loader.FORGE) {
			gameVersions.add(curseIsWeird.get(Loader.NEOFORGE.toCurseforgeGameVersionName()).id);
		}
		j.add("gameVersions", gameVersions);
		
		j.addProperty("releaseType", "release");
		
		JsonArray relationProjects = new JsonArray();
		for(CurseforgeProject dep : cf.curseforgeEmbeddedDeps(up.meta.loader, up.meta.minecraftVersion)) {
			JsonObject q = new JsonObject();
			q.addProperty("slug", dep.curseforgeSlug());
			q.addProperty("projectID", Integer.valueOf(dep.curseforgeProjectId()));
			q.addProperty("type", "embeddedLibrary");
			relationProjects.add(q);
		}
		//i love to copy paste
		for(CurseforgeProject dep : cf.curseforgeRequiredDeps(up.meta.loader, up.meta.minecraftVersion)) {
			JsonObject q = new JsonObject();
			q.addProperty("slug", dep.curseforgeSlug());
			q.addProperty("projectID", Integer.valueOf(dep.curseforgeProjectId()));
			q.addProperty("type", "requiredDependency");
			relationProjects.add(q);
		}
		JsonObject asdf = new JsonObject();
		asdf.add("projects", relationProjects);
		if(!relationProjects.isEmpty()) //if it exists it must not be empty
			j.add("relations", asdf);
		
		try(Methanol c = newRequester().build()) {
			HttpRequest.Builder reqb = mkTokenedRequest(HOST + "/projects/" + cf.curseforgeProjectId() + "/upload-file");
			
			MultipartBodyPublisher pub = MultipartBodyPublisher.newBuilder()
				.textPart("metadata", new Gson().toJson(j))
				.filePart("file", up.jar, MediaType.APPLICATION_OCTET_STREAM)
				.build();
			
			HttpRequest req = reqb.POST(pub).build();
			HttpResponse<String> res = c.send(req, HttpResponse.BodyHandlers.ofString());
			if(res.statusCode() / 100 != 2) throw new IllegalStateException("bad response when uploading file " + res + ": " + res.body());
			
			String body = res.body();
			System.out.println(res.body());
			return new Gson().fromJson(body, CurseforgeUploadResponse.class);
		}
	}
}

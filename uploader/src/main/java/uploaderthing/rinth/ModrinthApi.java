package uploaderthing.rinth;

import com.github.mizosoft.methanol.MediaType;
import com.github.mizosoft.methanol.Methanol;
import com.github.mizosoft.methanol.MultipartBodyPublisher;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.Fields;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import uploaderthing.UploadBundle;
import uploaderthing.meta.Loader;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ModrinthApi {
	final Host host;
	public static final Host STAGING = new Staging();
	public static final Host PRODUCTION = new Prod();
	
	String clientId, clientSecret, redirectUri;
	String code;
	String accessToken;
	
	public static final String USER_AGENT = "quat's bulk uploader doohicky (github.com/quat1024/mods)";
	
	public ModrinthApi(Host host, Properties secrets) {
		this.host = host;
		host.readSecrets(this, secrets);
		
		if(this.clientId == null) throw new NullPointerException("Missing clientId");
		if(this.clientSecret == null) throw new NullPointerException("Missing clientSecret");
		
		this.redirectUri = "http://localhost:4200/";
	}
	
	public void getOauthCode() throws Exception {
		String scope = String.join(" ", "USER_READ_EMAIL", "USER_READ", "VERSION_CREATE");
		String salt = UUID.randomUUID().toString();
		
		CompletableFuture<String> authCodeOauthBlocker = new CompletableFuture<>();
		
		// see https://docs.modrinth.com/guide/oauth/ for oauth flow
		
		//first start the server we'll use to catch the oauth request
		
		//start a jetty server on localhost 4200
		//https://jetty.org/docs/jetty/12.1/programming-guide/server/http.html
		Server jettyServer = new Server(new QueuedThreadPool());
		ServerConnector conn = new ServerConnector(jettyServer);
		conn.setPort(4200);
		jettyServer.addConnector(conn); //accept clients on port 4200
		jettyServer.setHandler(new Handler.Abstract() {
			@Override
			public boolean handle(Request request, Response response, Callback callback) throws Exception {
				System.out.println("Got request " + request);
				
				//read query parameters into a map
				Map<String, String> parms = new HashMap<>();
				for(Fields.Field field : Request.extractQueryParameters(request)) {
					String old = parms.put(field.getName(), field.getValue());
					if(old != null) throw new IllegalStateException("dupe key " + field.getName());
				}
				
				//modrinth reported an error
				if(parms.containsKey("error"))
					throw new IllegalStateException(parms.get("error") + " -- " + parms.get("error_description"));
				
				//csrf
				if(!salt.equals(parms.get("state"))) throw new IllegalStateException("wrong salt");
				
				//looks good, get the code
				String code = parms.get("code");
				if(code == null) throw new IllegalStateException("didn't get code");
				
				authCodeOauthBlocker.complete(code);
				
				String page = "<h1>Your did it</h1>";
				response.write(true, StandardCharsets.UTF_8.encode(page), callback);
				callback.succeeded();
				return true;
			}
		});
		
		jettyServer.start();
		
		// build url to oauth to modrinth
		String base = host.authorizationPage();
		base = base + "?response_type=code";
		base = base + "&client_id=" + enc(clientId);
		base = base + "&scope=" + enc(scope);
		base = base + "&state=" + enc(salt);
		base = base + "&redirect_uri=" + enc(redirectUri);
		
		System.out.println("Click on this to authorize with modrinth: ");
		System.out.println();
		System.out.println("   " + base);
		System.out.println();
		
		//wait for oauth to complete
		String code = Objects.requireNonNull(authCodeOauthBlocker.get());
		System.out.println("Got auth code");
		
		//graceful shutdown to give server time to respond
		Thread.sleep(500);
		jettyServer.stop();
		
		this.code = code;
	}
	
	private Methanol.Builder newRequester() {
		return Methanol.newBuilder().userAgent(USER_AGENT);
	}
	
	private HttpRequest.Builder mkRequest(String uri) {
		return HttpRequest.newBuilder()
			.uri(URI.create(uri))
			.header("User-Agent", USER_AGENT);
	}
	
	private HttpRequest.Builder mkTokenedRequest(String uri) {
		if(accessToken == null) throw new IllegalStateException("Requires accessToken");
		
		return mkRequest(uri).header("Authorization", "Bearer " + accessToken);
	}
	
	public void getAccessToken() throws Exception {
		if(code == null) throw new IllegalStateException("oauth code not obtained");
		
		try(Methanol c = newRequester().build()) {
			Map<String, String> form = new LinkedHashMap<>();
			form.put("code", code);
			form.put("client_id", clientId);
			form.put("redirect_uri", redirectUri);
			form.put("grant_type", "authorization_code");
			
			//encode the form (stolen from stackoverflow)
			String encodedForm = form.entrySet().stream()
				.map(e -> enc(e.getKey()) + "=" + enc(e.getValue()))
				.collect(Collectors.joining("&"));
			
			//shoot off the request
			HttpRequest req = mkRequest(host.tokenExchange())
				.header("Content-Type", "application/x-www-form-urlencoded")
				.header("Authorization", clientSecret)
				.POST(HttpRequest.BodyPublishers.ofString(encodedForm, StandardCharsets.UTF_8))
				.build();
			HttpResponse<String> res = c.send(req, HttpResponse.BodyHandlers.ofString());
			if(res.statusCode() / 100 != 2) throw new IllegalStateException("bad response when exchanging token; " + res);
			JsonObject json = new Gson().fromJson(res.body(), JsonObject.class);
			this.accessToken = json.get("access_token").getAsString();
			System.out.println("Got token");
		}
	}
	
	public void dumpUserData() throws Exception {
//		try(Methanol c = newRequester().build()) {
//			HttpRequest req = mkTokenedRequest(host.apiRoot() + "/user").GET().build();
//			HttpResponse<String> res = c.send(req, HttpResponse.BodyHandlers.ofString());
//			if(res.statusCode() / 100 != 2) throw new IllegalStateException("bad response when getting user data " + res);
//			System.out.println(res.body());
//		}
	}
	
	public ModrinthUploadResponse uploadProject(UploadBundle up, LatestModrinthVersionCache latestVersionCache) throws Exception {
		String filename = up.jar.getFileName().toString();
		PublishableModrinthProject modrinthProj = (PublishableModrinthProject) up.proj;
		
		JsonObject j = new JsonObject();
		
		j.addProperty("project_id", modrinthProj.modrinthProjectId());
		
		j.addProperty("name", "v" + up.meta.version);
		j.addProperty("version_number", up.meta.version + "-" + up.meta.loader + "-" + up.meta.minecraftVersion);
		j.addProperty("changelog", String.join("\n", up.changelog));
		
		//todo: properly list dependencies
		// modrinth requires you to dep on a specific version of everything! which is odd to me
		JsonArray dependencies = new JsonArray();
		for(ModrinthProject requiredDep : modrinthProj.modrinthRequiredDeps(up.meta.loader, up.meta.minecraftVersion)) {
			JsonObject dep = new JsonObject();
			dep.addProperty("dependency_type", "required");
			dep.addProperty("project_id", requiredDep.modrinthProjectId());
			dep.addProperty("version_id", latestVersionCache.getOrFetchLatestVersion(requiredDep, up.meta.loader, up.meta.minecraftVersion));
			dep.addProperty("file_name", filename);
			dependencies.add(dep);
		}
		//go go gadget copypaste coding (i am really slapping this together)
		for(ModrinthProject embeddedDep : modrinthProj.modrinthEmbeddedDeps(up.meta.loader, up.meta.minecraftVersion)) {
			JsonObject dep = new JsonObject();
			dep.addProperty("dependency_type", "embedded");
			dep.addProperty("project_id", embeddedDep.modrinthProjectId());
			dep.addProperty("version_id", latestVersionCache.getOrFetchLatestVersion(embeddedDep, up.meta.loader, up.meta.minecraftVersion));
			dep.addProperty("file_name", filename);
			dependencies.add(dep);
		}
		j.add("dependencies", new JsonArray());
		
		JsonArray gameVersions = new JsonArray();
		gameVersions.add(up.meta.minecraftVersion);
		j.add("game_versions", gameVersions);
		
		j.addProperty("version_type", "release");
		
		JsonArray loaders = new JsonArray();
		loaders.add(up.meta.loader.toString());
		//neoforge compat hax for forge 1.20.1
		if("1.20.1".equals(up.meta.minecraftVersion) && up.meta.loader == Loader.FORGE) {
			loaders.add(Loader.NEOFORGE.toString());
		}
		j.add("loaders", loaders);
		
		//yes it's required
		j.addProperty("featured", false);
		
		//oddball filename multipart form-data stuff
		j.addProperty("primary_file", filename);
		JsonArray fileParts = new JsonArray();
		fileParts.add(filename);
		j.add("file_parts", fileParts);
		
		//System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(j));
		
		try(Methanol c = newRequester().build()) {
			HttpRequest.Builder reqb = mkTokenedRequest(host.apiRoot() + "/version");
			
			MultipartBodyPublisher pub = MultipartBodyPublisher.newBuilder()
				.textPart("data", new Gson().toJson(j))
				.filePart(filename, up.jar, MediaType.APPLICATION_OCTET_STREAM)
				.build();
			
			HttpRequest req = reqb.POST(pub).build();
			HttpResponse<String> res = c.send(req, HttpResponse.BodyHandlers.ofString());
			if(res.statusCode() / 100 != 2) throw new IllegalStateException("bad response when uploading file " + res + ": " + res.body());
			
			String body = res.body();
			return new Gson().fromJson(body, ModrinthUploadResponse.class);
		}
	}
	
	public String getLatestVersion(ModrinthProject proj, Loader loader, String minecraftVersion) throws Exception {
		try(Methanol c = newRequester().build()) {
			String url = host.apiRoot() + "/project/" + proj.modrinthProjectId() + "/version";
			url = url + "?loaders=" + enc("[\"" + loader.toString() + "\"]"); //a json array
			url = url + "&game_versions=" + enc("[\"" + minecraftVersion + "\"]");
			
			//done with a token to hopefully catch non-approved versions too
			HttpRequest req = mkTokenedRequest(url).GET().build();
			HttpResponse<String> res = c.send(req, HttpResponse.BodyHandlers.ofString());
			if(res.statusCode() / 100 != 2) throw new IllegalStateException("bad response when getting latest version " + res + ": " + res.body());
			
			String body = res.body();
			List<ModrinthVersionQueryResponse> queryResponses = new Gson().fromJson(body, new TypeToken<List<ModrinthVersionQueryResponse>>() {});
			
			if(queryResponses.isEmpty()) {
				throw new IllegalStateException("found 0 versions for " + proj.modrinthProjectId() + " " + loader + " " + minecraftVersion);
			}
			queryResponses = new ArrayList<>(queryResponses);
			queryResponses.sort(Comparator.comparing(x -> x.date_published));
			
//			System.out.println("got query responses: " + queryResponses);
			ModrinthVersionQueryResponse last = queryResponses.getLast();
			System.out.println("!! latest version of " + proj.getClass().getSimpleName() + " for " + loader + " " + minecraftVersion + " is " + last.version_number + "(" + last.id + ")");
			
			return last.id;
		}
	}
	
	private static String enc(String a) {
		return URLEncoder.encode(a, StandardCharsets.UTF_8);
	}
	
	public interface Host {
		void readSecrets(ModrinthApi api, Properties secrets);
		String apiRoot();
		String tokenExchange();
		String authorizationPage();
	}
	
	static class Staging implements Host {
		@Override
		public void readSecrets(ModrinthApi api, Properties secrets) {
			api.clientId = secrets.getProperty("modrinth_staging_uploader_client_id");
			api.clientSecret = secrets.getProperty("modrinth_staging_uploader_client_secret");
		}
		
		@Override
		public String apiRoot() {
			return "https://staging-api.modrinth.com/v2";
		}
		
		@Override
		public String tokenExchange() {
			return "https://staging-api.modrinth.com/_internal/oauth/token";
		}
		
		@Override
		public String authorizationPage() {
			//signing on on staging.modrinth.com seems to redirect you here,
			//and then i'm not signed in on staging.modrinth.com, just this one.
			//not sure if that hash will change
			return "https://32458fd4.code-efh.pages.dev/auth/authorize";
		}
	}
	
	static class Prod implements Host {
		@Override
		public void readSecrets(ModrinthApi api, Properties secrets) {
			api.clientId = secrets.getProperty("modrinth_uploader_client_id");
			api.clientSecret = secrets.getProperty("modrinth_uploader_client_secret");
		}
		
		@Override
		public String apiRoot() {
			return "https://api.modrinth.com/v2";
		}
		
		@Override
		public String tokenExchange() {
			return "https://api.modrinth.com/_internal/oauth/token";
		}
		
		@Override
		public String authorizationPage() {
			return "https://modrinth.com/auth/authorize";
		}
	}
}

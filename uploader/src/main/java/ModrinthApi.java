import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.Fields;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ModrinthApi {
	String clientId, clientSecret, redirectUri;
	String code;
	String accessToken;
	
	public static final String ENDPOINT = "https://api.modrinth.com/v2/";
	
	public static final String USER_AGENT = "quat's bulk uploader doohicky (github.com/quat1024/mods)";
	
	public ModrinthApi(Properties secrets) {
		this.clientId = secrets.getProperty("modrinth_uploader_client_id");
		this.clientSecret = secrets.getProperty("modrinth_uploader_client_secret");
		this.redirectUri = "http://localhost:4200/";
	}
	
	public void getOauthCode() throws Exception {
		String scope = String.join(" ", "USER_READ_EMAIL", "USER_READ");
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
		String base = "https://modrinth.com/auth/authorize";
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
		
		String tokenExchangeEndpoint = "https://api.modrinth.com/_internal/oauth/token";
		
		try(HttpClient c = HttpClient.newHttpClient()) {
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
			HttpRequest req = mkRequest(tokenExchangeEndpoint)
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
		try(HttpClient c = HttpClient.newHttpClient()) {
			HttpRequest req = mkTokenedRequest(ENDPOINT + "user").GET().build();
			HttpResponse<String> res = c.send(req, HttpResponse.BodyHandlers.ofString());
			if(res.statusCode() / 100 != 2) throw new IllegalStateException("bad response when getting user data " + res);
			System.out.println(res.body());
		}
	}
	
	private static String enc(String a) {
		return URLEncoder.encode(a, StandardCharsets.UTF_8);
	}
}

package agency.highlysuspect.quatlib.any.config.hdc;

import agency.highlysuspect.quatlib.any.config.ConfigOpt;
import agency.highlysuspect.quatlib.any.config.ConfigSection;
import agency.highlysuspect.quatlib.any.config.MatchedUnparsedConfig;
import agency.highlysuspect.quatlib.any.config.MutableMapConfig;
import agency.highlysuspect.quatlib.any.config.ValidatedConfig;
import agency.highlysuspect.quatlib.any.config.sn.Sn;
import agency.highlysuspect.quatlib.any.config.sn.SnParser;
import agency.highlysuspect.quatlib.any.failure.ContextChain;
import agency.highlysuspect.quatlib.any.failure.ReportedException;
import agency.highlysuspect.quatlib.any.util.LogFacade;
import agency.highlysuspect.quatlib.any.util.SharedConfigFileWatcher;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class HalfDecentConfigFile extends MutableMapConfig {
	public HalfDecentConfigFile(Path path, ConfigSection schema, LogFacade log, Executor background, ContextChain ctx) {
		this.path = path;
		this.schema = schema;
		this.log = log;
		this.background = background;
		this.ctx = ctx;
	}
	
	private final Path path;
	private final ConfigSection schema;
	private final LogFacade log;
	private final Executor background;
	private final ContextChain ctx;
	
	//filewatcher debouncing. we can get multiple events from the OS.
	long filewatcherDebounce = 0;
	protected static final int FILEWATCHER_DEBOUNCE_MS = 500;
	
	//ignore filewatcher pings that happen after we manually save the file
	//n.b.: if the game is lagging hard or if there's lots of stuff scheduled on
	//the background executor, this can take a very long time.
	long lastIngameSave = 0;
	protected static final int SAVE_TIMEOUT_MS = 10000;
	
	@Override
	public void modify(Consumer<Handle> modifier) {
		//apply all of the changes, and then schedule a save (once!)
		super.modify(modifier);
		saveLater2(ctx);
	}
	
	public void saveNow2(ContextChain ctx) {
		doSave2(state, ctx);
	}
	
	public void saveLater2(ContextChain ctx) {
		log.info("Scheduling save of config file {}", path);
		
		//make a clone that's hopefully safe to pass between threads
		Map<ConfigOpt<?>, Object> stateClone = new IdentityHashMap<>(state);
		background.execute(() -> doSave2(stateClone, ctx));
	}
	
	private void doSave2(Map<ConfigOpt<?>, Object> theState, ContextChain ctx) {
		log.info("Saving config file to {}", path);
		ctx = ctx.detail("While saving config file to " + path);
		
		//if this throws, it's probably my bug, throw a runtime exception
		String serializedConfig;
		try {
			serializedConfig = new HalfDecentConfigWriter().writeTopLevel(schema, new MutableMapConfig(theState));
		} catch (Exception e) {
			RuntimeException oops = new RuntimeException("Failed to serialize config! This is a bug!", e);
			ctx.cause(oops).sneakyReportError();
			throw oops;
		}
		
		//we're about to trigger the filewatcher by saving the file
		lastIngameSave = System.currentTimeMillis();
		
		//if this throws it's a filesystem error
		try {
			Files.writeString(path, serializedConfig, StandardCharsets.UTF_8);
			log.info("Saved successfully!");
		} catch (Exception e) {
			ctx.cause(e).detail("Failed to save config file!").sneakyReportError();
		}
	}
	
	public void load2(ContextChain ctx) {
		log.info("Loading config file {}", path);
		ctx = ctx.detail("While loading config file from " + path);
		
		if(Files.notExists(path)) {
			log.info("Config file doesn't exist. Writing a new one and loading default options");
			state = new IdentityHashMap<>();
			doSave2(state, ctx.detail("Config file did not exist, writing a new one"));
			return;
		}
		
		//read the file
		String read;
		try {
			read = Files.readString(path, StandardCharsets.UTF_8);
		} catch (Exception e) {
			RuntimeException r = new RuntimeException("Failed to read config file at " + path, e);
			ctx.cause(r).sneakyReportError();
			throw r;
		}
		
		//parse it into sn
		Sn<?> parsed;
		try {
			parsed = new SnParser(read).parseTopLevel();
			ReportedException.fake();
		} catch (ReportedException e) {
			return; //couldn't parse into sn, give up
		}
		
		//match it to config options
		MatchedUnparsedConfig matched = new MatchedUnparsedConfig(schema, parsed.view());
		
		//validate it
		ValidatedConfig validated = matched.parseAndValidate2(ctx);
		
		//all good, time to load it
		state = new IdentityHashMap<>(validated.toMap());
		log.info("Loaded {} options.", state.size());
		
		//schedule a saveback
		saveLater2(ctx.detail("Saveback after loading file"));
	}
	
	public void watchForChanges(ContextChain ctx) {
		ContextChain ctx2 = ctx.detail("While watching the file at " + path);
		
		SharedConfigFileWatcher.watch(path, () -> {
			//we're on a different thread now
			long lastFilewatcherDebounce = filewatcherDebounce;
			long now = System.currentTimeMillis();
			filewatcherDebounce = now;
			
			if(now - lastFilewatcherDebounce < FILEWATCHER_DEBOUNCE_MS) {
				//too spammy
				//log.info("Only been {}ms since last filewatcher ping, ignoring change to {}", now - lastFilewatcherDebounce, path.getFileName());
			} else if(now - lastIngameSave < SAVE_TIMEOUT_MS) {
				log.info("Only been {}ms since last in-game save, ignoring change to {}", now - lastIngameSave, path.getFileName());
			} else {
				//uhhhhmh hopefully this is safe to call off-thread?
				load2(ctx2);
			}
		});
	}
}

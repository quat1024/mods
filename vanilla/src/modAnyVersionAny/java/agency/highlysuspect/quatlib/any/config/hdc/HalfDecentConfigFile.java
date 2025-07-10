package agency.highlysuspect.quatlib.any.config.hdc;

import agency.highlysuspect.quatlib.any.config.ConfigOpt;
import agency.highlysuspect.quatlib.any.config.ConfigSection;
import agency.highlysuspect.quatlib.any.config.MatchedUnparsedConfig;
import agency.highlysuspect.quatlib.any.config.MutableMapConfig;
import agency.highlysuspect.quatlib.any.config.ValidatedConfig;
import agency.highlysuspect.quatlib.any.config.sn.Sn;
import agency.highlysuspect.quatlib.any.config.sn.SnParser;
import agency.highlysuspect.quatlib.any.failure.ContextChain;
import agency.highlysuspect.quatlib.any.failure.FailureBin;
import agency.highlysuspect.quatlib.any.failure.Report2;
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
	public HalfDecentConfigFile(Path path, ConfigSection schema, LogFacade log, Executor background) {
		this.path = path;
		this.schema = schema;
		this.log = log;
		this.background = background;
	}
	
	private final Path path;
	private final ConfigSection schema;
	private final LogFacade log;
	private final Executor background;
	
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
		saveLater();
	}
	
	public void saveNow() {
		doSave(state);
	}
	
	public void saveLater() {
		log.info("Scheduling save of config file {}", path);
		
		//make a clone that's hopefully safe to pass between threads
		Map<ConfigOpt<?>, Object> stateClone = new IdentityHashMap<>(state);
		background.execute(() -> doSave(stateClone));
	}
	
	private void doSave(Map<ConfigOpt<?>, Object> theState) {
		log.info("Saving config file to {}", path);
		//write it out
		String serializedConfig = new HalfDecentConfigWriter().writeTopLevel(schema, new MutableMapConfig(theState));
		
		//we're about to trigger the filewatcher by saving the file
		lastIngameSave = System.currentTimeMillis();
		
		//do it
		try {
			Files.writeString(path, serializedConfig, StandardCharsets.UTF_8);
			log.info("Saved successfully!");
		} catch (Exception e) {
			log.warn("Failed to save config file at " + path, e);
		}
	}
	
	public void load() {
		log.info("Loading config file {}", path);
		
		if(Files.notExists(path)) {
			log.info("Config file doesn't exist. Writing a new one and loading default options", path);
			state = new IdentityHashMap<>();
			doSave(state);
			return;
		}
		
		FailureBin failures = new FailureBin();
		Report2.Report2Formatter reporter = new Report2.LogFacadeReport2Formatter(log);
		ContextChain ctx = failures.detail("Config file at " + path);
		
		//read the file
		
		String read;
		try {
			read = Files.readString(path, StandardCharsets.UTF_8);
		} catch (Exception e) {
			ctx.detail("Failed to read file from " + path).addError(e);
			failures.reportWarnings(reporter);
			failures.reportErrors(reporter);
			return;
		}
		
		//parse the file
		Sn<?> parsed;
		try {
			parsed = new SnParser(read).parseTopLevel(ctx.detail("While parsing the file"));
		} catch (Report2 e) {
			failures.reportWarnings(reporter);
			failures.reportErrors(reporter);
			return;
		}
		
		//match
		MatchedUnparsedConfig matched = new MatchedUnparsedConfig(schema, parsed.view(), ctx.detail("While matching the file"));
		
		//validate
		ValidatedConfig validated = matched.parseAndValidate(ctx.detail("While validating the file"));
		
		//report errors, and if there aren't any, load the file
		failures.reportWarnings(reporter);
		failures.reportErrors(reporter);
		
		if(failures.noErrors()) {
			state = validated.toMap();
			log.info("Loaded successfully!");
		} else {
			log.warn("Not loading config file; there were errors. See above.");
		}
		
		//schedule a saveback (fixes the formatting, etc)
		saveLater();
	}
	
	public void watchForChanges() {
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
				load();
			}
		});
	}
}

package agency.highlysuspect.quatlib.any.config.hdc;

import agency.highlysuspect.quatlib.any.config.ConfigOpt;
import agency.highlysuspect.quatlib.any.config.ConfigSection;
import agency.highlysuspect.quatlib.any.config.MatchedUnparsedConfig;
import agency.highlysuspect.quatlib.any.config.MutableMapConfig;
import agency.highlysuspect.quatlib.any.config.sn.Sn;
import agency.highlysuspect.quatlib.any.config.sn.SnParser;
import agency.highlysuspect.quatlib.any.failure.Report;
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
	
	public void saveNow() throws Report {
		doSave(state);
	}
	
	public void saveLater() {
		log.info("Scheduling save of config file {}", path);
		
		//make a clone that's hopefully safe to pass between threads
		Map<ConfigOpt<?>, Object> stateClone = new IdentityHashMap<>(state);
		background.execute(() -> {
			try {
				doSave(stateClone);
			} catch (Report e) {
				e.logTo(log);
			}
		});
	}
	
	private void doSave(Map<ConfigOpt<?>, Object> theState) throws Report {
		try {
			log.info("Saving config file to {}", path);
			//write it out
			String serializedConfig = new HalfDecentConfigWriter().writeTopLevel(schema, new MutableMapConfig(theState));
			
			//we're about to trigger the filewatcher by saving the file
			lastIngameSave = System.currentTimeMillis();
			
			//do it
			Files.writeString(path, serializedConfig, StandardCharsets.UTF_8);
			log.info("Saved successfully!");
		} catch (Throwable e) {
			throw Report.modify(e, it -> it.addMessage("Failed to save config to " + path));
		}
	}
	
	public void load() throws Report {
		try {
			log.info("Loading config file {}", path);
			
			if(Files.notExists(path)) {
				log.info("Config file doesn't exist. Writing a new one and loading default options", path);
				state = new IdentityHashMap<>();
				doSave(state);
				return;
			}
			
			//parse the file
			Sn<?> parsed = new SnParser(Files.readString(path, StandardCharsets.UTF_8)).parseTopLevel();
			
			//match it to config options
			MatchedUnparsedConfig matched = new MatchedUnparsedConfig(schema, parsed.view());
			
			//validate it
			state = new IdentityHashMap<>(matched.parseAndValidate().toMap());
			log.info("Loaded successfully!");
			
			//schedule a saveback (fixes the formatting, etc)
			saveLater();
		} catch (Throwable e) {
			throw Report.modify(e, it -> it.addMessage("Failed to load config from " + path));
		}
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
				try {
					//uhhhhmh hopefully this is safe to call off-thread?
					load();
				} catch (Report e) {
					e.logTo(log);
				}
			}
		});
	}
}

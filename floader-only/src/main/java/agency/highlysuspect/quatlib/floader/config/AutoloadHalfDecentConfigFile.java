package agency.highlysuspect.quatlib.floader.config;

import agency.highlysuspect.quatlib.any.config.ConfigOpt;
import agency.highlysuspect.quatlib.any.config.ConfigSection;
import agency.highlysuspect.quatlib.any.config.MatchedUnparsedConfig;
import agency.highlysuspect.quatlib.any.config.MutableMapConfig;
import agency.highlysuspect.quatlib.any.config.sn.Sn;
import agency.highlysuspect.quatlib.any.config.sn.SnParser;
import agency.highlysuspect.quatlib.any.config.sn.SnView;
import agency.highlysuspect.quatlib.any.failure.ConsoleReportFormatter;
import agency.highlysuspect.quatlib.any.failure.Report;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class AutoloadHalfDecentConfigFile extends MutableMapConfig {
	public AutoloadHalfDecentConfigFile(Path path, ConfigSection schema, Consumer<String> log, Executor background) {
		this.path = path;
		this.schema = schema;
		this.log = log;
		this.background = background;
	}
	
	private final Path path;
	private final ConfigSection schema;
	private final Consumer<String> log;
	private final Executor background;
	
	//TODO actually implement the filewatcher lmao
	//filewatcher debouncing
	long filewatcherDebounce = 0;
	protected static final int DEBOUNCE_MS = 300;
	//ignore filewatcher changes that happen after we manually save the file
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
		log.accept("Scheduling save of config file " + path);
		
		//make a clone that's hopefully safe to pass between threads
		Map<ConfigOpt<?>, Object> stateClone = new IdentityHashMap<>(state);
		background.execute(() -> {
			try {
				doSave(stateClone);
			} catch (Report e) {
				//TODO, log the error *properly* to the logger
				new ConsoleReportFormatter().report(Report.modify(e, it -> it.addMessage("Failed to save config to " + path)));
			}
		});
	}
	
	private void doSave(Map<ConfigOpt<?>, Object> theState) throws Report {
		log.accept("Saving config file " + path);
		try {
			String serializedConfig = new HalfDecentConfigWriter().writeTopLevel(schema, new MutableMapConfig(theState));
			
			//we're about to trigger the filewatcher by saving the file
			//TODO actually put in the filewatcher
			filewatcherDebounce = System.currentTimeMillis();
			lastIngameSave = System.currentTimeMillis();
			
			Files.writeString(path, serializedConfig, StandardCharsets.UTF_8);
		} catch (Throwable e) {
			//TODO, log the error *properly* to the logger
			new ConsoleReportFormatter().report(Report.modify(e, it -> it.addMessage("Failed to save config to " + path)));
			return;
		}
		log.accept("Saved successfully!");
	}
	
	public void load() throws Report {
		try {
			if(Files.notExists(path)) {
				log.accept("Config file at " + path + " doesn't exist, using default options");
				state = new IdentityHashMap<>();
			} else {
				log.accept("Loading config file " + path);
				String snString = Files.readString(path, StandardCharsets.UTF_8);
				
				Sn<?> parsed = new SnParser(snString).parseTopLevel();
				SnView view = parsed.view();
				MatchedUnparsedConfig matched = new MatchedUnparsedConfig().match(schema, view);
				
				state = new IdentityHashMap<>(matched.parseAndValidate().toMap());
				log.accept("Loaded successfully");
			}
		} catch (Throwable e) {
			throw Report.modify(e, it -> it.addMessage("Failed to load config from " + path));
		}
	}
}

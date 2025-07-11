package agency.highlysuspect.quatlib.craftless.config.hdc;

import agency.highlysuspect.quatlib.craftless.config.ConfigOpt;
import agency.highlysuspect.quatlib.craftless.config.ConfigSection;
import agency.highlysuspect.quatlib.craftless.config.MatchedUnparsedConfig;
import agency.highlysuspect.quatlib.craftless.config.MutableMapConfig;
import agency.highlysuspect.quatlib.craftless.config.ValidatedConfig;
import agency.highlysuspect.quatlib.craftless.config.sn.Sn;
import agency.highlysuspect.quatlib.craftless.config.sn.SnParser;
import agency.highlysuspect.quatlib.craftless.failure.CtxChain;
import agency.highlysuspect.quatlib.craftless.failure.ReportedException;
import agency.highlysuspect.quatlib.craftless.util.LogFacade;
import agency.highlysuspect.quatlib.craftless.util.SharedConfigFileWatcher;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class HalfDecentConfigFile extends MutableMapConfig {
	public HalfDecentConfigFile(CtxChain ctx, ConfigSection schema, Path path, LogFacade log, Executor background) {
		this.ctx = ctx.detail("Config file at " + path);
		this.schema = schema;
		this.path = path;
		this.log = log;
		this.background = background;
	}
	
	private final CtxChain ctx;
	private final ConfigSection schema;
	private final Path path;
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
		CtxChain ctx2 = ctx.detail("Problem saving config file");
		
		//if this throws, it's probably my bug, throw a runtime exception
		String serializedConfig;
		try {
			serializedConfig = new HalfDecentConfigWriter().writeTopLevel(schema, new MutableMapConfig(theState));
		} catch (Exception e) {
			throw ctx2.cause(e).detail("Failed to serialize config! This is a bug!").uncheckedReportError();
		}
		
		//we're about to trigger the filewatcher by saving the file
		lastIngameSave = System.currentTimeMillis();
		
		//if this throws it's a filesystem error
		try {
			Files.writeString(path, serializedConfig, StandardCharsets.UTF_8);
			log.info("Saved successfully!");
		} catch (Exception e) {
			throw ctx2.cause(e).detail("Failed to write config file to disk!").uncheckedReportError();
		}
	}
	
	public void load() {
		log.info("Loading config file {}", path);
		CtxChain ctx2 = ctx.detail("Problem loading config file");
		
		if(Files.notExists(path)) {
			log.info("Config file doesn't exist. Writing a new one and loading default options");
			state = new IdentityHashMap<>();
			doSave(state);
			return;
		}
		
		//read the file
		String read;
		try {
			read = Files.readString(path, StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw ctx2.cause(e).detail("Failed to read config file at " + path).uncheckedReportError();
		}
		
		//parse, match, validate
		Sn<?> parsed;
		try {
			parsed = new SnParser(read).parseTopLevel(ctx2);
		} catch (ReportedException e) {
			log.warn("Aborting config load, unrecoverable parse error");
			return;
		}
		MatchedUnparsedConfig matched = new MatchedUnparsedConfig(schema, parsed.view(ctx2));
		ValidatedConfig validated = matched.parseAndValidate();
		
		state = new IdentityHashMap<>(validated.toMap());
		log.info("Loaded {} options.", state.size());
		
		//schedule a saveback
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
				log.info("Only been {}ms since last save, ignoring change to {}", now - lastIngameSave, path.getFileName());
			} else {
				//uhhhhmh hopefully this is safe to call off-thread?
				//the only thing that touches the main thread is swapping the 'state' variable, and... the new warning-reporting stuff (oh)
				//yeah that might not be thread safe
				load();
			}
		});
	}
	
	public static HalfDecentConfigFile make(CtxChain ctx, ConfigSection schema, Path path, LogFacade log, Executor background) {
		ctx = ctx.detail("Config file at " + path.getParent().getFileName() + "/" + path.getFileName());
		
		HalfDecentConfigFile cfg = new HalfDecentConfigFile(ctx, schema, path, log, background);
		cfg.load();
		cfg.watchForChanges();
		return cfg;
	}
}

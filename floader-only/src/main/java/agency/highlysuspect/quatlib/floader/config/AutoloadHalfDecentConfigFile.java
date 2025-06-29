package agency.highlysuspect.quatlib.floader.config;

import agency.highlysuspect.quatlib.any.config.ConfigOpt;
import agency.highlysuspect.quatlib.any.config.ConfigSection;
import agency.highlysuspect.quatlib.any.config.MatchedUnparsedConfig;
import agency.highlysuspect.quatlib.any.config.ReadableConfig;
import agency.highlysuspect.quatlib.any.config.WritableConfig;
import agency.highlysuspect.quatlib.any.config.failure.ConsoleReportFormatter;
import agency.highlysuspect.quatlib.any.config.failure.Report;
import agency.highlysuspect.quatlib.any.config.sn.Sn;
import agency.highlysuspect.quatlib.any.config.sn.SnParser;
import agency.highlysuspect.quatlib.any.config.sn.SnView;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public class AutoloadHalfDecentConfigFile implements ReadableConfig, WritableConfig {
	public AutoloadHalfDecentConfigFile(Path path, ConfigSection schema, Consumer<String> log, ExecutorService background) {
		this.path = path;
		this.schema = schema;
		this.log = log;
		this.background = background;
	}
	
	private final Path path;
	private final ConfigSection schema;
	private final Consumer<String> log;
	private final ExecutorService background;
	
	private Map<ConfigOpt<?>, Object> options = new IdentityHashMap<>();
	
	//TODO actually implement the filewatcher lmao
	//filewatcher debouncing
	long filewatcherDebounce = 0;
	protected static final int DEBOUNCE_MS = 300;
	//ignore filewatcher changes that happen after we manually save the file
	long lastIngameSave = 0;
	protected static final int SAVE_TIMEOUT_MS = 10000;
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(ConfigOpt<? extends T> opt) {
		T val = (T) options.get(opt);
		return val == null ? opt.getDefaultValue() : val;
	}
	
	@Override
	public void modify(Consumer<Handle> modifier) {
		//apply all of the changes, and then schedule a save (once!)
		modifier.accept(writeHandle);
		saveLater();
	}
	
	private final WritableConfig.Handle writeHandle = new Handle() {
		@Override
		public <T> Handle set(ConfigOpt<? super T> opt, T value) {
			options.put(opt, value);
			return writeHandle;
		}
	};
	
	public void saveNow() throws Report {
		doSave(options);
	}
	
	public void saveLater() {
		//make a clone that's hopefully safe to pass between threads
		Map<ConfigOpt<?>, Object> stateClone = new HashMap<>(options);
		background.submit(() -> {
			try {
				doSave(stateClone);
			} catch (Report e) {
				//TODO, log the error *properly* to the logger
				new ConsoleReportFormatter().report(Report.modify(e, it -> it.addMessage("Failed to save config to " + path)));
			}
		});
	}
	
	private void doSave(Map<ConfigOpt<?>, Object> state) throws Report {
		try {
			String serializedConfig = new HalfDecentConfigWriter().writeTopLevel(schema, new ReadableConfig.Mapped(state));
			
			//we're about to trigger the filewatcher by saving the file
			filewatcherDebounce = System.currentTimeMillis();
			lastIngameSave = System.currentTimeMillis();
			
			Files.writeString(path, serializedConfig, StandardCharsets.UTF_8);
		} catch (Throwable e) {
			//TODO, log the error *properly* to the logger
			new ConsoleReportFormatter().report(Report.modify(e, it -> it.addMessage("Failed to save config to " + path)));
		}
	}
	
	public void load() throws Report {
		try {
			String snString = Files.readString(path, StandardCharsets.UTF_8);
			
			Sn<?> parsed = new SnParser(snString).parseTopLevel();
			SnView view = parsed.view();
			MatchedUnparsedConfig matched = new MatchedUnparsedConfig().match(schema, view);
			
			options = new IdentityHashMap<>(matched.parseAndValidate().toMap());
			
		} catch (Throwable e) {
			throw Report.modify(e, it -> it.addMessage("Failed to load config from " + path));
		}
	}
}

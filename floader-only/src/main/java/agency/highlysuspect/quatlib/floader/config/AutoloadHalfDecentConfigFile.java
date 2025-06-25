package agency.highlysuspect.quatlib.floader.config;

import agency.highlysuspect.quatlib.any.config.ConfigOpt;
import agency.highlysuspect.quatlib.any.config.ConfigSection;
import agency.highlysuspect.quatlib.any.config.MatchedUnparsedConfig;
import agency.highlysuspect.quatlib.any.config.ReadableConfig;
import agency.highlysuspect.quatlib.any.config.ValidatedConfig;
import agency.highlysuspect.quatlib.any.config.WritableConfig;
import agency.highlysuspect.quatlib.any.config.failure.Report;
import agency.highlysuspect.quatlib.any.config.sn.Sn;
import agency.highlysuspect.quatlib.any.config.sn.SnParser;
import agency.highlysuspect.quatlib.any.config.sn.SnView;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class AutoloadHalfDecentConfigFile implements ReadableConfig, WritableConfig, WritableConfig.Handle {
	public AutoloadHalfDecentConfigFile(Path path, ConfigSection schema, Consumer<String> log) {
		this.path = path;
		this.schema = schema;
		this.log = log;
	}
	
	private final Path path;
	private final ConfigSection schema;
	private final Consumer<String> log;
	
	private final Map<ConfigOpt<?>, Object> options = new IdentityHashMap<>();
	
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
		modifier.accept(this);
	}
	
	@Override
	public <T> Handle set(ConfigOpt<? super T> opt, T value) {
		return this;
	}
	
	public void saveNow(Map<ConfigOpt<?>, Object> state) throws Report {
		try {
			String serializedConfig = new HalfDecentConfigWriter().writeTopLevel(schema, new ReadableConfig.Mapped(state));
			
			//we're about to trigger the filewatcher by saving the file
			filewatcherDebounce = System.currentTimeMillis();
			lastIngameSave = System.currentTimeMillis();
			
			Files.writeString(path, serializedConfig, StandardCharsets.UTF_8);
		} catch (Throwable e) {
			throw Report.modify(e, it -> it.addMessage("Failed to save config to " + path));
		}
	}
	
	//TODO: saveLater (runs on an executor service, makes a copy of the state before submitting to it though)
	
	public void load() throws Report {
		try {
			String snString = Files.readString(path, StandardCharsets.UTF_8);
			
			Sn<?> parsed = new SnParser(snString).parseTopLevel();
			SnView view = parsed.view();
			MatchedUnparsedConfig matched = new MatchedUnparsedConfig().match(schema, view);
			ValidatedConfig valid = new ValidatedConfig().parseAndValidate(matched);
			
			//TODO add some way to get the Map out of a ValidatedConfig or maybe even all configs
			
		} catch (Throwable e) {
			throw Report.modify(e, it -> it.addMessage("Failed to load config from " + path));
		}
	}
}

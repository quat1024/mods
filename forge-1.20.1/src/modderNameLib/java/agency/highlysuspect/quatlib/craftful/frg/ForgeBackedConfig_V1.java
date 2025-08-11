package agency.highlysuspect.quatlib.craftful.frg;

import agency.highlysuspect.quatlib.craftless.config.*;
import agency.highlysuspect.quatlib.craftless.config.sn.Sn;
import agency.highlysuspect.quatlib.craftless.config.sn.SnParser;
import agency.highlysuspect.quatlib.craftless.config.sn.SnWriter;
import agency.highlysuspect.quatlib.craftless.failure.CtxChain;
import agency.highlysuspect.quatlib.craftless.failure.ReportedException;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.*;
import java.util.function.Consumer;

/**
 * it's a v1 since i feel this is gonna be pretty shite
 */
public class ForgeBackedConfig_V1 implements WritableConfig {
	public ForgeBackedConfig_V1(CtxChain ctx, ConfigSection schema, ForgeConfigSpec.Builder builder) {
		liveValues = new HashMap<>();
		parsedValues = new HashMap<>();
		this.ctx = ctx;
		
		accept(schema, builder, true);
	}
	
	//walk a ConfigSection and add neo config options for all of the entries
	private void accept(SectOrOpt item, ForgeConfigSpec.Builder builder, boolean root) {
		builder.comment(item.getComment().toArray(new String[0]));
		if(item instanceof ConfigSection sect) {
			if(!root) builder.push(sect.getName());
			for(SectOrOpt child : sect.getChildren()) accept(child, builder, false);
			if(!root) builder.pop();
		} else if(item instanceof ConfigOpt<?> opt) {
			acceptOpt(opt, builder);
		}
	}
	
	private <T> void acceptOpt(ConfigOpt<T> opt, ForgeConfigSpec.Builder builder) {
		if(nightConfigFriendly(opt)) {
			//this type is simple and corresponds directly to a neo config type
			//skip my parser and just use neo's
			liveValues.put(opt, builder.define(opt.getName(), opt.getDefaultValue()));
		} else {
			//on forge's end it will be saved as a string, and i'll parse it myself
			String writtenDef = new SnWriter().write(opt.write(opt.getDefaultValue()));
			liveValues.put(opt, builder.define(opt.getName(), writtenDef));
		}
	}
	
	//mapping from ConfigOpts to neo values which change as the user edits the file
	private final Map<ConfigOpt<?>, ForgeConfigSpec.ConfigValue<?>> liveValues;
	//mapping from ConfigOpts to the actual values they represent.
	//lazily-populated, may be null if the option hasn't been loaded yet.
	private Map<ConfigOpt<?>, Object> parsedValues;
	//reload hooks
	private final List<Consumer<ReadableConfig>> reloadHooks = new ArrayList<>(1);
	
	//error context
	private final CtxChain ctx;
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(ConfigOpt<T> opt) {
		return (T) parsedValues.computeIfAbsent(opt, this::getFresh);
	}
	
	@Override
	public void modify(Consumer<Handle> modifier) {
		Set<ForgeConfigSpec.ConfigValue<?>> changed = new HashSet<>();
		modifier.accept(new Handle() {
			@Override
			public <T> Handle set(ConfigOpt<T> opt, T value) {
				//if the value passes validation and correction, store the corrected version
				//in the neo config, and also update my parsedValues cache
				ForgeConfigSpec.ConfigValue<?> live = liveValues.get(opt);
				validateAndCorrectWithMyRules(opt, live, value, ctxFor(live)).ifPresent(corrected -> {
					parsedValues.put(opt, corrected);
					setInForge(opt, live, corrected);
					changed.add(live);
				});
				return this;
			}
		});
		changed.forEach(ForgeConfigSpec.ConfigValue::save); //todo is this correct
	}
	
	private <T> T getFresh(ConfigOpt<T> opt) {
		ForgeConfigSpec.ConfigValue<?> live = liveValues.get(opt);
		return getFreshIfValid(opt, live).orElseGet(opt::getDefaultValue);
	}
	
	private <T, U> Optional<T> getFreshIfValid(ConfigOpt<T> opt, ForgeConfigSpec.ConfigValue<U> live) {
		CtxChain ctx2 = ctxFor(live);
		return parseWithMyRules(opt, live, ctx2)
			.flatMap(parsed -> validateAndCorrectWithMyRules(opt, live, parsed, ctx2));
	}
	
	//whether this option happens to be of a type that nightconfig can handle.
	//works by reading the default value. yeah that's kind of janky.
	private <T> boolean nightConfigFriendly(ConfigOpt<T> opt) {
		T def = opt.getDefaultValue();
		return def instanceof String || def instanceof Number || def instanceof Boolean;
	}
	
	//if it's nightconfig friendly, plop it directly in, otherwise write it as a string
	@SuppressWarnings("unchecked")
	private <T, U> void setInForge(ConfigOpt<T> opt, ForgeConfigSpec.ConfigValue<U> live, T val) {
		if(nightConfigFriendly(opt)) live.set((U) val);
		else live.set((U) new SnWriter().write(opt.write(val)));
	}
	
	@SuppressWarnings("unchecked")
	private <T, U> Optional<T> parseWithMyRules(ConfigOpt<T> opt, ForgeConfigSpec.ConfigValue<U> live, CtxChain ctx2) {
		try {
			T parsed;
			if(nightConfigFriendly(opt)) {
				//skip my parser and leverage forge's
				parsed = (T) live.get();
			} else {
				//in forge it gets saved as a string, i'll parse the sn
				String unparsed = (String) live.get();
				Sn<?> snParsed = new SnParser(unparsed).parseValue(ctx2);
				parsed = opt.parse(snParsed.view(ctx2), ctx2);
			}
			return Optional.of(parsed);
		} catch (ReportedException e) {
			return Optional.empty();
		}
	}
	
	private <T, U> Optional<T> validateAndCorrectWithMyRules(ConfigOpt<T> opt, ForgeConfigSpec.ConfigValue<U> live, T val, CtxChain ctx2) {
		try {
			T corrected = opt.correct(val, ctx2);
			opt.validate(corrected, ctx2);
			
			//if it was corrected, apply the correction to the forge config
			if(!Objects.equals(corrected, val)) setInForge(opt, live, corrected);
			
			return Optional.of(corrected);
		} catch (ReportedException e) {
			return Optional.empty();
		}
	}
	
	//TODO: this should probably take a ConfigOpt, they just don't know their own path at the moment
	private CtxChain ctxFor(ForgeConfigSpec.ConfigValue<?> live) {
		CtxChain ctx2 = ctx;
		for(String pathSegment : live.getPath()) ctx2 = ctx2.path(pathSegment);
		return ctx2;
	}
	
	public void onExternalChange() {
		//reparse all the values, keep the ones which didn't error.
		//this means an option failing validation or parsing will keep the
		//old value which i think is reasonable
		Map<ConfigOpt<?>, Object> newParsed = new HashMap<>(parsedValues);
		
		liveValues.forEach((opt, live) -> getFreshIfValid(opt, live).ifPresent(it -> newParsed.put(opt, it)));
		parsedValues = newParsed;
		
		for(Consumer<ReadableConfig> hook : reloadHooks) hook.accept(this);
	}
	
	public ForgeBackedConfig_V1 addReloadHook(Consumer<ReadableConfig> hook) {
		reloadHooks.add(hook);
		return this;
	}
	
	public void registerConfigReloadListeners(IEventBus modBus, ForgeConfigSpec builtSpec) {
		modBus.addListener((ModConfigEvent.Loading evt) -> {
			if(evt.getConfig().getSpec() == builtSpec) onExternalChange();
		});
		modBus.addListener((ModConfigEvent.Reloading evt) -> {
			if(evt.getConfig().getSpec() == builtSpec) onExternalChange();
		});
	}
	
	public static ForgeBackedConfig_V1 make(CtxChain ctx, ConfigSection schema, IEventBus modBus, ModContainer me) {
		return make(ctx, schema, modBus, me, ModConfig.Type.COMMON);
	}
	
	public static ForgeBackedConfig_V1 make(CtxChain ctx, ConfigSection schema, IEventBus modBus, ModContainer me, ModConfig.Type type) {
		ctx = ctx.detail("Config file at config/" + me.getModId() + "-" + type.extension() + ".toml");
		
		ForgeConfigSpec.Builder bob = new ForgeConfigSpec.Builder();
		ForgeBackedConfig_V1 cfg = new ForgeBackedConfig_V1(ctx, schema, bob);
		ForgeConfigSpec built = bob.build();
		ModLoadingContext.get().registerConfig(type, built);
		cfg.registerConfigReloadListeners(modBus, built);
		return cfg;
	}
}

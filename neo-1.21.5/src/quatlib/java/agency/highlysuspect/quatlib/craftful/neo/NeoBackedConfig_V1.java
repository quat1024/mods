package agency.highlysuspect.quatlib.craftful.neo;

import agency.highlysuspect.quatlib.craftless.config.ConfigOpt;
import agency.highlysuspect.quatlib.craftless.config.ConfigSection;
import agency.highlysuspect.quatlib.craftless.config.SectOrOpt;
import agency.highlysuspect.quatlib.craftless.config.WritableConfig;
import agency.highlysuspect.quatlib.craftless.config.sn.Sn;
import agency.highlysuspect.quatlib.craftless.config.sn.SnParser;
import agency.highlysuspect.quatlib.craftless.config.sn.SnWriter;
import agency.highlysuspect.quatlib.craftless.failure.CtxChain;
import agency.highlysuspect.quatlib.craftless.failure.ReportedException;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 * it's a v1 since i feel this is gonna be pretty shite
 */
public class NeoBackedConfig_V1 implements WritableConfig {
	public NeoBackedConfig_V1(CtxChain ctx, ConfigSection schema, ModConfigSpec.Builder builder) {
		liveValues = new HashMap<>();
		parsedValues = new HashMap<>();
		this.ctx = ctx;
		
		accept(schema, builder, true);
	}
	
	private void accept(SectOrOpt item, ModConfigSpec.Builder builder, boolean root) {
		builder.comment(item.getComment().toArray(new String[0]));
		
		if(root) builder.comment("");
		
		if(item instanceof ConfigSection sect) {
			if(!root) builder.push(sect.getName());
			for(SectOrOpt child : sect.getChildren()) accept(child, builder, false);
			if(!root) builder.pop();
		} else if(item instanceof ConfigOpt<?> opt) {
			acceptOpt(opt, builder);
		}
	}
	
	private <T> void acceptOpt(ConfigOpt<T> opt, ModConfigSpec.Builder builder) {
		//for simple types, integrate better into the forge config system
		if(putDirectlyInForge(opt)) {
			liveValues.put(opt, builder.define(opt.getName(), opt.getDefaultValue()));
		} else {
			//fall back to the full sn system
			String writtenDef = new SnWriter().write(opt.write(opt.getDefaultValue()));
			liveValues.put(opt, builder.define(opt.getName(), writtenDef));
		}
	}
	
	private <T> boolean putDirectlyInForge(ConfigOpt<T> opt) {
		T def = opt.getDefaultValue();
		return def instanceof String || def instanceof Number || def instanceof Boolean;
	}
	
	private final Map<ConfigOpt<?>, ModConfigSpec.ConfigValue<?>> liveValues;
	private Map<ConfigOpt<?>, Object> parsedValues;
	private final CtxChain ctx;
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(ConfigOpt<? extends T> opt) {
		return (T) parsedValues.computeIfAbsent(opt, this::getFresh);
	}
	
	@Override
	public void modify(Consumer<Handle> modifier) {
		Set<ModConfigSpec.ConfigValue<?>> changed = new HashSet<>();
		
		modifier.accept(new Handle() {
			@Override
			public <T> Handle set(ConfigOpt<? super T> opt, T value) {
				//TODO: do correction, validation (i think the ? super T bound is making it impossible)
				parsedValues.put(opt, value);
				
				//write through into forge
				ModConfigSpec.ConfigValue<?> live = liveValues.get(opt);
				setInForge(opt, live, value);
				changed.add(live);
				
				return this;
			}
		});
		
		changed.forEach(ModConfigSpec.ConfigValue::save);
	}
	
	private <T> T getFresh(ConfigOpt<T> opt) {
		return parseValidateAndCorrectWithMyRules(opt, liveValues.get(opt))
			.orElseGet(opt::getDefaultValue);
	}
	
	@SuppressWarnings("unchecked")
	private <T, U> Optional<T> parseValidateAndCorrectWithMyRules(ConfigOpt<T> opt, ModConfigSpec.ConfigValue<U> live) {
		CtxChain ctx2 = ctx.detail("Option '" + String.join("'.'", live.getPath()) + "'");
		
		try {
			T parsed;
			if(putDirectlyInForge(opt)) {
				//this is a type where it's okay to skip my parser and put it directly in the forge config
				//number, string, boolean basically
				parsed = (T) live.get();
			} else {
				//it's something more complex, in forge it gets saved as a string and i'll do the parsing myself
				String unparsed = (String) live.get();
				Sn<?> snParsed = new SnParser(unparsed).parseValue(ctx2);
				parsed = opt.parse(snParsed.view(ctx2), ctx2);
			}
			
			T corrected = opt.correct(parsed, ctx2);
			opt.validate(corrected, ctx2);
			
			//if it was corrected apply the correction to the forge config
			if(!Objects.equals(corrected, parsed))
				setInForge(opt, live, corrected);
			
			return Optional.of(corrected);
		} catch (ReportedException e) {
			return Optional.empty();
		}
	}
	
	@SuppressWarnings("unchecked")
	private <T, U> void setInForge(ConfigOpt<T> opt, ModConfigSpec.ConfigValue<U> live, T val) {
		if(putDirectlyInForge(opt)) live.set((U) val);
		else live.set((U) new SnWriter().write(opt.write(val)));
	}
	
	public void onExternalChange() {
		Map<ConfigOpt<?>, Object> newParsed = new HashMap<>(parsedValues);
		liveValues.forEach((myOpt, forgeOpt) ->
			parseValidateAndCorrectWithMyRules(myOpt, forgeOpt)
				.ifPresent(it -> newParsed.put(myOpt, it)));
		parsedValues = newParsed;
	}
}

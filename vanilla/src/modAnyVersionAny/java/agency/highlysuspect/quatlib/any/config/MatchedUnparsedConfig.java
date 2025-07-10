package agency.highlysuspect.quatlib.any.config;

import agency.highlysuspect.quatlib.any.config.sn.SnView;
import agency.highlysuspect.quatlib.any.failure.ContextChain;
import agency.highlysuspect.quatlib.any.failure.Report;
import agency.highlysuspect.quatlib.any.failure.Report2;

import java.util.IdentityHashMap;
import java.util.Map;

public class MatchedUnparsedConfig extends IdentityHashMap<ConfigOpt<?>, SnView> {
	public MatchedUnparsedConfig(ConfigSection schema, SnView view, ContextChain ctx) {
		matchImpl(schema, view, ctx);
	}
	
	private void matchImpl(SectOrOpt item, SnView view, ContextChain ctx) {
		if(view == null) return;
		
		//if we're looking at a config section
		if(item instanceof ConfigSection section) {
			//then we should also be looking at an sn map
			SnView.MapView map = view.asMapOrNull();
			if(map == null) {
				//TODO: warning?
				return;
			}
			
			//each child in the schema should have a corresponding child in the sn
			for(SectOrOpt child : section.getChildren()) {
				SnView childSn = map.getOrNull(child.getName());
				if(childSn == null) {
					//TODO: warning?
					continue;
				}
				matchImpl(child, childSn, ctx);
			}
		}
		
		//otherwise we're looking at a config option
		if(item instanceof ConfigOpt<?> opt) {
			put(opt, view);
		}
	}
	
	public ValidatedConfig parseAndValidate(ContextChain ctx) {
		ValidatedConfig validOptions = new ValidatedConfig();
		
		for(Map.Entry<ConfigOpt<?>, SnView> e : entrySet()) {
			ConfigOpt<?> opt = e.getKey();
			SnView sn = e.getValue();
			if(sn == null) continue; //expected, if the config file is too small
			
			validOptions.put(opt, parseAndValidateImpl(opt, sn, ctx));
		}
		return validOptions;
	}
	
	//just need to name the generic
	private <T> T parseAndValidateImpl(ConfigOpt<T> opt, SnView view, ContextChain ctx) {
		//parse it. parsing is a failable operation and we might not get a T.
		T parsed;
		try {
			parsed = opt.parse(view, ctx.detail("Option '" + view.path() + "' failed to parse"));
		} catch (Report e) {
			//TODO phasing out Report in favor of Report2.
			throw new RuntimeException(e);
		} catch (Report2 e) {
			//TODO: include some kind of "using default value ..." warning?
			return opt.getDefaultValue();
		}
		
		//correct it. this might pop a warning if the value is out of range or something.
		T corrected = opt.correct(parsed, ctx.detail("Option '" + view.path() + "' needed correction"));
		
		//validate it. this will pop errors if the value breaks constraints
		boolean valid = opt.validate(corrected, ctx.detail("Option '" + view.path() + "' failed validation"));
		if(valid) return corrected;
		
		return opt.getDefaultValue();
	}
}

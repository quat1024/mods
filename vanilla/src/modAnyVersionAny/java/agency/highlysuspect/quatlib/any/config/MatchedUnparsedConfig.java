package agency.highlysuspect.quatlib.any.config;

import agency.highlysuspect.quatlib.any.config.sn.SnView;
import agency.highlysuspect.quatlib.any.failure.CtxChain;
import agency.highlysuspect.quatlib.any.failure.ReportedException;

import java.util.IdentityHashMap;
import java.util.Map;

public class MatchedUnparsedConfig extends IdentityHashMap<ConfigOpt<?>, SnView> {
	public MatchedUnparsedConfig(ConfigSection schema, SnView view) {
		matchImpl(schema, view);
	}
	
	private void matchImpl(SectOrOpt item, SnView view) {
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
				matchImpl(child, childSn);
			}
		}
		
		//otherwise we're looking at a config option
		if(item instanceof ConfigOpt<?> opt) {
			put(opt, view);
		}
	}
	
	public ValidatedConfig parseAndValidate(CtxChain ctx) {
		ValidatedConfig validOptions = new ValidatedConfig();
		
		for(Map.Entry<ConfigOpt<?>, SnView> e : entrySet()) {
			ConfigOpt<?> opt = e.getKey();
			SnView sn = e.getValue();
			if(sn == null) continue;
			
			Object valid;
			try {
				valid = parseAndValidateImpl(opt, sn, ctx);
			} catch (ReportedException reported) {
				//there was some problem parsing this config option
				//but it's been reported, so let's keep going and parse the next one
				continue;
			}
			validOptions.put(opt, valid);
		}
		return validOptions;
	}
	
	//just need to name the generic
	private <T> T parseAndValidateImpl(ConfigOpt<T> opt, SnView view, CtxChain ctx) throws ReportedException {
		T parsed = opt.parse(view, ctx); //throws on parse error
		T corrected = opt.correct(parsed, ctx);
		opt.validate(corrected, ctx); //throws on validation error
		return corrected;
	}
}

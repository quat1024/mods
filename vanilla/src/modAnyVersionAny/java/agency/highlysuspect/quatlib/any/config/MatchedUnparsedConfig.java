package agency.highlysuspect.quatlib.any.config;

import agency.highlysuspect.quatlib.any.config.sn.SnView;
import agency.highlysuspect.quatlib.any.failure.ContextChain;
import agency.highlysuspect.quatlib.any.failure.Report;
import agency.highlysuspect.quatlib.any.failure.ReportedException;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

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
	
	public ValidatedConfig parseAndValidate() throws Report {
		ValidatedConfig validOptions = new ValidatedConfig();
		
		for(Map.Entry<ConfigOpt<?>, SnView> e : entrySet()) {
			ConfigOpt<?> opt = e.getKey();
			SnView sn = e.getValue();
			if(sn == null) continue;
			
			validOptions.put(opt, parseAndValidateImpl(opt, sn));
		}
		return validOptions;
	}
	
	//just need to name the generic
	private <T> T parseAndValidateImpl(ConfigOpt<T> opt, SnView view) throws Report {
		//TODO: catch exceptions and add them to a warnings list, then ignore the failing option
		try {
			T parsed = opt.parse(view);
			T corrected = opt.correct(parsed);
			opt.validate(corrected);
			return corrected;
		} catch (Throwable e) {
			throw Report.modify(e, it -> it.addMessage("Problem while parsing option '" + view.path() + "'"));
		}
	}
	
	public ValidatedConfig parseAndValidate2(ContextChain ctx) {
		ValidatedConfig validOptions = new ValidatedConfig();
		
		for(Map.Entry<ConfigOpt<?>, SnView> e : entrySet()) {
			ConfigOpt<?> opt = e.getKey();
			SnView sn = e.getValue();
			if(sn == null) continue;
			
			try {
				validOptions.put(opt, parseAndValidateImpl2(opt, sn, ctx));
			} catch (ReportedException reported) {
				//was some problem parsing this config option
				//but it's been reported so let's keep going and parse the next one
			}
		}
		return validOptions;
	}
	
	//just need to name the generic
	private <T> T parseAndValidateImpl2(ConfigOpt<T> opt, SnView view, ContextChain ctx) throws ReportedException {
		T parsed = opt.parse(view);
		T corrected = opt.correct(parsed);
		opt.validate(corrected);
		return corrected;
	}
}

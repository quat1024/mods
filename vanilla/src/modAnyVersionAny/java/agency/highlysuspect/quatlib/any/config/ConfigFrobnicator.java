package agency.highlysuspect.quatlib.any.config;

import agency.highlysuspect.quatlib.any.config.sn.SnException;
import agency.highlysuspect.quatlib.any.config.sn.SnView;

import java.util.HashMap;
import java.util.Map;

//I have no idea what to call this shit
//TODO put this code somewhere sensible
public class ConfigFrobnicator {
	public Map<ConfigOpt<?>, SnView> assignSn(ConfigSection schema, SnView view) throws SnException {
		Map<ConfigOpt<?>, SnView> result = new HashMap<>();
		assignSnImpl(schema, view, result);
		return result;
	}
	
	private void assignSnImpl(SectOrOpt item, SnView view, Map<ConfigOpt<?>, SnView> result) throws SnException {
		//if we're looking at a config section
		if(item instanceof ConfigSection section) {
			//then we should also be looking at an sn map
			SnView.MapView map = view.asMap(); //TODO: throws, should warn instead
			
			//each child in the schema should have a corresponding child in the sn
			for(SectOrOpt child : section.getChildren()) {
				SnView childSn = map.get(child.getName());
				assignSnImpl(child, childSn, result);
			}
		}
		
		//otherwise we're looking at a config option
		if(item instanceof ConfigOpt<?> opt) {
			System.out.println("assigning option " + opt.getName() + " to sn " + view.path());
			result.put(opt, view);
		}
	}
	
	public ConfigState.Mapped parseAndValidate(Map<ConfigOpt<?>, SnView> assigned) throws ConfigException {
		Map<ConfigOpt<?>, Object> map = new HashMap<>();
		
		for(Map.Entry<ConfigOpt<?>, SnView> e : assigned.entrySet()) {
			map.put(e.getKey(), parseAndValidateImpl(e.getKey(), e.getValue()));
		}
		
		return new ConfigState.Mapped(map);
	}
	
	private <T> T parseAndValidateImpl(ConfigOpt<T> opt, SnView view) throws ConfigException {
		T parsed = opt.parse(view);
		T corrected = opt.correct(parsed);
		opt.validate(corrected);
		return corrected;
	}
}

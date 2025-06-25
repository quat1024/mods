package agency.highlysuspect.quatlib.any.config;

import agency.highlysuspect.quatlib.any.config.sn.SnView;

import java.util.LinkedHashMap;
import java.util.Map;

public class MatchedUnparsedConfig extends LinkedHashMap<ConfigOpt<?>, SnView> {
	public MatchedUnparsedConfig() {}
	
	public MatchedUnparsedConfig(Map<? extends ConfigOpt<?>, ? extends SnView> m) {
		super(m);
	}
	
	public MatchedUnparsedConfig match(ConfigSection schema, SnView view) {
		matchImpl(schema, view);
		return this;
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
}

package agency.highlysuspect.quatlib.any.config;

import agency.highlysuspect.quatlib.any.util.SnocList;

import java.util.HashMap;
import java.util.Map;

//I have no idea what to call this shit
//TODO put this code somewhere sensible
public class ConfigFrobnicator {
	@SuppressWarnings("Convert2Diamond") //javac doesn't like it
	public Map<ConfigOpt<?>, String> assignStrings(ConfigSection root, Map<SnocList<String>, String> stringyConfig) {
		Map<ConfigOpt<?>, String> stringyValues = new HashMap<>();
		
		root.accept(new ConfigVisitor<RuntimeException>() {
			SnocList<String> path = SnocList.empty();
			
			@Override
			public void openSection(ConfigSection section) {
				path = path.snoc(section.getName());
			}
			
			@Override
			public void closeSection(ConfigSection section) {
				path = path.head();
			}
			
			@Override
			public <T> void visitOpt(ConfigOpt<T> opt) {
				SnocList<String> bigPath = path.snoc(opt.getName());
				String stringyValue = stringyConfig.get(bigPath);
				if(stringyValue == null) {
					//uhh
					System.out.println("missing value for " + bigPath);
				} else {
					stringyValues.put(opt, stringyValue);
				}
			}
		});
		
		return stringyValues;
	}
	
	public ConfigState.Mapped parseAndValidate(ConfigSection root, Map<ConfigOpt<?>, String> assigned) throws ConfigException {
		Map<ConfigOpt<?>, Object> map = new HashMap<>();
		
		root.accept(new ConfigVisitor<ConfigException>() {
			SnocList<String> path = SnocList.empty();
			
			@Override
			public void openSection(ConfigSection section) {
				path = path.snoc(section.getName());
			}
			
			@Override
			public void closeSection(ConfigSection section) {
				path = path.head();
			}
			
			@Override
			public <T> void visitOpt(ConfigOpt<T> opt) throws ConfigException {
				String stringValue = assigned.get(opt);
				if(stringValue == null) {
					//weird
					map.put(opt, opt.getDefaultValue());
					return;
				}
				
				T parsed = opt.parse(stringValue);
				T corrected = opt.correct(parsed);
				
				map.put(opt, corrected);
			}
		});
		
		return new ConfigState.Mapped(map);
	}
}

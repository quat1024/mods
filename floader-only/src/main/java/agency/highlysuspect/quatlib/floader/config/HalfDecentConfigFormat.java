package agency.highlysuspect.quatlib.floader.config;

import agency.highlysuspect.quatlib.any.config.ConfigException;
import agency.highlysuspect.quatlib.any.config.ConfigFrobnicator;
import agency.highlysuspect.quatlib.any.config.ConfigOpt;
import agency.highlysuspect.quatlib.any.config.ConfigSection;
import agency.highlysuspect.quatlib.any.config.ConfigState;
import agency.highlysuspect.quatlib.any.config.SectOrOpt;
import agency.highlysuspect.quatlib.any.config.sn.ConcreteInfo;
import agency.highlysuspect.quatlib.any.config.sn.Sn;
import agency.highlysuspect.quatlib.any.config.sn.SnMap;
import agency.highlysuspect.quatlib.any.config.sn.SnParser;
import agency.highlysuspect.quatlib.any.config.sn.SnView;
import agency.highlysuspect.quatlib.any.config.sn.SnWriter;

import java.util.Map;

public class HalfDecentConfigFormat {
	public SnMap toSn(ConfigSection schema, ConfigState state, ConcreteInfo concrete) {
		SnMap target = new SnMap();
		concrete.assignComment(target, schema.getComment()); //root comment goes on the root
		
		for(SectOrOpt child : schema.getChildren()) {
			toSnImpl(child, state, target, concrete);
		}
		
		return target;
	}
	
	private void toSnImpl(SectOrOpt item, ConfigState state, SnMap target, ConcreteInfo concrete) {
		if(item instanceof ConfigSection section) {
			SnMap subMap = new SnMap();
			for(SectOrOpt child : section.getChildren()) {
				toSnImpl(child, state, subMap, concrete);
			}
			target.put(section.getName(), subMap);
			
			//the subcategory's comment goes on its map
			concrete.assignComment(subMap, item.getComment());
		}
		
		if(item instanceof ConfigOpt<?> opt) {
			Sn<?> sn = getSn(opt, state);
			target.put(opt.getName(), sn);
			//and the config option's comment goes here
			concrete.assignComment(sn, opt.getComment());
		}
	}
	
	//to name the generic
	private static <T> Sn<?> getSn(ConfigOpt<T> opt, ConfigState state) {
		T value = state.get(opt);
		return opt.write(value);
	}
	
	public static void main(String... args) throws ConfigException {
		//define configopts somewhere, these can be globals or whatever
		ConfigOpt<Integer> rabbits = new ConfigOpt.IntOpt("rabbits", 5, "How many rabbits?").setMin(0);
		ConfigOpt<Integer> bunnies = new ConfigOpt.IntOpt("bunnies", 5, "How many bunnies?").setMin(0);
		ConfigOpt<Integer> lizards = new ConfigOpt.IntOpt("lizards", 5, "How many lizards?").setMin(0);
		ConfigOpt<Integer> dragons = new ConfigOpt.IntOpt("dragons", 5, "How many dragons?").setMin(0);
		ConfigOpt<String> dragonEssay = new ConfigOpt.StringOpt("Dragon Essay Question", "Pretty good", "How do you feel about dragons?");
		
		//shape them into a schema with sections
		ConfigSection schema = new ConfigSection("coolmod", "This is my cool config file");
		schema.subsection("mammals", "Yuck stinky mammals").add(rabbits, bunnies);
		schema.subsection("reptiles", "Wooo lets go", "I love lizards").add(lizards, dragons, dragonEssay);
		
		//parse to an Sn
		ConcreteInfo ci = new ConcreteInfo();
		SnMap intermediateRepresentation = new HalfDecentConfigFormat().toSn(schema, ConfigState.Default.INSTANCE, ci);
		
		//write it out
		String written = new SnWriter.Commented(ci).writeTopLevel(intermediateRepresentation);
		
		String modified = written.replace("dragons = 5", "dragons = 999");
		System.out.println(modified);
		
		//parse it back, first into an Sn (json if it only had strings)
		SnMap parsed = new SnParser(modified).parseTopLevel();
		SnView view = new SnView.Impl(parsed);
		
		//figure out which bit of Sn goes to which option
		Map<ConfigOpt<?>, SnView> assigned = new ConfigFrobnicator().assignSn(schema, view);
		
		//finally parse into real java objects
		ConfigState validated = new ConfigFrobnicator().parseAndValidate(assigned);
		
		//reading the config is pretty simple and uses the ConfigOpt objects for well-typedness
		System.out.println("There are " + validated.get(dragons) + " dragons");
	}
}

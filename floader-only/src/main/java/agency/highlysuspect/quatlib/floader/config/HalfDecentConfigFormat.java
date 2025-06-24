package agency.highlysuspect.quatlib.floader.config;

import agency.highlysuspect.quatlib.any.config.ConfigFrobnicator;
import agency.highlysuspect.quatlib.any.config.ConfigOpt;
import agency.highlysuspect.quatlib.any.config.ConfigSection;
import agency.highlysuspect.quatlib.any.config.ConfigState;
import agency.highlysuspect.quatlib.any.config.failure.ConsoleReportFormatter;
import agency.highlysuspect.quatlib.any.config.failure.Report;
import agency.highlysuspect.quatlib.any.config.sn.SnMap;
import agency.highlysuspect.quatlib.any.config.sn.SnParser;
import agency.highlysuspect.quatlib.any.config.sn.SnView;

import java.util.Map;

public class HalfDecentConfigFormat {
	public static void main(String... args) {
		try {
			main2();
		} catch (Report e) {
			new ConsoleReportFormatter().report(e);
		}
	}
	
	public static void main2() throws Report {
		//define configopts somewhere, these can be globals or whatever
		ConfigOpt<Integer> rabbits = new ConfigOpt.IntOpt("rabbits", 5, "How many rabbits?").setMin(0);
		ConfigOpt<Integer> bunnies = new ConfigOpt.IntOpt("bunnies", 5, "How many bunnies?").setMin(0);
		ConfigOpt<Integer> lizards = new ConfigOpt.IntOpt("lizards", 5, "How many lizards?").setMin(0);
		ConfigOpt<Integer> dragons = new ConfigOpt.IntOpt("dragons", 5, "How many dragons?").setMin(0);
		ConfigOpt<String> dragonEssay = new ConfigOpt.StringOpt("Dragon Essay Question", "Pretty good", "How do you feel about dragons?");
		
		//shape them into a schema with sections
		ConfigSection schema = new ConfigSection("coolmod", "This is my cool config file");
		schema.subsection("mammals", "Sorry for saying bunnys are stinky before", "I didn't mean it").add(rabbits, bunnies);
		schema.subsection("reptiles", "Wooo lets go", "I love lizards").add(lizards, dragons, dragonEssay);
		
		//write out the default config
		String written = new HalfDecentConfigWriter().writeTopLevel(schema, ConfigState.Default.INSTANCE);
		
		//simulate modifying the config...
		String modified = written.replace("dragons = 5", "dragons = 999");
		System.out.println(modified);
		
		//parse it back, first into an Sn (a structure that's like json, if it had only strings)
		SnMap parsed = new SnParser(modified).parseTopLevel();
		SnView view = parsed.view();
		
		//figure out which fragment of Sn goes to which option
		Map<ConfigOpt<?>, SnView> assigned = new ConfigFrobnicator().assignSn(schema, view);
		
		//finally parse into real java objects
		ConfigState validated = new ConfigFrobnicator().parseAndValidate(assigned);
		
		//reading the config is pretty simple and uses the ConfigOpt objects for well-typedness
		System.out.println("There are " + validated.get(dragons) + " dragons");
//			String modified2 = modified.replace("999", "{ \"oh\" = \"no\" }");

//		//what if there's an error?
		//TODO: the error report seems backwards to me (it starts with 'could not parse as integer')
		// (i hastily flipped it around with the console formatter, hmm)
		try {
			String modified2 = modified.replace("999", "jsdhakhdjsad");
			System.out.println(modified2);
			view = new SnParser(modified2).parseTopLevel().view();
			assigned = new ConfigFrobnicator().assignSn(schema, view);
			validated = new ConfigFrobnicator().parseAndValidate(assigned);
		} catch (Throwable e) {
			throw Report.modify(e, it -> it.addMessage("Problem loading MyCoolMod config file at config/my_cool_mod.txt"));
		}
		
		//you can browse this structure in a typesafe way without parsing it further than strings! kinda fun!
		//useful for handling migrations. if you delete a config option it's still in the old config files, right?
		SnView reptilesView = view.asMap().get("reptiles");
		SnView essayQuestionView = reptilesView.asMap().get("Dragon Essay Question");
		System.out.println(essayQuestionView);
		//-> SnView, path 'reptiles.Dragon Essay Question' (looking at SnStr)
		System.out.println("the essay is: " + essayQuestionView.asString());
		//-> Pretty good
	}
}

package agency.highlysuspect.quatlib.any.config.hdc;

import agency.highlysuspect.quatlib.any.config.ConfigOpt;
import agency.highlysuspect.quatlib.any.config.ConfigSection;
import agency.highlysuspect.quatlib.any.config.DefaultConfig;
import agency.highlysuspect.quatlib.any.config.MatchedUnparsedConfig;
import agency.highlysuspect.quatlib.any.config.MutableMapConfig;
import agency.highlysuspect.quatlib.any.config.ReadableConfig;
import agency.highlysuspect.quatlib.any.config.ValidatedConfig;
import agency.highlysuspect.quatlib.any.config.sn.SnMap;
import agency.highlysuspect.quatlib.any.config.sn.SnParser;
import agency.highlysuspect.quatlib.any.config.sn.SnView;
import agency.highlysuspect.quatlib.any.failure.ConsoleReportFormatter;
import agency.highlysuspect.quatlib.any.failure.FailureBin;
import agency.highlysuspect.quatlib.any.failure.Report;
import agency.highlysuspect.quatlib.any.failure.Report2;

public class HdcTestingAaaa {
	public static void main(String... args) throws Report2 {
		try {
			main2();
		} catch (Report e) {
			new ConsoleReportFormatter().report(e);
		}
	}
	
	public static void main2() throws Report, Report2 {
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
		String written = new HalfDecentConfigWriter().writeTopLevel(schema, DefaultConfig.INSTANCE);
		
		//simulate modifying the config...
		String modified = written.replace("dragons = 5", "dragons = 999");
		System.out.println(modified);
		
		//parse it back, first into an Sn (a structure that's like json, if it had only strings)
		FailureBin failures = new FailureBin();
		SnMap parsed = new SnParser(modified).parseTopLevel(failures.detail("While parsing the config file at: asdf.txt"));
		SnView view = parsed.view();
		
		//figure out which fragment of Sn goes to which option
		MatchedUnparsedConfig matched = new MatchedUnparsedConfig(schema, view, failures.detail("While matching config file"));
		
		//finally parse into real java objects
		ValidatedConfig validated = matched.parseAndValidate(failures.detail("While parsing and validating config file"));
		ReadableConfig config = new MutableMapConfig(validated.toMap());
		
		//reading the config is pretty simple and uses the ConfigOpt objects for well-typedness
		System.out.println("There are " + config.get(dragons) + " dragons");
		
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

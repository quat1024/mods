package agency.highlysuspect.quatlib.floader.config;

import agency.highlysuspect.quatlib.any.config.ConfigException;
import agency.highlysuspect.quatlib.any.config.ConfigFrobnicator;
import agency.highlysuspect.quatlib.any.config.ConfigOpt;
import agency.highlysuspect.quatlib.any.config.ConfigSection;
import agency.highlysuspect.quatlib.any.config.ConfigState;
import agency.highlysuspect.quatlib.any.config.ConfigVisitor;
import agency.highlysuspect.quatlib.any.util.SnocList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HalfDecentConfigFormat {
	public String write(ConfigSection root, ConfigState state) {
		List<String> out = new ArrayList<>();
		
		root.accept(new ConfigVisitor<>() {
			int depth = -1;
			
			private void write(String... lines) {
				String indent = "\t".repeat(Math.max(0, depth));
				for(String line : lines) out.add(indent + line);
			}
			
			private void blank() {
				out.add("");
			}
			
			@Override
			public void openSection(ConfigSection section) {
				depth++;
				
				for(String commentLine : section.getComment()) {
					write("# " + commentLine);
				}
				write(section.getName() + " {");
			}
			
			@Override
			public void closeSection(ConfigSection section) {
				//tidy up blank lines
				if(!out.isEmpty() && out.getLast().isEmpty()) out.removeLast();
				
				write("}");
				blank();
				depth--;
			}
			
			@Override
			public <T> void visitOpt(ConfigOpt<T> opt) {
				depth++;
				for(String commentLine : opt.getComment()) write("# " + commentLine);
				
				T defaultValue = opt.getDefaultValue();
				String writtenDefaultValue = opt.write(defaultValue);
				if(writtenDefaultValue.isEmpty()) writtenDefaultValue = "<empty>";
				write("# Default: " + writtenDefaultValue);
				
				//TODO: escape shit like newlines
				T currentValue = state.get(opt);
				write(opt.getName() + ": " + opt.write(currentValue));
				
				blank();
				depth--;
			}
		});
		
		return String.join("\n", out);
	}
	
	
	public Map<SnocList<String>, String> parseToStrings(String configFile) {
		Map<SnocList<String>, String> result = new LinkedHashMap<>();
		Lineserator iter = new Lineserator(configFile.lines().iterator());
		parseToStringsImpl(iter, SnocList.empty(), result);
		return result;
	}
	
	private void parseToStringsImpl(Lineserator iter, SnocList<String> fullName, Map<SnocList<String>, String> result) {
		while(iter.hasNext()) {
			String line = iter.next().trim();
			if(line.isEmpty() || line.startsWith("#")) continue;
			
			String[] split = line.split(":", 2);
			if(split.length == 2) {
				//if it has a colon, it's a value
				String key = split[0].trim();
				String value = split[1].trim();
				result.put(fullName.snoc(key), value);
			} else if(line.startsWith("}")) {
				//closing a section
				return;
			} else if(line.endsWith("{")) {
				//opening a section: recur into the section
				String sectionName = line.substring(0, line.length() - 1).trim();
				parseToStringsImpl(iter, fullName.snoc(sectionName), result);
			}
		}
	}
	
	protected static class Lineserator implements Iterator<String> {
		public Lineserator(Iterator<String> delegate) {
			this.delegate = delegate;
		}
		
		private final Iterator<String> delegate;
		int lineNo = 0;
		
		@Override
		public boolean hasNext() {
			return delegate.hasNext();
		}
		
		@Override
		public String next() {
			lineNo++;
			return delegate.next();
		}
		
		public int getLineNo() {
			return lineNo;
		}
	}
	
	public static void main(String... args) throws ConfigException {
		
		//define configopts somewhere, these can be globals or whatever
		ConfigOpt<Integer> rabbits = new ConfigOpt.IntOpt("rabbits", 5, "How many rabbits?").setMin(0);
		ConfigOpt<Integer> bunnies = new ConfigOpt.IntOpt("bunnies", 5, "How many bunnies?").setMin(0);
		ConfigOpt<Integer> lizards = new ConfigOpt.IntOpt("lizards", 5, "How many lizards?").setMin(0);
		ConfigOpt<Integer> dragons = new ConfigOpt.IntOpt("dragons", 5, "How many dragons?").setMin(0);
		
		//shape them into a schema with sections
		ConfigSection schema = new ConfigSection("coolmod", "This is my cool config file");
		schema.subsection("mammals", "Yuck stinky mammals").add(rabbits, bunnies);
		schema.subsection("reptiles", "Wooo lets go", "I love lizards").add(lizards, dragons);
		
		//format it to a config file
		String written = new HalfDecentConfigFormat().write(schema, ConfigState.Default.INSTANCE);
		
		String modified = written.replace("dragons: 5", "dragons: 999");
		System.out.println(modified);
		
		//parse it back, first into a bag of strings
		Map<SnocList<String>, String> parsed = new HalfDecentConfigFormat().parseToStrings(modified);
		//then figure out which string goes to which option
		//yes this api is bad, I need to figure out where to put this code
		Map<ConfigOpt<?>, String> assigned = new ConfigFrobnicator().assignStrings(schema, parsed);
		//and finally parse them into real java objects and run validations, ditto for the code organization
		ConfigState validated = new ConfigFrobnicator().parseAndValidate(schema, assigned);
		
		//reading the config is pretty simple and uses the ConfigOpt objects for well-typedness
		System.out.println("There are " + validated.get(dragons) + " dragons");
		
		
		String written2 = new HalfDecentConfigFormat().write(schema, validated);
		if(modified.equals(written2)) {
			System.out.println("THEYRE THE SAME");
		} else {
			System.out.println("THEYRE DIFFERENT?????");
		}
	}
}

package agency.highlysuspect.quatlib.floader.config;

import agency.highlysuspect.quatlib.any.config.ConfigOpt;
import agency.highlysuspect.quatlib.any.config.ConfigSection;
import agency.highlysuspect.quatlib.any.config.ConfigState;
import agency.highlysuspect.quatlib.any.config.SectOrOpt;
import agency.highlysuspect.quatlib.any.config.sn.Sn;
import agency.highlysuspect.quatlib.any.config.sn.SnWriter;
import agency.highlysuspect.quatlib.any.util.IndentStringBuilder;

public class HalfDecentConfigWriter extends SnWriter {
	public String writeTopLevel(ConfigSection section, ConfigState state) {
		IndentStringBuilder out = new IndentStringBuilder();
		acceptRootSection(section, state, out);
		return out.toString();
	}
	
	public void accept(SectOrOpt item, ConfigState state, IndentStringBuilder out) {
		if(item instanceof ConfigSection section) acceptSection(section, state, out);
		else if(item instanceof ConfigOpt<?> opt) acceptOption(opt, state, out);
	}
	
	protected void comment(SectOrOpt item, IndentStringBuilder out) {
		for(String comment : item.getComment()) {
			out.append("% " + comment);
			out.newline();
		}
	}
	
	public void acceptRootSection(ConfigSection section, ConfigState state, IndentStringBuilder out) {
		comment(section, out);
		out.newline();
		
		for(SectOrOpt item : section.getChildren()) {
			accept(item, state, out);
			out.newline().newline();
		}
		
		out.backspace();
	}
	
	public void acceptSection(ConfigSection section, ConfigState state, IndentStringBuilder out) {
		comment(section, out);
		
		out.append(escapeAndQuoteIfNeeded(section.getName()))
			.append(" = {")
			.newline()
			.increaseIndent();
		
		for(SectOrOpt item : section.getChildren()) {
			accept(item, state, out);
			out.newline().newline(); //blank line
		}
		
		out.backspace().backspace(); //rm last blank line
		
		out.decreaseIndent().newline().append("}");
	}
	
	public <T> void acceptOption(ConfigOpt<T> opt, ConfigState state, IndentStringBuilder out) {
		comment(opt, out);
		
		out.append(escapeAndQuoteIfNeeded(opt.getName())).append(" = ");
		
		T item = state.get(opt);
		Sn<?> sn = opt.write(item);
		accept(sn, out); //use the regular sn writer
	}
}

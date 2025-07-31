package agency.highlysuspect.quatlib.craftless.config.hdc;

import agency.highlysuspect.quatlib.craftless.config.ConfigOpt;
import agency.highlysuspect.quatlib.craftless.config.ConfigSection;
import agency.highlysuspect.quatlib.craftless.config.ReadableConfig;
import agency.highlysuspect.quatlib.craftless.config.SectOrOpt;
import agency.highlysuspect.quatlib.craftless.config.sn.Sn;
import agency.highlysuspect.quatlib.craftless.config.sn.SnWriter;
import agency.highlysuspect.quatlib.craftless.util.IndentStringBuilder;

public class HalfDecentConfigWriter extends SnWriter {
	public String writeTopLevel(ConfigSection section, ReadableConfig state) {
		IndentStringBuilder out = new IndentStringBuilder();
		acceptRootSection(section, state, out);
		return out.toString();
	}
	
	public void accept(SectOrOpt item, ReadableConfig state, IndentStringBuilder out) {
		if(item instanceof ConfigSection section) acceptSection(section, state, out);
		else if(item instanceof ConfigOpt<?> opt) acceptOption(opt, state, out);
	}
	
	protected void comment(SectOrOpt item, IndentStringBuilder out) {
		for(String comment : item.getComment()) {
			if(!comment.isEmpty()) out.append("% " + comment);
			out.newline();
		}
	}
	
	public void acceptRootSection(ConfigSection section, ReadableConfig state, IndentStringBuilder out) {
		comment(section, out);
		out.newline();
		
		for(SectOrOpt item : section.getChildren()) {
			accept(item, state, out);
			out.newline().newline();
		}
		
		out.backspace();
	}
	
	public void acceptSection(ConfigSection section, ReadableConfig state, IndentStringBuilder out) {
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
	
	public <T> void acceptOption(ConfigOpt<T> opt, ReadableConfig state, IndentStringBuilder out) {
		comment(opt, out);
		
		out.append(escapeAndQuoteIfNeeded(opt.getName())).append(" = ");
		
		T item = state.get(opt);
		Sn<?> sn = opt.write(item);
		accept(sn, out); //use the regular sn writer
	}
}

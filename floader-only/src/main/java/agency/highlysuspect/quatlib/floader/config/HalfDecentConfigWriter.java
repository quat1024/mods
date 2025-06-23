package agency.highlysuspect.quatlib.floader.config;

import agency.highlysuspect.quatlib.any.config.ConfigOpt;
import agency.highlysuspect.quatlib.any.config.ConfigSection;
import agency.highlysuspect.quatlib.any.config.ConfigState;
import agency.highlysuspect.quatlib.any.config.SectOrOpt;

import java.util.List;

public class HalfDecentConfigWriter {
	public String write(ConfigSection root, ConfigState state) {
		StringBuilder out = new StringBuilder();
		writeImpl(out, root, state, true, 0);
		return out.toString();
	}
	
	private void indent(StringBuilder out, int indent) {
		out.append("\t".repeat(indent));
	}
	
	private void writeComment(StringBuilder out, List<String> comment, int indent) {
		for(String commentLine : comment) {
			indent(out, indent);
			if(!commentLine.trim().isEmpty()) {
				out.append("# ").append(commentLine);
			}
			out.append("\n");
		}
	}
	
	private void writeImpl(StringBuilder out, SectOrOpt item, ConfigState state, boolean root, int indent) {
		writeComment(out, item.getComment(), indent);
		
		if(item instanceof ConfigSection section) {
			//name and opening block
			if(!root) {
				indent(out, indent);
				out.append(section.getName()).append(" = {");
			}
			
			//contents
			for(SectOrOpt child : section.getChildren()) {
				writeImpl(out, child, state, false, root ? indent : indent + 1);
				out.append("\n");
			}
			
			//closing
			if(!root) {
				out.deleteCharAt(out.length() - 1); //delete the last newline
				indent(out, indent);
				out.append("}");
			}
		}
		
		if(item instanceof ConfigOpt<?> opt) {
			//name
			indent(out, indent);
			out.append(opt.getName()).append(" = ");
			
			//contents
		}
	}
}

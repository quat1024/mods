package agency.highlysuspect.quatlib.any.config.sn;

import agency.highlysuspect.quatlib.any.util.IndentStringBuilder;

public class SnWriter {
	public String write(Sn<?> sn) {
		IndentStringBuilder out = new IndentStringBuilder();
		accept(sn, out);
		return out.toString();
	}
	
	public void accept(Sn<?> sn, IndentStringBuilder out) {
		switch(sn) {
			case SnList snList -> acceptList(snList, out);
			case SnMap snMap -> acceptMap(snMap, out);
			case SnStr snStr -> acceptStr(snStr, out);
		}
	}
	
	public void acceptList(SnList list, IndentStringBuilder out) {
		//opening bracket
		out.append("[").newline().increaseIndent();
		
		//list contents
		for(Sn<?> child : list) {
			accept(child, out);
			out.newline();
		}
		
		//rm last newline
		out.backspace();
		
		//closing bracket
		out.decreaseIndent().newline().append("]");
	}
	
	public void acceptMap(SnMap map, IndentStringBuilder out) {
		//opening curly
		out.append("{").newline().increaseIndent();
		
		//map contents
		map.forEach((key, value) -> {
			out.append(escapeAndQuoteIfNeeded(key));
			out.append(" = ");
			accept(value, out);
			out.newline(); //newline
		});
		
		//rm last newline
		out.backspace();
		
		//closing curly
		out.decreaseIndent().newline().append("}");
	}
	
	public void acceptStr(SnStr str, IndentStringBuilder out) {
		out.append(escapeAndQuoteIfNeeded(str.value()));
	}
	
	/// quoting rules ///
	
	protected static boolean requireQuoting(int c) {
		return c == '\n' || c == '\t' || c == '\\' || c == '"' || c == '=' || c == '{' || c == '}' || c == '[' || c == ']';
	}
	
	protected static boolean requireEscaping(int c) {
		return c == '\n' || c == '\t' || c == '\\' || c == '"';
	}
	
	protected static boolean needsQuotes(String s) {
		return s.trim().length() != s.length() || s.chars().anyMatch(SnWriter::requireQuoting);
	}
	
	protected static String escapeAndQuote(String s) {
		StringBuilder escaped = new StringBuilder("\"");
		s.chars().forEach(c -> {
			if(requireEscaping(c)) escaped.append("\\");
			if(c == '\n') escaped.append('n');
			else if(c == '\t') escaped.append('t');
			else escaped.append((char) c);
		});
		return escaped.append("\"").toString();
	}
	
	protected static String escapeAndQuoteIfNeeded(String s) {
		if(needsQuotes(s)) return escapeAndQuote(s);
		else return s;
	}
}

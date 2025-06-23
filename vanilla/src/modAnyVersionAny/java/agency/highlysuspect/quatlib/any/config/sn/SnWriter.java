package agency.highlysuspect.quatlib.any.config.sn;

import agency.highlysuspect.quatlib.any.util.IndentStringBuilder;

public class SnWriter {
	public String write(Sn<?> sn) {
		IndentStringBuilder out = new IndentStringBuilder();
		accept(sn, out);
		return out.toString();
	}
	
	public String writeTopLevel(SnMap map) {
		IndentStringBuilder out = new IndentStringBuilder();
		acceptMapContents(map, out);
		return out.toString();
	}
	
	protected void comment(Sn<?> sn, IndentStringBuilder out) {
		//no-op, see subclass
	}
	
	public void accept(Sn<?> sn, IndentStringBuilder out) {
		switch(sn) {
			case SnList snList -> acceptList(snList, out);
			case SnMap snMap -> acceptMap(snMap, out);
			case SnStr snStr -> acceptStrValue(snStr, out);
		}
	}
	
	public void acceptList(SnList list, IndentStringBuilder out) {
		//opening bracket
		out.append("[").newline().increaseIndent();
		
		//list contents
		for(Sn<?> child : list) {
			comment(child, out);
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
		acceptMapContents(map, out);
		
		//rm last blank line
		out.backspace().backspace();
		
		//closing curly
		out.decreaseIndent().newline().append("}");
	}
	
	public void acceptMapContents(SnMap map, IndentStringBuilder out) {
		//map contents
		map.forEach((key, value) -> {
			comment(value, out);
			out.append(escapeAndQuoteIfNeeded(key));
			out.append(" = ");
			accept(value, out);
			out.newline().newline(); //blank line
		});
	}
	
	public void acceptStrValue(SnStr str, IndentStringBuilder out) {
		out.append(escapeAndQuoteIfNeeded(str.value()));
	}
	
	/// quoting rules ///
	
	private static boolean requireQuoting(int c) {
		return c == '\n' || c == '\t' || c == '\\' || c == '"' || c == '=' || c == '{' || c == '}' || c == '[' || c == ']';
	}
	
	private static boolean requireEscaping(int c) {
		return c == '\n' || c == '\t' || c == '\\' || c == '"';
	}
	
	private static boolean needsQuotes(String s) {
		return s.trim().length() != s.length() || s.chars().anyMatch(SnWriter::requireQuoting);
	}
	
	private static String escapeAndQuote(String s) {
		StringBuilder escaped = new StringBuilder("\"");
		s.chars().forEach(c -> {
			if(requireEscaping(c)) escaped.append("\\");
			if(c == '\n') escaped.append('n');
			else if(c == '\t') escaped.append('t');
			else escaped.append((char) c);
		});
		return escaped.append("\"").toString();
	}
	
	private static String escapeAndQuoteIfNeeded(String s) {
		if(needsQuotes(s)) return escapeAndQuote(s);
		else return s;
	}
	
	/// commented?
	
	public static class Commented extends SnWriter {
		public Commented(ConcreteInfo concrete) {
			this.concrete = concrete;
		}
		
		protected final ConcreteInfo concrete;
		
		@Override
		protected void comment(Sn<?> sn, IndentStringBuilder out) {
			for(String comment : concrete.getComment(sn)) {
				out.append("% " + comment);
				out.newline();
			}
		}
	}
}

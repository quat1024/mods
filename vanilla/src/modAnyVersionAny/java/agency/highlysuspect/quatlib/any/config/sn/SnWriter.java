package agency.highlysuspect.quatlib.any.config.sn;

import org.jetbrains.annotations.Nullable;

public class SnWriter implements SnVisitor<RuntimeException> {
	public SnWriter() {
		concrete = null;
	}
	
	public SnWriter(@Nullable ConcreteInfo concrete) {
		this.concrete = concrete;
	}
	
	public String toString() {
		return out.toString();
	}
	
	@Nullable ConcreteInfo concrete;
	
	StringBuilder out = new StringBuilder();
	int indent = 0;
	boolean linebreak = true;
	
	private boolean requireQuoting(int c) {
		return c == '\n' || c == '\t' || c == '\\' || c == '"' || c == '=' || c == '{' || c == '}' || c == '[' || c == ']';
	}
	
	private boolean requireEscaping(int c) {
		return c == '\n' || c == '\t' || c == '\\' || c == '"';
	}
	
	private boolean needsQuotes(String s) {
		return s.chars().anyMatch(this::requireQuoting);
	}
	
	private String escapeAndQuote(String s) {
		StringBuilder escaped = new StringBuilder("\"");
		s.chars().forEach(c -> {
			if(requireEscaping(c)) escaped.append("\\");
			if(c == '\n') escaped.append('n');
			else if(c == '\t') escaped.append('t');
			else escaped.append((char) c);
		});
		return escaped.append("\"").toString();
	}
	
	private String escapeAndQuoteIfNeeded(String s) {
		if(needsQuotes(s)) return escapeAndQuote(s);
		else return s;
	}
	
	private void indentedAppend(String s) {
		if(linebreak) out.append("\t".repeat(indent));
		out.append(s);
		linebreak = false;
	}
	
	private void comment(Sn<?> node) {
		if(concrete == null) return;
		for(String s : concrete.getComment(node)) {
			if(!s.trim().isEmpty()) {
				indentedAppend("% ");
				out.append(s);
				newline();
			}
		}
	}
	
	private void newline() {
		linebreak = true;
		out.append('\n');
	}
	
	private void unBlankline() {
		if(out.length() < 3) return;
		int last = out.length() - 1;
		int prev = out.length() - 2;
		if(out.charAt(last) == '\n' && out.charAt(prev) == '\n') out.deleteCharAt(last);
	}
	
	@Override
	public void visitString(String s) {
		indentedAppend(escapeAndQuoteIfNeeded(s));
	}
	
	@Override
	public void openMap(SnMap map) {
		out.append("{");
		newline();
		indent++;
	}
	
	@Override
	public void closeMap(SnMap map) {
		indent--;
		unBlankline();
		indentedAppend("}");
	}
	
	@Override
	public void mapItem(String k, Sn<?> item) {
		comment(item);
		indentedAppend(escapeAndQuoteIfNeeded(k));
		out.append(" = ");
		item.accept(this);
		newline();
		newline();
	}
	
	@Override
	public void openList(SnList list) {
		out.append("[");
		indent++;
		newline();
	}
	
	@Override
	public void closeList(SnList list) {
		indent--;
		newline();
		indentedAppend("]");
	}
	
	@Override
	public void listItem(int i, Sn<?> item) {
		comment(item);
		item.accept(this);
		newline();
	}
}

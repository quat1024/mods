package agency.highlysuspect.quatlib.any.util;

public class IndentStringBuilder {
	public IndentStringBuilder(String indentStr, StringBuilder out) {
		this.indentStr = indentStr;
		this.out = out;
	}
	
	public IndentStringBuilder() {
		this("\t", new StringBuilder());
	}
	
	private final String indentStr;
	private final StringBuilder out;
	
	private int level = 0;
	private boolean indentAllowed = true;
	
	//don't put newlines in this string
	public IndentStringBuilder append(String s) {
		appendIndent();
		out.append(s);
		return this;
	}
	
	public IndentStringBuilder appendAll(String... strings) {
		appendIndent();
		for(String s : strings) out.append(s);
		return this;
	}
	
	public IndentStringBuilder appendOnePerLine(String... strings) {
		for(String s : strings) {
			append(s);
			newline();
		}
		return this;
	}
	
	public IndentStringBuilder appendIndent() {
		if(indentAllowed) {
			out.append(indentStr.repeat(level));
			indentAllowed = false;
		}
		return this;
	}
	
	public IndentStringBuilder newline() {
		out.append("\n");
		indentAllowed = true;
		return this;
	}
	
	public IndentStringBuilder increaseIndent() {
		level++;
		return this;
	}
	
	public IndentStringBuilder decreaseIndent() {
		level--;
		return this;
	}
	
	public IndentStringBuilder backspace() {
		out.deleteCharAt(out.length() - 1);
		return this;
	}
	
	@Override
	public String toString() {
		return out.toString();
	}
}

package agency.highlysuspect.quatlib.any.config.sn;

import agency.highlysuspect.quatlib.any.failure.CtxChain;
import agency.highlysuspect.quatlib.any.failure.FailureLogReporter;
import agency.highlysuspect.quatlib.any.failure.FailureSourceSink;
import agency.highlysuspect.quatlib.any.failure.ReportedException;
import agency.highlysuspect.quatlib.any.util.LogFacade;
import org.jetbrains.annotations.Nullable;

public class SnParser {
	public SnParser(String s) {
		this.s = s;
		this.cursor = new Cursor();
	}
	
	private static final int EOF = Integer.MIN_VALUE;
	private final String s;
	private final Cursor cursor;
	
	private static final char KV_SPLIT = '=';
	private static final char LINE_COMMENT_CHAR = '%';
	
	private static final String BARE_STRING_ENDERS = "{}[],\r\n" + KV_SPLIT + LINE_COMMENT_CHAR;
	
	//rip context-sensitive barestring parsing, too ridiculous for this world
//	private static final String BARE_KEY_ENDERS = "{[\r\n" + LINE_COMMENT_CHAR + KV_SPLIT;
//	private static final String BARE_VALUE_ENDERS_WITHIN_MAP = "}\r\n" + LINE_COMMENT_CHAR;
//	private static final String BARE_VALUE_ENDERS_WITHIN_LIST = ",]\r\n" + LINE_COMMENT_CHAR;
	
	public @Nullable SnMap tryParseTopLevel(CtxChain ctx) {
		try {
			return parseTopLevel(ctx);
		} catch (ReportedException e) {
			return null;
		}
	}
	
	public SnMap parseTopLevel(CtxChain ctx) throws ReportedException {
		//keep parsing items until we run out the file, put em in a big map
		SnMap top = new SnMap();
		while(true) {
			cursor.skipWhitespaceAndComments();
			int next = cursor.peek();
			if(next == EOF) return top;
			else parseKvInto(top, ctx);
		}
	}
	
	private void parseKvInto(SnMap map, CtxChain ctx) throws ReportedException {
		cursor.skipWhitespaceAndComments();
		String key = parseKey(ctx);
		ctx = ctx.path(key);
		
		cursor.skipWhitespaceAndComments();
		int next = cursor.peek();
		if(next == KV_SPLIT) {
			//key = value.
			cursor.right();
			
			//if `=` is the last thing on the line, it denotes an empty string.
			//anything else has to go on the same line as the equals. this is
			//kind of an awkward exception, but it exists so this
			//    foo =
			//    bar = baz
			//definitely parses as 'foo = ""' and 'bar = baz'
			cursor.skipSameLineWhitespaceAndComments();
			next = cursor.peek();
			//System.out.println("parsed key " + key + " cursor is at " + cursor + " next char is " + (char) next);
			if(next == '\r' || next == '\n') map.put(key, Sn.str(""));
			else map.put(key, parseValue(ctx));
		} else if(next == '{') {
			//a map with the initial equal-sign omitted
			map.put(key, parseMap(ctx));
		} else if(next == '[') {
			//a list with the initial equal-sign omitted
			map.put(key, parseList(ctx));
		} else if(next == EOF) {
			ctx.detail("unexpected end of file").reportWarning();
		} else {
			throw ctx.detail("unexpected character '" + (char) next + "'").reportError();
		}
	}
	
	private SnMap parseMap(CtxChain ctx) throws ReportedException {
		assert cursor.peek() == '{';
		int blockStartLine = cursor.startLine;
		cursor.right();
		
		SnMap map = new SnMap();
		
		while(true) {
			cursor.skipWhitespaceAndComments();
			int next = cursor.peek();
			
			if(next == EOF) {
				//TODO: report the warning
				ctx.detail("unclosed block starting on line " + blockStartLine).reportWarning();
				return map;
			} else if(next == ',') {
				//skip it but keep going
				cursor.right();
			} else if(next == '}') {
				//done parsing this map
				cursor.right();
				return map;
			} else {
				//parse a key-value, put it in the map, and go round again to parse another
				parseKvInto(map, ctx);
			}
		}
	}
	
	private SnList parseList(CtxChain ctx) throws ReportedException {
		assert cursor.peek() == '[';
		int listStartLine = cursor.startLine;
		cursor.right();
		
		SnList list = new SnList();
		
		while(true) {
			cursor.skipWhitespaceAndComments();
			int next = cursor.peek();
			if(next == EOF) {
				ctx.detail("unclosed list starting on line " + listStartLine).reportWarning();
				return list;
			} else if(next == ',') {
				//skip it but go again
				cursor.right();
			} else if(next == ']') {
				//done parsing this list
				cursor.right();
				return list;
			} else {
				//parse an item
				list.add(parseValue(ctx.path("[" + list.size() + "]")));
			}
		}
	}
	
	private String parseKey(CtxChain ctx) throws ReportedException {
		return cursor.peek() == '"' ? parseQuotedString(ctx) : parseBareString(ctx);
	}
	
	private Sn<?> parseValue(CtxChain ctx) throws ReportedException {
		int next = cursor.peek();
		if(next == EOF) throw ctx.detail("unexpected end of file while parsing a value, on line " + cursor.endLine).reportError();
		if(next == '"') return Sn.str(parseQuotedString(ctx));
		if(next == '{') return parseMap(ctx);
		if(next == '[') return parseList(ctx);
		return Sn.str(parseBareString(ctx));
	}
	
	private String parseBareString(CtxChain ctx) throws ReportedException {
		cursor.selectUntil(BARE_STRING_ENDERS);
		String bareStringWithWhitespace = cursor.cut();
		
		if(bareStringWithWhitespace.isEmpty()) {
			//we can end up here if we get stuck on a barestring ender in an object or array.
			//basically imagine this
			//    key = hello, world
			//where user meant to write "hello, world".
			//we will parse 'key = hello', barestring stops on the comma; try to
			//parse another key, fall into this barestring code, but we're still
			//at the same character the last barestring ended on, no good.
			//try to produce a more sensible error message in this case.
			int next = cursor.peek();
			if(next == EOF) {
				//todo figure out why this can happen lol..
				throw ctx.detail("Unexpected end of file at line " + cursor.endLine).reportError();
			} else {
				throw ctx.detail("To use the character '" + (char) next + "' in a string, it must be quoted. Line: " + cursor.endLine).reportError();
			}
		}
		
		return bareStringWithWhitespace.trim();
	}
	
	private String parseQuotedString(CtxChain ctx) throws ReportedException {
		assert cursor.peek() == '"';
		cursor.right();
		cursor.delete(); //move past opening double-quote
		
		ctx = ctx.detail("while parsing a quoted string starting on line " + cursor.endLine);
		
		StringBuilder quotedString = new StringBuilder();
		int startLine = cursor.startLine; //for error reporting
		while(true) {
			cursor.selectUntil("\\\"\n"); //select until backslash, double-quote, or newline
			int next = cursor.peek();
			if(next == EOF) {
				ctx.detail("Unexpected end-of-file. Is the string missing a closing quote?").reportWarning();
				quotedString.append(cursor.cut());
				break;
			} else if(next == '\n') {
				ctx.detail("Quoted string runs off the end of the line (note: use \\ as a line-continuation character)").reportWarning();
				quotedString.append(cursor.cut());
				break;
			}
			else if(next == '\\') {
				quotedString.append(cursor.cut()); //cut everything before backslash
				cursor.right(); //eat backslash
				
				int thingToUnescape = cursor.peek();
				cursor.right();
				cursor.delete();
				switch(thingToUnescape) {
					case EOF: throw ctx.detail("End-of-file parsing an escape sequence").reportError();
					case 'n': quotedString.append('\n');
					case 't': quotedString.append('\t');
					default: quotedString.append((char) thingToUnescape); //including \" and \<literal-newline>
				}
			} else { //the closing double-quote
				quotedString.append(cursor.cut());
				cursor.right();
				break;
			}
		}
		
		return quotedString.toString();
	}
	
	//imagine a graphical text editor where shift-arrowkeys extend the selection
	class Cursor {
		int start = 0, end = 0, startLine = 1, endLine = 1;
		
		//return the character after the selection or EOF if it falls off the end of the string
		int peek() {
			if(end < s.length()) return s.charAt(end);
			else return EOF;
		}
		
		//select one more character
		//like pressing shift-rightarrow
		void right() {
			if(peek() == '\n') endLine++;
			end++;
		}
		
		//discard the selection: move the start of the selection up to the end without returning it
		//like pressing delete
		void delete() {
			start = end;
			startLine = endLine;
		}
		
		//take a snapshot of the selection, then move the start of the selection up to the end
		//like pressing ctrl-x
		String cut() {
			String selection = s.substring(start, end);
			delete();
			return selection;
		}
		
		void skipSameLineWhitespaceAndComments() {
			selectSameLineWhitespace();
			delete();
			if(peek() == LINE_COMMENT_CHAR) {
				selectUntil("\r\n");
				delete();
			}
		}
		
		void skipWhitespaceAndComments() {
			while(true) {
				//skip over whitespace
				selectWhitespace();
				delete();
				
				//if there is a comment, skip the rest of the line and go again
				if(peek() == LINE_COMMENT_CHAR) {
					selectUntil("\r\n");
					delete();
					continue;
				}
				return;
			}
		}
		
		//select characters stopping before EOF or the first non-whitespace character
		void selectWhitespace() {
			while(true) {
				int next = peek();
				if(next == EOF) break;
				if(Character.isWhitespace(next)) {
					right();
					continue;
				}
				break;
			}
		}
		
		void selectSameLineWhitespace() {
			while(true) {
				int next = peek();
				if(next == EOF || next == '\r' || next == '\n') break;
				if(Character.isWhitespace(next)) {
					right();
					continue;
				}
				break;
			}
		}
		
		//select characters, stopping before EOF or one of the characters in this string
		//(the string is used as a shitty Set<char> basically)
		void selectUntil(String untils) {
			while(true) {
				int next = peek();
				if(next == EOF) break;
				if(untils.indexOf(next) == -1) {
					right();
					continue;
				}
				break;
			}
		}
		
		@Override
		public String toString() {
			return "cursor[" + start + "," + end + "] (lines " + startLine + "-" + endLine + "), selecting '" + s.substring(start, end) + "'";
		}
	}
	
	public static void main(String... args) {
		
		FailureSourceSink blah = new FailureLogReporter(LogFacade.Sysout.INSTANCE);
		
		String testFile = """
		% cool comemnt i've decide on
		key1 = value1
		key2
				= "value2"
				key3 {
						subkey1 = subval1 % end-of-line comment
						subkey2 = subval2
				}
				key4 = {
						subkey3 = "subval3"
						subkey4 = "subval4 addasd"
						subkey5 =
						subkey6 = askjdasd
				}
		
		letter [
			Maybe "these unquoted string rules" are too lenient?
			This file format is a little bit of a disaster, don't you think.
			Haha, anyway, just catching up.
			{
			  signed = "Your friend, quat."
			  date = Jun 24 2025
			  enclosed = [heart sticker, postcard, "an opening bracket ["
			              "and curly brace {", smashed M&M candy]
			}
		]
		
		crytyping [
		  how can i hold,,all,, these,,,, commas, ,, ,,, bro,,,,,
		]
		
		hmm [
		  outer[inner]outer[inner again, that's neat]outer
		]
		
		coolmap { with = keys, on = one, line = ? }
		
		foo = {
		  bar = "hello, world
		  list = [
		    item1, item2, item3, {
		      key = "value
				
		""";
		SnMap map = new SnParser(testFile).tryParseTopLevel(blah.detail("in MyConfigFile.txt"));
		new FlatteningSnWriter().accept(map, (k, v) -> System.out.println(k + "\n\t-> '" + v + "'"));
		System.out.println(new SnWriter().write(map));
	}
}
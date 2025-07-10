package agency.highlysuspect.quatlib.any.config.sn;

import agency.highlysuspect.quatlib.any.failure.ContextChain;
import agency.highlysuspect.quatlib.any.failure.FailureBin;
import agency.highlysuspect.quatlib.any.failure.Report2;
import agency.highlysuspect.quatlib.any.util.LogFacade;

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
	
	public SnMap parseTopLevel(ContextChain ctx) throws Report2 {
		//keep parsing items until we run out the file, put em in a big map
		SnMap top = new SnMap();
		while(true) {
			cursor.skipWhitespaceAndComments();
			int next = cursor.peek();
			if(next == EOF) return top;
			else parseKvInto(top, ctx);
		}
	}
	
	private void parseKvInto(SnMap map, ContextChain ctx) throws Report2 {
		cursor.skipWhitespaceAndComments();
		String key = parseKey(ctx);
		ctx = ctx.detail("While parsing the value for '" + key + "'");
		
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
			if(next == '\r' || next == '\n') map.put(key, Sn.str(""));
			else map.put(key, parseValue(ctx));
		} else if(next == '{') {
			//a map with the initial equal-sign omitted
			map.put(key, parseMap(ctx));
		} else if(next == '[') {
			//a list with the initial equal-sign omitted
			map.put(key, parseList(ctx));
		} else if(next == EOF) {
			//the end of the file? uh, ok, i guess we're not getting a value after all
			ctx.detail("Unexpected end-of-file on line " + cursor.endLine).addWarning();
		} else {
			throw ctx.detail("Unexpected character '" + (char) next + "'").addErrorWithException();
		}
	}
	
	private SnMap parseMap(ContextChain ctx) throws Report2 {
		ctx = ctx.detail("While parsing a curly-brace block");
		
		assert cursor.peek() == '{';
		cursor.right();
		
		SnMap map = new SnMap();
		
		while(true) {
			cursor.skipWhitespaceAndComments();
			int next = cursor.peek();
			
			if(next == EOF) {
				ctx.detail("Unexpected end-of-file on line " + cursor.endLine).addWarning();
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
				int i = map.size() + 1;
				parseKvInto(map, ctx.detail("While parsing the " + englishOrdinal(i) + " entry in the curly-brace block"));
			}
		}
	}
	
	private SnList parseList(ContextChain ctx) throws Report2 {
		ctx = ctx.detail("While parsing a square-bracketed list");
		
		assert cursor.peek() == '[';
		cursor.right();
		
		SnList list = new SnList();
		
		while(true) {
			cursor.skipWhitespaceAndComments();
			int next = cursor.peek();
			if(next == EOF) {
				ctx.detail("Unexpected end-of-file on line " + cursor.endLine).addWarning();
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
				int i = list.size() + 1;
				list.add(parseValue(ctx.detail("While parsing the " + englishOrdinal(i) + " item of the list")));
			}
		}
	}
	
	private String parseKey(ContextChain ctx) throws Report2 {
		return cursor.peek() == '"' ? parseQuotedString(ctx) : parseBareString(ctx);
	}
	
	private Sn<?> parseValue(ContextChain ctx) throws Report2 {
		int next = cursor.peek();
		if(next == EOF) throw ctx.detail("Unexpected end-of-file on line " + cursor.endLine).addErrorWithException();
		if(next == '{') return parseMap(ctx);
		if(next == '[') return parseList(ctx);
		if(next == '"') return Sn.str(parseQuotedString(ctx));
		return Sn.str(parseBareString(ctx));
	}
	
	private String parseBareString(ContextChain ctx) throws Report2 {
		ctx = ctx.detail("While parsing an unquoted string on line " + cursor.startLine);
		
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
				//todo figure out whenif this can happen lol..
				throw ctx.detail("Unexpected end-of-file").addErrorWithException();
			} else {
				throw ctx.detail("To use the character '" + (char) next + "' in a string, it must be quoted").addErrorWithException();
			}
		}
		
		return bareStringWithWhitespace.trim();
	}
	
	private String parseQuotedString(ContextChain ctx) throws Report2 {
		assert cursor.peek() == '"';
		cursor.right();
		cursor.delete(); //move past opening double-quote
		
		ctx = ctx.detail("While parsing a quoted string starting on line " + cursor.startLine);
		
		StringBuilder quotedString = new StringBuilder();
		
		while(true) {
			cursor.selectUntil("\\\"\n"); //select until backslash, double-quote, or newline
			int next = cursor.peek();
			if(next == EOF) {
				ctx.detail("Unexpected end-of-file. Is the string missing a closing quote?").addWarning();
				quotedString.append(cursor.cut());
				break;
			} else if(next == '\n') {
				//TODO: should this be a hard error? (to try and avoid messing up the rest of the file by parsing strings as data and data as strings?)
				ctx.detail("String runs off the end of the line.")
					.addSection("Note", "If you want a multiline string, use \\ as a line-continuation character.")
					.addWarning();
				quotedString.append(cursor.cut());
				break;
			} else if(next == '\\') {
				quotedString.append(cursor.cut()); //cut everything before backslash
				cursor.right(); //eat backslash
				
				int thingToUnescape = cursor.peek();
				cursor.right();
				cursor.delete();
				switch(thingToUnescape) {
					case EOF: throw ctx.detail("Unexpected end-of-file parsing an escape sequence on line " + cursor.endLine).addErrorWithException();
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
	
	private String englishOrdinal(int i) {
		return i + ordinalSuffix(i);
	}
	
	private static String ordinalSuffix(int i) {
		int mod100 = i % 100;
		if(mod100 >= 4 && mod100 <= 19) return "th";
		else return switch(i % 10) {
			case 1 -> "st";
			case 2 -> "nd";
			case 3 -> "rd";
			default -> "th";
		};
	}
	
	public static void main(String... args) throws Report2 {
		
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
			  signed = "Your friend, quat.
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
		
		coolmap { with = keys, on = one, line = ? }""";
		
		FailureBin failures = new FailureBin();
		SnMap map = new SnParser(testFile).parseTopLevel(failures.detail("While parsing the config file at sample.txt"));
		
		LogFacade log = LogFacade.Sysout.INSTANCE;
		Report2.Report2Formatter formatter = new Report2.LogFacadeReport2Formatter(log);
		failures.reportWarnings(formatter);
		failures.reportErrors(formatter);
		
		new FlatteningSnWriter().accept(map, (k, v) -> System.out.println(k + "\n\t-> '" + v + "'"));
		System.out.println(new SnWriter().write(map));
	}
}
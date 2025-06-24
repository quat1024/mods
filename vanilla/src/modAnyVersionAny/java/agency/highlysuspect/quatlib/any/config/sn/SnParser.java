package agency.highlysuspect.quatlib.any.config.sn;

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
	
	private static final String BARE_KEY_ENDERS = "{[\r\n" + LINE_COMMENT_CHAR + KV_SPLIT;
	private static final String BARE_VALUE_ENDERS_WITHIN_MAP = "}\r\n" + LINE_COMMENT_CHAR;
	private static final String BARE_VALUE_ENDERS_WITHIN_LIST = ",]\r\n" + LINE_COMMENT_CHAR;
	
	public SnMap parseTopLevel() {
		//keep parsing items until we run out the file, put em in a big map
		SnMap top = new SnMap();
		while(true) {
			cursor.skipWhitespaceAndComments();
			int next = cursor.peek();
			if(next == EOF) return top;
			else parseKvInto(top);
		}
	}
	
	private void parseKvInto(SnMap map) {
		cursor.skipWhitespaceAndComments();
		String key = parseKey();
		
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
			else map.put(key, parseValue(BARE_VALUE_ENDERS_WITHIN_MAP));
		} else if(next == '{') {
			//a map with the initial equal-sign omitted
			map.put(key, parseMap());
		} else if(next == '[') {
			//a list with the initial equal-sign omitted
			map.put(key, parseList());
		} else if(next == EOF) {
			throw new IllegalStateException("unexpected end of file after parsing key '" + key + "'");
		} else {
			throw new IllegalStateException("unexpected character '" + (char) next + "' after parsing key '" + key + "'");
		}
	}
	
	private SnMap parseMap() {
		assert cursor.peek() == '{';
		int blockStartLine = cursor.startLine;
		cursor.right();
		
		SnMap map = new SnMap();
		
		while(true) {
			cursor.skipWhitespaceAndComments();
			int next = cursor.peek();
			
			if(next == EOF) {
				//TODO: report the warning
				//throw new IllegalStateException("unclosed block starting on line " + blockStartLine);
				return map;
			} else if(next == '}') {
				//done parsing this map
				cursor.right();
				return map;
			} else {
				//parse a key-value, put it in the map, and go round again to parse another
				parseKvInto(map);
			}
		}
	}
	
	private SnList parseList() {
		assert cursor.peek() == '[';
		cursor.right();
		
		SnList list = new SnList();
		
		while(true) {
			cursor.skipWhitespaceAndComments();
			int next = cursor.peek();
			if(next == EOF) {
				//TODO: report a warning
				return list;
			} else if(next == ']') {
				//done parsing this list
				cursor.right();
				return list;
			} else {
				//parse an item
				list.add(parseValue(BARE_VALUE_ENDERS_WITHIN_LIST));
				
				//if there is a comma, skip it (n.b. list trailing comma is allowed)
				cursor.skipWhitespaceAndComments();
				next = cursor.peek();
				if(next == ',') cursor.right();
			}
		}
	}
	
	private Sn<?> parseValue(String enders) {
		int next = cursor.peek();
		if(next == EOF) throw new IllegalStateException("unexpected end of file while parsing value");
		if(next == '"') return Sn.str(parseQuotedString());
		if(next == '{') return parseMap();
		if(next == '[') return parseList();
		return Sn.str(parseBareValue(enders));
	}
	
	//parse a bare string until the end of the line, a structure closer, or an end-of-line comment
	private String parseBareValue(String enders) {
		cursor.selectUntil(enders);
		return cursor.cut().trim();
	}
	
	private String parseKey() {
		return cursor.peek() == '"' ? parseQuotedString() : parseBareKey();
	}
	
	//parse a bare string until the end of the line, an equal sign (which would start a kv),
	//an end-of-line comment, or an structure opener
	private String parseBareKey() {
		cursor.selectUntil(BARE_KEY_ENDERS);
		return cursor.cut().trim();
	}
	
	private String parseQuotedString() {
		assert cursor.peek() == '"';
		cursor.right();
		cursor.delete(); //move past opening double-quote
		
		StringBuilder quotedString = new StringBuilder();
		int startLine = cursor.startLine; //for error reporting
		while(true) {
			cursor.selectUntil("\\\"\n"); //select until backslash, double-quote, or newline
			int next = cursor.peek();
			if(next == EOF) throw new RuntimeException("non-terminated string, starting on line " + startLine);
			else if(next == '\n') throw new RuntimeException("a string runs off the end of line " + cursor.endLine + " (hint: use backslash as a line-continuation character, or use '\\n')");
			else if(next == '\\') {
				quotedString.append(cursor.cut()); //cut everything before backslash
				cursor.right(); //eat backslash
				
				int thingToUnescape = cursor.peek();
				cursor.right();
				cursor.delete();
				switch(thingToUnescape) {
					case EOF: throw new RuntimeException("non-terminated string (end-of-file parsing an escape sequence) starting on line " + startLine);
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
			  signed = Your friend, quat.
			  date = Jun 24 2025
			  enclosed = [heart sticker, postcard, an opening bracket [
			              and curly brace {, smashed M&M candy]
			}
		]
		\t
		\t""";
		
		SnMap map = new SnParser(testFile).parseTopLevel();
		new FlatteningSnWriter().accept(map, (k, v) -> System.out.println(k + "\n\t-> '" + v + "'"));
		System.out.println(new SnWriter().write(map));
		System.out.println(new SnWriter().write(new SnStr("lajdkjas\n\nlasdklasd")));
	}
}
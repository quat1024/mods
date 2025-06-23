package agency.highlysuspect.quatlib.floader.config;

import agency.highlysuspect.quatlib.any.config.sn.FlatteningSnVisitor;
import agency.highlysuspect.quatlib.any.config.sn.Sn;
import agency.highlysuspect.quatlib.any.config.sn.SnMap;
import agency.highlysuspect.quatlib.any.config.sn.SnStr;
import agency.highlysuspect.quatlib.any.config.sn.SnWriter;

public class HalfDecentConfigParser2 {
	public HalfDecentConfigParser2(String s) {
		this.s = s;
		this.cursor = new Cursor();
	}
	
	private static final int EOF = Integer.MIN_VALUE;
	private final String s;
	private final Cursor cursor;
	
	private static final char KV_SPLIT = '=';
	private static final char LINE_COMMENT_CHAR = '%';
	
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
			//key = value
			cursor.right();
			cursor.skipWhitespaceAndComments();
			map.put(key, parseValue());
		} else if(next == '{') {
			//a map with the initial equal-sign omitted?
			map.put(key, parseMap());
		} else if(next == EOF) {
			throw new IllegalStateException("unexpected end of file after parsing key '" + key + "'");
		} else {
			throw new IllegalStateException("unexpected character " + (char) next + " after parsing key '" + key + "'");
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
				//TODO: make this a warning and return the map anyway?
				throw new IllegalStateException("unclosed block starting on line " + blockStartLine);
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
	
	private Sn<?> parseValue() {
		int next = cursor.peek();
		if(next == EOF) throw new IllegalStateException("unexpected end of file while parsing value");
		if(next == '"') return Sn.str(parseQuotedString());
		if(next == '{') return parseMap();
		//todo, arrays
		return Sn.str(parseBareValue());
	}
	
	//parse a bare string until the end of the line, a structure closer, or an end-of-line comment
	private static final String BARE_VALUE_ENDERS = "}\r\n" + LINE_COMMENT_CHAR;
	private String parseBareValue() {
		cursor.selectUntil(BARE_VALUE_ENDERS);
		return cursor.cut().trim();
	}
	
	private String parseKey() {
		return cursor.peek() == '"' ? parseQuotedString() : parseBareKey();
	}
	
	//parse a bare string until the end of the line, an equal sign (which would start a kv),
	//an end-of-line comment, or an structure opener
	private static final String BARE_KEY_ENDERS = ("{\r\n" + LINE_COMMENT_CHAR) + KV_SPLIT;
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
			return "cursor[" + start + "," + end + "] (lines " + startLine + "-" + endLine + ")";
		}
	}
	
	public static void main(String... args) {
		String testFile = """
		% cool comemnt i've decide on
		key1 = value1
		key2
				=
            value2
    key3 {
      subkey1 = subval1 % end-of-line comment
      subkey2 = subval2
    }
    key4 = {
      subkey3 = "subval3"
      subkey4 = "subval4 addasd"
    }
			
			""";
		
		SnMap map = new HalfDecentConfigParser2(testFile).parseTopLevel();
		
		new FlatteningSnVisitor((k, v) -> System.out.println(k + "\n\t->" + v)).visit(map);
		
		SnWriter writer = new SnWriter();
		writer.visit(map);
		System.out.println(writer.toString());
		
		writer = new SnWriter();
		writer.visit(new SnStr("ajdadasd\n\nadsad"));
		System.out.println(writer.toString());
	}
}
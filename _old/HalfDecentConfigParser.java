package agency.highlysuspect.quatlib.floader.config;

import agency.highlysuspect.quatlib.any.util.SnocList;

import java.util.LinkedHashMap;
import java.util.Map;

public class HalfDecentConfigParser {
	public HalfDecentConfigParser(String s) {
		this.s = s;
		this.cursor = new Cursor();
		cursor.start = cursor.end = 0;
	}
	
	private static final int EOF = Integer.MIN_VALUE;
	
	private final String s;
	private final Cursor cursor;
	
	public void parseSingleItem(SnocList<String> position, Map<SnocList<String>, String> values) {
		parseItem(position, values);
		
		//check there's no gunk at the end of the string
		int end = cursor.endLine;
		skipWhitespaceAndComments();
		if(cursor.peek() != EOF) {
			//try to report a more comprehensible error message by including some of the gunk
			String errMsg = "characters";
			String k = parseKey().trim();
			if(!k.isEmpty()) errMsg = "'" + k + "'";
			throw new IllegalStateException("unexpected " + errMsg + " at end of the file, starting on line " + end);
		}
	}
	
	public void parseItem(SnocList<String> position, Map<SnocList<String>, String> values) {
		skipWhitespaceAndComments();
		String key = parseKey();
		
		skipWhitespaceAndComments();
		int next = cursor.peek();
		if(next == EOF) {
			throw new IllegalStateException("unexpected end of file after parsing key " + key);
		} else if(next == ':') {
			cursor.right();
			skipWhitespaceAndComments();
			String value = parseValue();
			values.put(position.snoc(key), value);
		} else if(next == '{') {
			int blockStartLine = cursor.startLine;
			
			cursor.right();
			SnocList<String> sub = position.snoc(key);
			
			while(true) {
				skipWhitespaceAndComments();
				next = cursor.peek();
				if(next == EOF) {
					throw new IllegalStateException("unclosed block starting on line " + blockStartLine);
				} else if(next == '}') {
					//done parsing the block
					cursor.right();
					return;
				} else {
					//recur
					parseItem(sub, values);
				}
			}
		} else {
			throw new IllegalStateException("unexpected character " + (char) next + " after parsing key " + key);
		}
	}
	
	private String parseKey() {
		return cursor.peek() == '"' ? parseQuotedString() : parseBareKey();
	}
	
	//parse a bare string until seeing a colon (which would start a kv) or an opening curly (which would start a block)
	private String parseBareKey() {
		cursor.selectUntil(":{\r\n");
		return cursor.cut().trim();
	}
	
	private String parseValue() {
		return cursor.peek() == '"' ? parseQuotedString() : parseBareValue();
	}
	
	//parse a bare string until the end of the line
	private String parseBareValue() {
		cursor.selectUntil("\r\n");
		return cursor.cut().trim();
	}
	
	private String parseQuotedString() {
		assert cursor.peek() == '"';
		cursor.right();
		cursor.delete(); //move past opening double-quote
		
		StringBuilder quotedString = new StringBuilder();
		int startLine = cursor.startLine; //for error reporting
		while(true) {
			cursor.selectUntil("\\\""); //select until backslash or double-quote
			int next = cursor.peek();
			if(next == EOF) throw new RuntimeException("non-terminated string, starting on line " + startLine);
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
	
	private void skipWhitespaceAndComments() {
		while(true) {
			//skip over whitespace
			cursor.selectWhitespace();
			cursor.delete();
			
			//if there is a comment, skip the rest of the line and go again
			if(cursor.peek() == '#') {
				cursor.selectUntil("\r\n");
				cursor.delete();
				continue;
			}
			return;
		}
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
		
		//select characters stopping before EOF or the first non-whitespace character
		void selectWhitespace() {
			while(true) {
				int next = peek();
				if(next == EOF || !Character.isWhitespace(next)) return;
				right();
			}
		}
		
		//select characters, stopping before EOF or one of the characters in this string
		//(the string is used as a shitty Set<char> basically)
		void selectUntil(String untils) {
			while(true) {
				int next = peek();
				if(next == EOF || untils.indexOf(next) != -1) return;
				right();
			}
		}
		
		@Override
		public String toString() {
			return "cursor[" + start + "," + end + "] (lines " + startLine + "-" + endLine + ")";
		}
	}
	
	public static void main(String... args) {
		String testFile = """
config {
  "key1": "value1"
  "key2"
   :
     value2
   key3       :
      value3
   this is the fourth key
   
   :
   this is the fourth value
   this is the fifth key
   
     :
     
     this is the fifth value
   
  subsection
    { key 6: " multi
       line
                 value  6 "
                 
      key 7 (uncanny): "That was \\"uncanny!\\""
}}
""";
		
		Map<SnocList<String>, String> parsed = new LinkedHashMap<>();
		new HalfDecentConfigParser(testFile).parseSingleItem(SnocList.empty(), parsed);
		
		parsed.forEach((k, v) -> System.out.println(String.join(".", k) + "\n\t-> " + v));
	}
}

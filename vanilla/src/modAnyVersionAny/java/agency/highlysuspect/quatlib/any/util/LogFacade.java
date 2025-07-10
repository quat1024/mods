package agency.highlysuspect.quatlib.any.util;

import java.io.PrintStream;
import java.util.function.Consumer;

public interface LogFacade extends Consumer<String> {
	void info(String pattern, Object... args);
	void warn(String pattern, Object... args);
	
	default void warn() {
		warn("");
	}
	
	@Override
	default void accept(String s) {
		info("{}", s);
	}
	
	class Sysout implements LogFacade {
		public static final Sysout INSTANCE = new Sysout();
		
		@Override
		public void info(String pattern, Object... args) {
			fancy(System.out, pattern, args);
		}
		
		@Override
		public void warn(String pattern, Object... args) {
			fancy(System.err, pattern, args);
		}
		
		private void simple(PrintStream out, Object message) {
			if(message instanceof Throwable t) t.printStackTrace(out);
			else out.println(message);
		}
		
		//honestly idgaf about the ability to escape the {} pattern
		private void fancy(PrintStream out, String pattern, Object[] args) {
			if(!pattern.isEmpty()) {
				int start = 0, arg = 0;
				while(true) {
					int end = pattern.indexOf("{}", start);
					if(end == -1) {
						//trailing text after last {} in the pattern
						out.println(pattern.substring(start));
						break;
					}
					//text before the {}
					out.print(pattern.substring(start, end));
					//one argument
					out.print(arg < args.length ? String.valueOf(args[arg]) : "null");
					arg++;
					//move past the {}
					start = end + 2;
				}
			}
			
			//if a Throwable is the last argument print its stack trace
			if(args != null && args.length > 0 && args[args.length - 1] instanceof Throwable t)
				t.printStackTrace(out);
		}
	}
	
	static void main(String... args) {
		LogFacade log = Sysout.INSTANCE;
		
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				for(int k = 0; k < 3; k++) {
					String pattern = "a".repeat(i) + "{}" + "c".repeat(j) + "{}" + "e".repeat(k);
					log.info(pattern, "b", "d", new Throwable(pattern));
				}
			}
		}
		
		log.info("literal log");
		log.info("hey whats up man... {}", "not much");
		log.info("{}", new Throwable("what man"));
		log.info("{} two {} four {}", "one", "three", "five", new Throwable("six"));
	}
}

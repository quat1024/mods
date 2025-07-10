package agency.highlysuspect.quatlib.any.failure;

import agency.highlysuspect.quatlib.any.util.LogFacade;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public
class LogFacadeReporter implements FailureConsumer {
	public LogFacadeReporter(LogFacade log) {
		this.log = log;
	}
	
	public final LogFacade log;
	
	@Override
	public void reportWarning(ContextChain warning) {
		log.info("Warning:");
		report(warning, log::info);
	}
	
	@Override
	public void reportError(ContextChain error) {
		log.warn("Error:");
		report(error, log::warn);
	}
	
	interface LogFunc {
		void log(String pattern, Object... args);
	}
	
	private void report(ContextChain ctx, LogFunc f) {
		List<Throwable> throwables = new ArrayList<>();
		
		while(ctx != null) {
			switch(ctx) {
				case ContextChain.StringLink s -> {
					f.log(" - {}", s.message);
					ctx = ctx.parent;
				}
				case ContextChain.PathLink s -> {
					ctx = logPath(s, f);
				}
				case ContextChain.ThrowableLink t -> {
					throwables.add(t.cause);
					f.log(" - '{}' [exception {}]", t.cause.getMessage(), throwables.size());
					ctx = ctx.parent;
				}
			}
		}
		
		for(int i = 0; i < throwables.size(); i++) {
			f.log("");
			f.log("=== Exception {} ===", i + 1, throwables.get(i));
		}
	}
	
	private ContextChain logPath(ContextChain.PathLink pathEnd, LogFunc f) {
		String bob = pathEnd.pathSegment;
		
		ContextChain ctx = pathEnd;
		while(ctx.parent instanceof ContextChain.PathLink path) {
			bob = path.pathSegment + "." + bob;
			ctx = ctx.parent;
		}
		f.log(" - at {}", bob);
		return ctx.parent;
	}
}

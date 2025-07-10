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
	public void reportWarning(CtxChain warning) {
		log.info("Warning:");
		report(warning, log::info);
	}
	
	@Override
	public void reportError(CtxChain error) {
		log.warn("Error:");
		report(error, log::warn);
	}
	
	interface LogFunc {
		void log(String pattern, Object... args);
	}
	
	private void report(CtxChain ctx, LogFunc f) {
		List<Throwable> throwables = new ArrayList<>();
		
		while(ctx != null) {
			switch(ctx) {
				case CtxChain.StringLink s -> {
					f.log(" - {}", s.message);
					ctx = ctx.getParent();
				}
				case CtxChain.PathLink s -> {
					ctx = logPath(s, f);
				}
				case CtxChain.ThrowableLink t -> {
					throwables.add(t.cause);
					f.log(" - '{}' [exception {}]", t.cause.getMessage(), throwables.size());
					ctx = ctx.getParent();
				}
				//default -> throw new IllegalStateException("unexpected CtxChain type: " + ctx);
			}
		}
		
		for(int i = 0; i < throwables.size(); i++) {
			f.log("");
			f.log("=== Exception {} ===", i + 1, throwables.get(i));
		}
	}
	
	private CtxChain logPath(CtxChain.PathLink pathEnd, LogFunc f) {
		String bob = pathEnd.pathSegment;
		
		CtxChain ctx = pathEnd;
		while(ctx.getParent() instanceof CtxChain.PathLink path) {
			bob = path.pathSegment + "." + bob;
			ctx = ctx.getParent();
		}
		f.log(" - at {}", bob);
		return ctx.getParent();
	}
}

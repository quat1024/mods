package agency.highlysuspect.quatlib.craftless.failure;

import agency.highlysuspect.quatlib.craftless.util.LogFacade;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public class FailureLogger implements FailureListener {
	public FailureLogger(LogFacade log) {
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
			if(ctx instanceof CtxChain.StringLink s) {
				f.log(" - {}", s.message);
				ctx = ctx.getParent();
			} else if(ctx instanceof CtxChain.PathLink p) {
				ctx = logPath(p, f);
			} else if(ctx instanceof CtxChain.ThrowableLink t) {
				throwables.add(t.cause);
				f.log(" - '{}: {}' [exception {}]", t.cause.getClass().getSimpleName(), t.cause.getMessage(), throwables.size());
				ctx = ctx.getParent();
			} else {
				//should be unreachable; i'm on j17 though so can't exhaustively switch over CtxChain types
				f.log(" - {}", ctx.thisSegmentString());
				ctx = ctx.getParent();
			}
		}
		
		for(int i = 0; i < throwables.size(); i++) {
			f.log(" ");
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
		f.log(" - at '{}'", bob);
		return ctx.getParent();
	}
}

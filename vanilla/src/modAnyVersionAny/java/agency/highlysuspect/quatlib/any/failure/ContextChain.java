package agency.highlysuspect.quatlib.any.failure;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed abstract class ContextChain permits ContextChain.StringLink, ContextChain.ThrowableLink  {
	//public constructor is in FailureBucket
	protected ContextChain(@Nullable ContextChain parent, @NotNull FailureBucket failures) {
		this.parent = parent;
		this.failures = failures;
	}
	
	@Nullable ContextChain parent;
	@NotNull FailureBucket failures;
	
	// creating them
	
	public ContextChain detail(String message) {
		return new StringLink(this, failures, message);
	}
	
	public ContextChain cause(Throwable cause) {
		return new ThrowableLink(this, failures, cause);
	}
	
	// reporting problems
	
	public void reportWarning() {
		failures.addWarning(this);
	}
	
	public void reportError() throws ReportedException {
		failures.addError(this);
		throw new ReportedException();
	}
	
	public void sneakyReportError() {
		failures.addError(this);
	}
	
	public static final class StringLink extends ContextChain {
		StringLink(@Nullable ContextChain parent, @NotNull FailureBucket warnings, String message) {
			super(parent, warnings);
			this.message = message;
		}
		
		public final String message;
	}
	
	public static final class ThrowableLink extends ContextChain {
		ThrowableLink(@Nullable ContextChain parent, @NotNull FailureBucket failures, Throwable cause) {
			super(parent, failures);
			this.cause = cause;
		}
		
		public final Throwable cause;
	}
}

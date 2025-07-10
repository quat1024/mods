package agency.highlysuspect.quatlib.any.failure;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed abstract class ContextChain permits ContextChain.StringLink, ContextChain.ThrowableLink, ContextChain.PathLink {
	//public constructor is in FailureBucket
	protected ContextChain(@Nullable ContextChain parent, @NotNull FailureConsumer failures) {
		this.parent = parent;
		this.failures = failures;
	}
	
	@Nullable ContextChain parent;
	@NotNull FailureConsumer failures;
	
	protected abstract String toMyString();
	
	// creating them
	
	public ContextChain detail(String message) {
		return new StringLink(this, failures, message);
	}
	
	public ContextChain cause(Throwable cause) {
		return new ThrowableLink(this, failures, cause);
	}
	
	public ContextChain path(String pathSegment) {
		return new PathLink(this, failures, pathSegment);
	}
	
	// reporting problems
	
	public void reportWarning() {
		failures.reportWarning(this);
	}
	
	public ReportedException reportError() {
		failures.reportError(this);
		return new ReportedException();
	}
	
	public void sneakyReportError() {
		failures.reportError(this);
	}
	
	//yeah
	
	@Override
	public String toString() {
		if(parent == null) return toMyString();
		else return parent + ", " + toMyString();
	}
	
	public static final class StringLink extends ContextChain {
		StringLink(@Nullable ContextChain parent, @NotNull FailureConsumer warnings, String message) {
			super(parent, warnings);
			this.message = message;
		}
		
		public final String message;
		
		@Override
		protected String toMyString() {
			return message;
		}
	}
	
	public static final class ThrowableLink extends ContextChain {
		ThrowableLink(@Nullable ContextChain parent, @NotNull FailureConsumer failures, Throwable cause) {
			super(parent, failures);
			this.cause = cause;
		}
		
		public final Throwable cause;
		
		@Override
		protected String toMyString() {
			return cause.getMessage();
		}
	}
	
	public static final class PathLink extends ContextChain {
		PathLink(@Nullable ContextChain parent, @NotNull FailureConsumer failures, String pathSegment) {
			super(parent, failures);
			this.pathSegment = pathSegment;
		}
		
		public final String pathSegment;
		
		@Override
		protected String toMyString() {
			return pathSegment;
		}
		
		@Override
		public String toString() {
			if(parent instanceof PathLink otherLink) return otherLink + "." + toMyString();
			else return super.toString();
		}
	}
}

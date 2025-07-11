package agency.highlysuspect.quatlib.craftless.failure;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed interface CtxChain permits CtxChain.StringLink, CtxChain.ThrowableLink, CtxChain.PathLink, CtxChain.Base {
	@Nullable CtxChain getParent();
	
	//making more links in the chain
	CtxChain detail(String message);
	CtxChain cause(Throwable cause);
	CtxChain path(String pathSegment);
	
	//reporting problems
	void reportWarning();
	ReportedException reportError();
	
	@SuppressWarnings("ThrowableNotThrown")
	default void sneakyReportError() {
		reportError();
	}
	
	@SuppressWarnings("ThrowableNotThrown")
	default RuntimeException uncheckedReportError() {
		reportError();
		if(this instanceof ThrowableLink t) return new RuntimeException(t.cause.getMessage(), t.cause);
		else return new RuntimeException(thisSegmentString());
	}
	
	String thisSegmentString();
	
	//create a CtxChain through FailureRoot, not here
	
	sealed abstract class Base implements CtxChain permits StringLink, ThrowableLink, PathLink {
		protected Base(@Nullable CtxChain parent, @NotNull FailureRoot root) {
			this.parent = parent;
			this.root = root;
		}
		
		@Nullable CtxChain parent;
		@NotNull protected FailureRoot root;
		
		// creating them
		
		public CtxChain detail(String message) {
			return new CtxChain.StringLink(this, root, message);
		}
		
		public CtxChain cause(Throwable cause) {
			return new CtxChain.ThrowableLink(this, root, cause);
		}
		
		public CtxChain path(String pathSegment) {
			return new CtxChain.PathLink(this, root, pathSegment);
		}
		
		// reporting problems
		
		public void reportWarning() {
			root.reportWarning(this);
		}
		
		public ReportedException reportError() {
			root.reportError(this);
			return new ReportedException();
		}
		
		//yeah
		
		@Override
		public @Nullable CtxChain getParent() {
			return parent;
		}
		
		@Override
		public String toString() {
			if(parent == null) return thisSegmentString();
			else return parent + ", " + thisSegmentString();
		}
	}
	
	final class StringLink extends Base implements CtxChain {
		StringLink(@Nullable CtxChain parent, @NotNull FailureRoot root, String message) {
			super(parent, root);
			this.message = message;
		}
		
		public final String message;
		
		@Override
		public String thisSegmentString() {
			return message;
		}
	}
	
	final class ThrowableLink extends Base implements CtxChain {
		ThrowableLink(@Nullable CtxChain parent, @NotNull FailureRoot root, Throwable cause) {
			super(parent, root);
			this.cause = cause;
		}
		
		public final Throwable cause;
		
		@Override
		public String thisSegmentString() {
			return cause.getMessage();
		}
	}
	
	//context segment representing like, "a path inside a structured file"
	//differs from StringLink in the way that they are formatted by error reporters (and toString)
	//adjacent pathlinks will get glued together into a string
	final class PathLink extends Base implements CtxChain {
		PathLink(@Nullable CtxChain parent, @NotNull FailureRoot root, String pathSegment) {
			super(parent, root);
			this.pathSegment = pathSegment;
		}
		
		public final String pathSegment;
		
		@Override
		public String thisSegmentString() {
			return pathSegment;
		}
		
		@Override
		public String toString() {
			if(getParent() instanceof CtxChain.PathLink otherLink) return otherLink + "." + pathSegment;
			else return super.toString();
		}
	}
}

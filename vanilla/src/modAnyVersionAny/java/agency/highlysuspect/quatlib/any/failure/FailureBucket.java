package agency.highlysuspect.quatlib.any.failure;

import java.util.ArrayList;
import java.util.List;

public class FailureBucket implements FailureConsumer {
	List<CtxChain> warnings = new ArrayList<>(4);
	List<CtxChain> errors = new ArrayList<>(2);
	
	public CtxChain detail(String message) {
		return new CtxChain.StringLink(null, this, message);
	}
	
	public void reportWarning(CtxChain warning) {
		warnings.add(warning);
	}
	
	public void reportError(CtxChain error) {
		errors.add(error);
	}
	
	//TODO, for convenience pretty much
	public static class Reporting extends FailureBucket {
		public Reporting(FailureConsumer log) {
			this.log = log;
		}
		
		FailureConsumer log;
		
		@Override
		public void reportWarning(CtxChain warning) {
			super.reportWarning(warning);
			log.reportWarning(warning);
		}
		
		@Override
		public void reportError(CtxChain error) {
			super.reportError(error);
			log.reportError(error);
		}
	}
}

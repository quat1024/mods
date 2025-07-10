package agency.highlysuspect.quatlib.any.failure;

import java.util.ArrayList;
import java.util.List;

public class FailureBucket implements FailureConsumer {
	List<ContextChain> warnings = new ArrayList<>(4);
	List<ContextChain> errors = new ArrayList<>(2);
	
	public ContextChain detail(String message) {
		return new ContextChain.StringLink(null, this, message);
	}
	
	public void reportWarning(ContextChain warning) {
		warnings.add(warning);
	}
	
	public void reportError(ContextChain error) {
		errors.add(error);
	}
	
	//TODO, for convenience pretty much
	public static class Reporting extends FailureBucket {
		public Reporting(FailureConsumer log) {
			this.log = log;
		}
		
		FailureConsumer log;
		
		@Override
		public void reportWarning(ContextChain warning) {
			super.reportWarning(warning);
			log.reportWarning(warning);
		}
		
		@Override
		public void reportError(ContextChain error) {
			super.reportError(error);
			log.reportError(error);
		}
	}
}

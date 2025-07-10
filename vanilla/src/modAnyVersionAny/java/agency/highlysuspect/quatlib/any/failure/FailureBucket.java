package agency.highlysuspect.quatlib.any.failure;

import java.util.ArrayList;
import java.util.List;

public class FailureBucket {
	List<ContextChain> warnings = new ArrayList<>(4);
	List<ContextChain> errors = new ArrayList<>(2);
	
	public ContextChain detail(String message) {
		return new ContextChain.StringLink(null, this, message);
	}
	
	public void addWarning(ContextChain warning) {
		warnings.add(warning);
	}
	
	public void addError(ContextChain error) {
		errors.add(error);
	}
}

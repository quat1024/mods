package agency.highlysuspect.quatlib.any.failure;

import java.util.ArrayList;
import java.util.List;

public class FailureBucket implements FailureSourceSink {
	List<CtxChain> warnings = new ArrayList<>(4);
	List<CtxChain> errors = new ArrayList<>(2);
	
	@Override
	public void reportWarning(CtxChain warning) {
		warnings.add(warning);
	}
	
	@Override
	public void reportError(CtxChain error) {
		errors.add(error);
	}
}

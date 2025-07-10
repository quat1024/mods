package agency.highlysuspect.quatlib.any.failure;

import java.util.ArrayList;
import java.util.List;

//todo needs a way to get the failures. lol
public class FailureBucket implements FailureSourceSink {
	final List<CtxChain> warnings = new ArrayList<>(4);
	final List<CtxChain> errors = new ArrayList<>(2);
	
	@Override
	public void reportWarning(CtxChain warning) {
		synchronized(warnings) {
			warnings.add(warning);
		}
	}
	
	@Override
	public void reportError(CtxChain error) {
		synchronized(errors) {
			errors.add(error);
		}
	}
}

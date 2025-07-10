package agency.highlysuspect.quatlib.any.failure;

public interface FailureSourceSink {
	//source
	default CtxChain detail(String message) {
		return new CtxChain.StringLink(null, this, message);
	}
	
	//sink
	void reportWarning(CtxChain warning);
	void reportError(CtxChain error);
}

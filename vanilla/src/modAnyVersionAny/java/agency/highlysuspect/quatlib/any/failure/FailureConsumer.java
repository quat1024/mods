package agency.highlysuspect.quatlib.any.failure;

public interface FailureConsumer {
	void reportWarning(CtxChain warning);
	void reportError(CtxChain error);
}

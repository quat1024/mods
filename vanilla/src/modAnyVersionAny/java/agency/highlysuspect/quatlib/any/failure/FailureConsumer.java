package agency.highlysuspect.quatlib.any.failure;

public interface FailureConsumer {
	void reportWarning(ContextChain warning);
	void reportError(ContextChain error);
}

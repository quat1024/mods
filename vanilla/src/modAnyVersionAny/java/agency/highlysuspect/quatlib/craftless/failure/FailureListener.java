package agency.highlysuspect.quatlib.craftless.failure;

public interface FailureListener {
	void reportWarning(CtxChain warning);
	void reportError(CtxChain error);
}

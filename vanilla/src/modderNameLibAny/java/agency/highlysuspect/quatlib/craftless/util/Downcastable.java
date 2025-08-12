package agency.highlysuspect.quatlib.craftless.util;

/**
 * For fluent builder-style apis in the prescence of inheritance
 */
@SuppressWarnings("unchecked")
public interface Downcastable<D> {
	default D downcast() {
		return (D) this;
	}
}

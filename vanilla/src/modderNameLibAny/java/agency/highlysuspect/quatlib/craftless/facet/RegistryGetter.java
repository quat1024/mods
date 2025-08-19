package agency.highlysuspect.quatlib.craftless.facet;

public interface RegistryGetter {
	Reg<?> getReg(RegType<?> type) throws UnsupportedOperationException;
}

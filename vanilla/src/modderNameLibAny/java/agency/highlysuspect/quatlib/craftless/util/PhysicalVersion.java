package agency.highlysuspect.quatlib.craftless.util;

public enum PhysicalVersion {
	V1_20_1,
	V1_21_1,
	V1_21_5,
	;
	
	public final String pretty = name().substring(1).replace('_', '.');
	
	@Override
	public String toString() {
		return pretty;
	}
}

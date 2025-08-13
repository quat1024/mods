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
	
	public static PhysicalVersion parse(String s) {
		return PhysicalVersion.valueOf("V" + s.replace('.', '_'));
	}
	
	public boolean singularTagNames() {
		return this.compareTo(V1_21_1) >= 0; // actually 1.21.0; see https://minecraft.wiki/w/Java_Edition_24w19a#General_2
	}
}

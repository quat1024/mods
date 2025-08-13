package agency.highlysuspect.quatlib.craftless.util;

public enum PhysicalLoader {
	FABRIC,
	FORGE,
	NEOFORGE
	; //please don't make me extend this enum
	
	public boolean isFabric() {
		return this == FABRIC;
	}
	
	public boolean isOldForge() {
		return this == FORGE;
	}
	
	public boolean isNeoforge() {
		return this == NEOFORGE;
	}
	
	public boolean isForgeish() {
		return this == FORGE || this == NEOFORGE;
	}
}

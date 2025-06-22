package agency.highlysuspect.crowmap.any;

import org.jetbrains.annotations.Nullable;

public abstract class CrowmapBase {
	public CrowmapBase() {
		INST = this;
	}
	
	public static final String MODID = "crowmap";
	
	//singleton
	public static @Nullable CrowmapBase INST;
	public static CrowmapBase inst() {
		return INST;
	}
}

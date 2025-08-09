package agency.highlysuspect.crowmap.craftful;

import agency.highlysuspect.crowmap.craftless.CrowmapBase;

public abstract class CrowmapMc extends CrowmapBase {
	public static CrowmapMc inst() {
		return (CrowmapMc) CrowmapBase.INST;
	}
}

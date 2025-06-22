package agency.highlysuspect.crowmap.ver;

import agency.highlysuspect.crowmap.any.CrowmapBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Crowmap1_21_5 extends CrowmapBase {
	public Crowmap1_21_5() {
		super();
	}
	
	public final Logger LOG = LoggerFactory.getLogger(MODID);
	
	//singleton
	public static Crowmap1_21_5 inst() {
		return (Crowmap1_21_5) CrowmapBase.INST;
	}
}

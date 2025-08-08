package agency.highlysuspect.rebindnarrator.craftful;

import agency.highlysuspect.rebindnarrator.craftless.RebindNarratorBase;

public abstract class RebindNarratorMc extends RebindNarratorBase {
	public static RebindNarratorMc inst() {
		return (RebindNarratorMc) RebindNarratorBase.INST;
	}
}

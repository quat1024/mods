package agency.highlysuspect.rebindnarrator.craftless;

public abstract class RebindNarratorModBase {
	public static final String MODID = "rebind_narrator";
	
	public void init() {
		NarratorKeyPredicate.Impl.INSTANCE = makeKeyPredicate();
	}
	
	public abstract NarratorKeyPredicate makeKeyPredicate();
}

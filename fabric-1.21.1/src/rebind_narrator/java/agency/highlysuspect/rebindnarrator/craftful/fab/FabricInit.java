package agency.highlysuspect.rebindnarrator.craftful.fab;

import agency.highlysuspect.rebindnarrator.craftless.RebindNarratorImpl;
import net.fabricmc.api.ClientModInitializer;

public class FabricInit implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// ... mods like AMECS, no more useless keys ...
		
		BasicFabricImpl basic = new BasicFabricImpl();
		basic.registerKeyMapping();
		RebindNarratorImpl.IMPL = basic;
	}
}

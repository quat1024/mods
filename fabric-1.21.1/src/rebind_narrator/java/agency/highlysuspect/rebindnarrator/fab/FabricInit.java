package agency.highlysuspect.rebindnarrator.fab;

import agency.highlysuspect.rebindnarrator.any.RebindNarratorImpl;
import net.fabricmc.api.ClientModInitializer;

public class FabricInit implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// ... mods like AMECS, no more useless keys ...
		//interesting: https://www.curseforge.com/minecraft/mc-mods/amecs-reborn
		
		BasicFabricImpl basic = new BasicFabricImpl();
		basic.registerKeyMapping();
		RebindNarratorImpl.IMPL = basic;
	}
}

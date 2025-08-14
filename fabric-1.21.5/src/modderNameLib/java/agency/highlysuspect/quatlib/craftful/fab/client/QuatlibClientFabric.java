package agency.highlysuspect.quatlib.craftful.fab.client;

import agency.highlysuspect.quatlib.craftless.fab.AfterQuatlibClientInitializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class QuatlibClientFabric implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		FabricLoader.getInstance().invokeEntrypoints("modder_name_lib:after_client", AfterQuatlibClientInitializer.class, AfterQuatlibClientInitializer::onInitializeClient);
	}
}

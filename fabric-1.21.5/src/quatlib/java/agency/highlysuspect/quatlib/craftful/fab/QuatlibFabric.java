package agency.highlysuspect.quatlib.craftful.fab;

import agency.highlysuspect.quatlib.craftful.QuatlibMc;
import agency.highlysuspect.quatlib.craftless.QuatlibBase;
import agency.highlysuspect.quatlib.craftless.fab.AfterQuatlibInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class QuatlibFabric extends QuatlibMc implements ModInitializer {
	@Override
	public void onInitialize() {
		//...do stuff...
		
		FabricLoader.getInstance().invokeEntrypoints("modder_name_lib:after", AfterQuatlibInitializer.class, AfterQuatlibInitializer::onInitialize);
	}
	
	public static QuatlibFabric inst() {
		return (QuatlibFabric) QuatlibBase.INST;
	}
}

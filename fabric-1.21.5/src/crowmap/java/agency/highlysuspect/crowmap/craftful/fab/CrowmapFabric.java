package agency.highlysuspect.crowmap.craftful.fab;

import agency.highlysuspect.crowmap.craftful.Crowmap1_21_5;
import agency.highlysuspect.crowmap.craftless.CrowmapBase;
import agency.highlysuspect.quatlib.craftful.Slf4jLogFacade;
import agency.highlysuspect.quatlib.craftless.config.ConfigSection;
import agency.highlysuspect.quatlib.craftless.config.WritableConfig;
import agency.highlysuspect.quatlib.craftless.config.hdc.HalfDecentConfigFile;
import agency.highlysuspect.quatlib.craftless.util.SharedConfigFileWatcher;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.Util;

public class CrowmapFabric extends Crowmap1_21_5 implements ModInitializer {
	public CrowmapFabric() {
	}
	
	@Override
	public void onInitialize() {
		//TODO: put this in a better spot (maybe ModderNameLib needs an initializer)
		SharedConfigFileWatcher.setLog(new Slf4jLogFacade("ModderNameLib"));
		
		init();
	}
	
	@Override
	public WritableConfig makeConfig(ConfigSection schema) {
		return HalfDecentConfigFile.make(
			failures.context(),
			schema,
			FabricLoader.getInstance().getConfigDir().resolve("crowmap.txt"),
			LOG, Util.backgroundExecutor()
		);
	}
	
	public static CrowmapFabric inst() {
		return (CrowmapFabric) CrowmapBase.INST;
	}
}

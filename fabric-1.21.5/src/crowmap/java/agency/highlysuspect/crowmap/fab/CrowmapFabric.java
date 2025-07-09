package agency.highlysuspect.crowmap.fab;

import agency.highlysuspect.crowmap.any.CrowmapBase;
import agency.highlysuspect.crowmap.ver.Crowmap1_21_5;
import agency.highlysuspect.quatlib.any.config.ConfigSection;
import agency.highlysuspect.quatlib.any.config.WritableConfig;
import agency.highlysuspect.quatlib.any.config.hdc.HalfDecentConfigFile;
import agency.highlysuspect.quatlib.any.failure.Report;
import agency.highlysuspect.quatlib.any.util.SharedConfigFileWatcher;
import agency.highlysuspect.quatlib.any.ver.Slf4jLogFacade;
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
	
	//singleton
	public static CrowmapFabric inst() {
		return (CrowmapFabric) CrowmapBase.INST;
	}
	
	@Override
	public WritableConfig makeConfig(ConfigSection schema) {
		HalfDecentConfigFile cfg = new HalfDecentConfigFile(
			FabricLoader.getInstance().getConfigDir().resolve("crowmap.txt"),
			schema,
			LOG,
			Util.backgroundExecutor()
		);
		
		cfg.watchForChanges();
		
		try {
			cfg.load();
		} catch (Report e) {
			e.logTo(LOG);
		}
		
		return cfg;
	}
}

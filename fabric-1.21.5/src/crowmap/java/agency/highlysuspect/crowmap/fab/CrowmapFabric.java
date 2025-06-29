package agency.highlysuspect.crowmap.fab;

import agency.highlysuspect.crowmap.any.CrowmapBase;
import agency.highlysuspect.crowmap.any.CrowmapBaseOpts;
import agency.highlysuspect.crowmap.ver.Crowmap1_21_5;
import agency.highlysuspect.quatlib.any.config.ConfigSection;
import agency.highlysuspect.quatlib.any.config.WritableConfig;
import agency.highlysuspect.quatlib.any.failure.Report;
import agency.highlysuspect.quatlib.floader.config.AutoloadHalfDecentConfigFile;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.Util;

public class CrowmapFabric extends Crowmap1_21_5 implements ModInitializer {
	public CrowmapFabric() {
	}
	
	@Override
	public void onInitialize() {
		init();
	}
	
	//singleton
	public static CrowmapFabric inst() {
		return (CrowmapFabric) CrowmapBase.INST;
	}
	
	@Override
	public WritableConfig makeConfig(ConfigSection schema) {
		AutoloadHalfDecentConfigFile cfg = new AutoloadHalfDecentConfigFile(
			FabricLoader.getInstance().getConfigDir().resolve("crowmap.txt"),
			schema,
			LOG::info,
			Util.backgroundExecutor()
		);
		
		//TODO abstract this away
		try {
			cfg.load();
			cfg.saveLater();
		} catch (Report e) {
			//TODO actually report
			// This code probably shouldn't be here anyway
			throw new RuntimeException(e);
		}
		
		//TODO delete
		LOG.info("aawaaga");
		LOG.info("show tooltip: {}", cfg.get(CrowmapBaseOpts.SHOW_TOOLTIP));
		LOG.info("tooltip selfdestruct: {}", cfg.get(CrowmapBaseOpts.TOOLTIP_SELF_DESTRUCT));
		
		return cfg;
	}
}

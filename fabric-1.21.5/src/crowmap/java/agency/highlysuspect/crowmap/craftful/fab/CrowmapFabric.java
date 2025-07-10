package agency.highlysuspect.crowmap.craftful.fab;

import agency.highlysuspect.crowmap.craftless.CrowmapBase;
import agency.highlysuspect.crowmap.craftful.Crowmap1_21_5;
import agency.highlysuspect.quatlib.craftless.config.ConfigSection;
import agency.highlysuspect.quatlib.craftless.config.WritableConfig;
import agency.highlysuspect.quatlib.craftless.config.hdc.HalfDecentConfigFile;
import agency.highlysuspect.quatlib.craftless.failure.CtxChain;
import agency.highlysuspect.quatlib.craftless.failure.FailureLogReporter;
import agency.highlysuspect.quatlib.craftless.failure.FailureSourceSink;
import agency.highlysuspect.quatlib.craftless.util.SharedConfigFileWatcher;
import agency.highlysuspect.quatlib.craftful.Slf4jLogFacade;
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
		//TODO: push up?
		FailureSourceSink fail = new FailureLogReporter(LOG);
		CtxChain ctx = fail.detail("Crowmap's config file");
		
		HalfDecentConfigFile cfg = new HalfDecentConfigFile(
			FabricLoader.getInstance().getConfigDir().resolve("crowmap.txt"),
			schema,
			LOG,
			Util.backgroundExecutor(),
			ctx
		);
		
		cfg.watchForChanges(ctx);
		cfg.load(ctx);
		
		return cfg;
	}
}

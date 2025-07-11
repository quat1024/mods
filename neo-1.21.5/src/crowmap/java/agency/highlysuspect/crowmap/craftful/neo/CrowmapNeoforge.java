package agency.highlysuspect.crowmap.craftful.neo;

import agency.highlysuspect.crowmap.craftful.Crowmap1_21_5;
import agency.highlysuspect.crowmap.craftless.CrowmapBase;
import agency.highlysuspect.quatlib.craftful.neo.NeoBackedConfig_V1;
import agency.highlysuspect.quatlib.craftless.config.ConfigSection;
import agency.highlysuspect.quatlib.craftless.config.WritableConfig;
import agency.highlysuspect.quatlib.craftless.failure.CtxChain;
import agency.highlysuspect.quatlib.craftless.failure.FailureLogReporter;
import agency.highlysuspect.quatlib.craftless.failure.FailureSourceSink;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@Mod(CrowmapBase.MODID)
public class CrowmapNeoforge extends Crowmap1_21_5 {
	public CrowmapNeoforge(ModContainer me, IEventBus modBus) {
		this.me = me;
		this.modBus = modBus;
		
		init();
	}
	
	public final ModContainer me;
	public final IEventBus modBus;
	
	public static CrowmapNeoforge inst() {
		return (CrowmapNeoforge) CrowmapBase.INST;
	}
	
	@Override
	public WritableConfig makeConfig(ConfigSection schema) {
		//TODO: push up?
		FailureSourceSink fail = new FailureLogReporter(LOG);
		CtxChain ctx = fail.detail("Crowmap's config file");
		
//		Path configDir = Objects.requireNonNull(FMLPaths.CONFIGDIR.get());
//
//		HalfDecentConfigFile cfg = new HalfDecentConfigFile(
//			configDir.resolve("crowmap.txt"),
//			schema,
//			LOG,
//			Util.backgroundExecutor(),
//			ctx
//		);
//
//		cfg.watchForChanges(ctx);
//		cfg.load(ctx);
		
		ModConfigSpec.Builder bob = new ModConfigSpec.Builder();
		NeoBackedConfig_V1 cfg = new NeoBackedConfig_V1(ctx, schema, bob);
		ModConfigSpec spec = bob.build();
		me.registerConfig(ModConfig.Type.COMMON, spec);
		
		modBus.addListener(ModConfigEvent.Loading.class, evt -> {
			if(evt.getConfig().getSpec() == spec) cfg.onExternalChange();
		});
		modBus.addListener(ModConfigEvent.Reloading.class, evt -> {
			if(evt.getConfig().getSpec() == spec) cfg.onExternalChange();
		});
		
		return cfg;
	}
}

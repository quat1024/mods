package agency.highlysuspect.crowmap.craftful.neo;

import agency.highlysuspect.crowmap.craftful.Crowmap1_21_5;
import agency.highlysuspect.crowmap.craftless.CrowmapBase;
import agency.highlysuspect.quatlib.craftful.neo.NeoBackedConfig_V1;
import agency.highlysuspect.quatlib.craftless.config.ConfigSection;
import agency.highlysuspect.quatlib.craftless.config.WritableConfig;
import agency.highlysuspect.quatlib.craftless.failure.CtxChain;
import agency.highlysuspect.quatlib.craftless.failure.FailureLogReporter;
import agency.highlysuspect.quatlib.craftless.failure.FailureSourceSink;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

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
		
		return NeoBackedConfig_V1.make(ctx, schema, modBus, me);
	}
	
	@Mod(value = CrowmapNeoforge.MODID, dist = Dist.CLIENT)
	public static class ClientInit {
		public ClientInit(ModContainer me) {
			me.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
		}
	}
}

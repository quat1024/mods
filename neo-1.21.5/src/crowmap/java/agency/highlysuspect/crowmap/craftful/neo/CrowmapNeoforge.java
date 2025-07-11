package agency.highlysuspect.crowmap.craftful.neo;

import agency.highlysuspect.crowmap.craftful.Crowmap1_21_5;
import agency.highlysuspect.crowmap.craftless.CrowmapBase;
import agency.highlysuspect.quatlib.craftful.neo.NeoBackedConfig_V1;
import agency.highlysuspect.quatlib.craftless.config.ConfigSection;
import agency.highlysuspect.quatlib.craftless.config.WritableConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(CrowmapBase.MODID)
public class CrowmapNeoforge extends Crowmap1_21_5 {
	public CrowmapNeoforge(ModContainer modContainer, IEventBus modBus) {
		this.modContainer = modContainer;
		this.modBus = modBus;
		
		init();
	}
	
	public final ModContainer modContainer;
	public final IEventBus modBus;
	
	@Override
	public WritableConfig makeConfig(ConfigSection schema) {
		return NeoBackedConfig_V1.make(
			failures.context().detail("Crowmap's config file"),
			schema,
			modBus, modContainer
		);
	}
	
	@Mod(value = CrowmapNeoforge.MODID, dist = Dist.CLIENT)
	public static class ClientInit {
		public ClientInit(ModContainer me) {
			me.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
		}
	}
	
	public static CrowmapNeoforge inst() {
		return (CrowmapNeoforge) CrowmapBase.INST;
	}
}

package agency.highlysuspect.crowmap.craftful.frg;


import agency.highlysuspect.crowmap.craftful.CrowmapMc;
import agency.highlysuspect.crowmap.craftless.CrowmapBase;
import agency.highlysuspect.quatlib.craftful.frg.ForgeBackedConfig_V1;
import agency.highlysuspect.quatlib.craftless.config.ConfigSection;
import agency.highlysuspect.quatlib.craftless.config.WritableConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CrowmapBase.MODID)
public class CrowmapForge extends CrowmapMc {
	public CrowmapForge() {
		this.modContainer = ModLoadingContext.get().getActiveContainer();
		this.modBus = FMLJavaModLoadingContext.get().getModEventBus();
		
		init();
		
		DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClassloadGuard::doIt);
	}
	
	public final ModContainer modContainer;
	public final IEventBus modBus;
	
	@Override
	public WritableConfig makeConfig(ConfigSection schema) {
		return ForgeBackedConfig_V1.make(
			failures.context(),
			schema,
			modBus, modContainer
		);
	}
	
	public static CrowmapForge inst() {
		return (CrowmapForge) CrowmapBase.INST;
	}
	
	//epic client entrypoint
	public static class ClassloadGuard {
		public static void doIt() {
			new CrowmapForgeClient();
		}
	}
}

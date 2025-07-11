package agency.highlysuspect.crowmap.craftful.fab;

import agency.highlysuspect.crowmap.craftful.CrowmapMc;
import agency.highlysuspect.crowmap.craftless.CrowmapBase;
import agency.highlysuspect.quatlib.craftless.config.ConfigSection;
import agency.highlysuspect.quatlib.craftless.config.WritableConfig;
import agency.highlysuspect.quatlib.craftless.config.hdc.HalfDecentConfigFile;
import agency.highlysuspect.quatlib.craftless.fab.AfterQuatlibInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.Util;

public class CrowmapFabric extends CrowmapMc implements AfterQuatlibInitializer {
	@Override
	public void onInitialize() {
		init();
	}
	
	@Override
	public WritableConfig makeConfig(ConfigSection schema) {
		return HalfDecentConfigFile.make(
			failures.context(),
			schema,
			FabricLoader.getInstance().getConfigDir().resolve("crowmap.txt"),
			LOG, Util.ioPool()
		);
	}
	
	public static CrowmapFabric inst() {
		return (CrowmapFabric) CrowmapBase.INST;
	}
}

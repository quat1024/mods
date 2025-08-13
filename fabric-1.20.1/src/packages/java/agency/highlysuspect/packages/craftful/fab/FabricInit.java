package agency.highlysuspect.packages.craftful.fab;

import agency.highlysuspect.packages.craftful.Packages;
import agency.highlysuspect.packages.craftful.net.ActionPacket;
import agency.highlysuspect.quatlib.craftless.config.ConfigSection;
import agency.highlysuspect.quatlib.craftless.config.WritableConfig;
import agency.highlysuspect.quatlib.craftless.config.hdc.HalfDecentConfigFile;
import agency.highlysuspect.quatlib.craftless.fab.AfterQuatlibInitializer;
import agency.highlysuspect.quatlib.craftless.util.Season1CrummyConfigUpgrader;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.Util;

import java.nio.file.Path;

public class FabricInit extends Packages implements AfterQuatlibInitializer {
	@Override
	public void onInitialize() {
		earlySetup();
	}
	
	@Override
	public boolean isFabric() {
		return true;
	}
	
	@Override
	public void registerActionPacketHandler() {
		ServerPlayNetworking.registerGlobalReceiver(ActionPacket.LONG_ID, (server, player, handler, buf, resp) -> ActionPacket.read(buf).handle(player));
	}
	
	@Override
	public WritableConfig makeConfig(ConfigSection schema) {
		Path configDir = FabricLoader.getInstance().getConfigDir();
		Path season1ConfigFile = configDir.resolve("packages-common.cfg");
		Path season2ConfigFile = configDir.resolve("packages-common.txt");
		
		new Season1CrummyConfigUpgrader(LOG, failures.context())
			.configureForPackages()
			.upgrade(season1ConfigFile, season2ConfigFile);
		
		return HalfDecentConfigFile.make(
			failures.context(),
			schema,
			season2ConfigFile,
			LOG, Util.ioPool()
		);
	}
	
}

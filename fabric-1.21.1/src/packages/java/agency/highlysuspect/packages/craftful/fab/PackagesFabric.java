package agency.highlysuspect.packages.craftful.fab;

import agency.highlysuspect.packages.craftful.Packages;
import agency.highlysuspect.packages.craftful.fab.net.ActionPacketFabric;
import agency.highlysuspect.quatlib.craftless.config.ConfigSection;
import agency.highlysuspect.quatlib.craftless.config.WritableConfig;
import agency.highlysuspect.quatlib.craftless.config.hdc.HalfDecentConfigFile;
import agency.highlysuspect.quatlib.craftless.fab.AfterQuatlibInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.Util;

import java.nio.file.Path;

public class PackagesFabric extends Packages implements AfterQuatlibInitializer {
	@Override
	public void onInitialize() {
		earlySetup();
	}
	
	@Override
	public void registerActionPacketHandler() {
		PayloadTypeRegistry.playC2S().register(ActionPacketFabric.TYPE, ActionPacketFabric.STREAM_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(ActionPacketFabric.TYPE, (payload, ctx) -> payload.handle(ctx.player()));
	}
	
	@Override
	public WritableConfig makeConfig(ConfigSection schema) {
		Path configDir = FabricLoader.getInstance().getConfigDir();
		Path season2ConfigFile = configDir.resolve("packages-common.txt");
		
		return HalfDecentConfigFile.make(
			failures.context(),
			schema,
			season2ConfigFile,
			LOG, Util.ioPool()
		);
	}
}

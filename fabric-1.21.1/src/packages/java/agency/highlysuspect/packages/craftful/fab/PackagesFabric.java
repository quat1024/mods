package agency.highlysuspect.packages.craftful.fab;

import agency.highlysuspect.packages.craftful.Packages;
import agency.highlysuspect.packages.craftful.content.PLatches;
import agency.highlysuspect.packages.craftful.fab.net.ActionPacketFabric;
import agency.highlysuspect.packages.craftful.junk.ItemStackPackageContainer2;
import agency.highlysuspect.packages.craftful.junk.PackageRules;
import agency.highlysuspect.quatlib.craftless.config.ConfigSection;
import agency.highlysuspect.quatlib.craftless.config.WritableConfig;
import agency.highlysuspect.quatlib.craftless.config.hdc.HalfDecentConfigFile;
import agency.highlysuspect.quatlib.craftless.fab.AfterQuatlibInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.Util;

import java.nio.file.Path;

public class PackagesFabric extends Packages implements AfterQuatlibInitializer {
	@Override
	public void onInitialize() {
		earlySetup();
		
		ItemStorage.SIDED.registerForBlockEntity((be, direction) -> new PackageStorage(be),
			PLatches.BlockEntityTypes.PACKAGE.get());
		
		//TODO: some way of testing this? :)
		ItemStorage.ITEM.registerForItems((stack, cic) -> new PackageStorage(new ItemStackPackageContainer2(stack, PackageRules.DEFAULT)),
			PLatches.Items.PACKAGE.get());
		
		//the Package Crafter is a vanilla inventory and Fabric automatically wraps vanilla inventories in transfer-api stuff.
		//Nothing needs to be done for it.
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

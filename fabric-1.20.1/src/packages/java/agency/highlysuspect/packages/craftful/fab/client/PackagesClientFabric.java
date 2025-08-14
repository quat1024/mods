package agency.highlysuspect.packages.craftful.fab.client;

import agency.highlysuspect.packages.craftful.Packages;
import agency.highlysuspect.packages.craftful.client.PClientBlockEventHandlers;
import agency.highlysuspect.packages.craftful.client.PackagesClient;
import agency.highlysuspect.packages.craftful.fab.client.model.FrapiMeshPackageMakerModel;
import agency.highlysuspect.packages.craftful.fab.client.model.FrapiMeshPackageModel;
import agency.highlysuspect.packages.craftful.net.ActionPacket;
import agency.highlysuspect.packages.craftless.PackagesBase;
import agency.highlysuspect.packages.craftless.client.PackagesBaseClient;
import agency.highlysuspect.quatlib.craftless.config.ConfigSection;
import agency.highlysuspect.quatlib.craftless.config.WritableConfig;
import agency.highlysuspect.quatlib.craftless.config.hdc.HalfDecentConfigFile;
import agency.highlysuspect.quatlib.craftless.fab.AfterQuatlibClientInitializer;
import agency.highlysuspect.quatlib.craftless.util.Season1CrummyConfigUpgrader;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.Util;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;

public class PackagesClientFabric extends PackagesClient implements AfterQuatlibClientInitializer {
	private final UnbakedModel packageModel = new FrapiMeshPackageModel();
	private final UnbakedModel packageMakerModel = new FrapiMeshPackageMakerModel();
	
	@Override
	public void onInitializeClient() {
		earlySetup();
		
		//TODO: is this needed?
		onConfigReload(config);
		
		//How convenient wow, the apis just magically line up, Thats crazy
		AttackBlockCallback.EVENT.register(PClientBlockEventHandlers::onHoldLeftClick);
		UseBlockCallback.EVENT.register(PClientBlockEventHandlers::onRightClick);
	}
	
	//models
	
	@Override
	public void setupCustomModelLoaders() {
		ResourceLocation specialPackage = Packages.rl("special/package");
		ResourceLocation specialPackageMaker = Packages.rl("special/package_maker");
		ModelResourceLocation packageInventory = new ModelResourceLocation(Packages.rl("package"), "inventory");
		ModelResourceLocation packageMakerInventory = new ModelResourceLocation(Packages.rl("package_maker"), "inventory");
		
		//block models (packages:special/package)
		//note that assets/packages/models/special/package.json actually does exist, but on fabric modelresourceproviders take priority
		ModelLoadingRegistry.INSTANCE.registerResourceProvider(res -> (id, ctx) -> {
			if(id.getNamespace().isEmpty() || id.getNamespace().charAt(0) != 'p') return null; //Is This Actually Faster... who knows...
			
			if(specialPackage.equals(id)) return packageModel;
			if(specialPackageMaker.equals(id)) return packageMakerModel;
			
			return null;
		});
		
		//item models (packages:item/package#inventory)
		//for blocks, the blockstate path is hardcoded, but i can point it at whatever model i want
		//items don't have that, the item model path is hardcoded and i need this api in order to load a non-json item model
		ModelLoadingRegistry.INSTANCE.registerVariantProvider(res -> (id, ctx) -> {
			if(id.getNamespace().isEmpty() || id.getNamespace().charAt(0) != 'p') return null; //Is This Actually Faster... who knows...
			
			if(packageInventory.equals(id)) return packageModel;
			if(packageMakerInventory.equals(id)) return packageMakerModel;
			
			return null;
		});
	}
	
	//networking
	
	@Override
	public void sendActionPacket(ActionPacket packet) {
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		packet.write(buf);
		ClientPlayNetworking.send(ActionPacket.LONG_ID, buf);
	}
	
	//config
	
	@Override
	public WritableConfig makeClientConfig(ConfigSection schema) {
		Path configDir = FabricLoader.getInstance().getConfigDir();
		Path season1ConfigFile = configDir.resolve("packages-client.cfg");
		Path season2ConfigFile = configDir.resolve("packages-client.txt");
		
		new Season1CrummyConfigUpgrader(Packages.LOG, Packages.inst().failures.context())
			.configureForPackages()
			.upgrade(season1ConfigFile, season2ConfigFile);
		
		return HalfDecentConfigFile.make(
			failures.context(),
			schema,
			season2ConfigFile,
			PackagesBase.LOG, Util.ioPool()
		).addReloadHook(this::onConfigReload); // <-------- TODO this reload hook API is crap
	}
	
	public static PackagesClientFabric inst() {
		return (PackagesClientFabric) PackagesBaseClient.INST;
	}
}

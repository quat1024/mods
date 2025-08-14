package agency.highlysuspect.packages.craftful.fab.client;

import agency.highlysuspect.packages.craftful.Packages;
import agency.highlysuspect.packages.craftful.client.PClientBlockEventHandlers;
import agency.highlysuspect.packages.craftful.client.PackagesClient;
import agency.highlysuspect.packages.craftful.fab.client.model.FrapiMeshPackageMakerModel;
import agency.highlysuspect.packages.craftful.fab.client.model.FrapiMeshPackageModel;
import agency.highlysuspect.packages.craftful.fab.net.ActionPacketFabric;
import agency.highlysuspect.packages.craftful.net.ActionPacket;
import agency.highlysuspect.packages.craftless.PackagesBase;
import agency.highlysuspect.packages.craftless.client.PackagesBaseClient;
import agency.highlysuspect.quatlib.craftless.config.ConfigSection;
import agency.highlysuspect.quatlib.craftless.config.WritableConfig;
import agency.highlysuspect.quatlib.craftless.config.hdc.HalfDecentConfigFile;
import agency.highlysuspect.quatlib.craftless.fab.AfterQuatlibClientInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.Util;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;

public class PackagesClientFabric extends PackagesClient implements AfterQuatlibClientInitializer {
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
		
		ModelLoadingPlugin.register((mlcontext) -> {
			UnbakedModel packageUnbaked = new FrapiMeshPackageModel();
			UnbakedModel packageMakerUnbaked = new FrapiMeshPackageMakerModel();
			
			//Point the special model IDs at the special unbaked models
			mlcontext.resolveModel().register(ctx -> {
				if(specialPackage.equals(ctx.id())) return packageUnbaked;
				if(specialPackageMaker.equals(ctx.id())) return packageMakerUnbaked;
				return null;
			});
			
			//Ensure the special models are referenced? (probably not needed)
			mlcontext.addModels(specialPackage, specialPackageMaker);
			
			//Point the item models at them too
			//(basically blockstate files can point the block at packages:special/package,
			//but there are no "itemstates" to do the same for items, at leaast not in this version...)
			mlcontext.modifyModelBeforeBake().register(ModelModifier.OVERRIDE_PHASE, (model, ctx) -> {
				if(packageInventory.equals(ctx.topLevelId())) return packageUnbaked;
				if(packageMakerInventory.equals(ctx.topLevelId())) return packageMakerUnbaked;
				return model;
			});
		});
	}
	
	//networking
	
	@Override
	public void sendActionPacket(ActionPacket packet) {
		ClientPlayNetworking.send(new ActionPacketFabric(packet));
	}
	
	//config
	
	@Override
	public WritableConfig makeClientConfig(ConfigSection schema) {
		Path configDir = FabricLoader.getInstance().getConfigDir();
		Path season2ConfigFile = configDir.resolve("packages-client.txt");
		
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

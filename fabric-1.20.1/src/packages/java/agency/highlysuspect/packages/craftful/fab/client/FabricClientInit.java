package agency.highlysuspect.packages.craftful.fab.client;

import agency.highlysuspect.packages.craftful.Packages;
import agency.highlysuspect.packages.craftful.client.PClientBlockEventHandlers;
import agency.highlysuspect.packages.craftful.client.PackagesClient;
import agency.highlysuspect.packages.craftful.fab.client.model.FrapiMeshPackageMakerModel;
import agency.highlysuspect.packages.craftful.fab.client.model.FrapiMeshPackageModel;
import agency.highlysuspect.packages.craftful.fab.compat.frex.FrexCompat;
import agency.highlysuspect.packages.craftful.net.ActionPacket;
import agency.highlysuspect.packages.craftful.platform.RegistryHandle;
import agency.highlysuspect.packages.craftful.platform.client.MyScreenConstructor;
import agency.highlysuspect.packages.craftless.PackagesBase;
import agency.highlysuspect.packages.craftless.client.PackagesBaseClient;
import agency.highlysuspect.quatlib.craftless.config.ConfigSection;
import agency.highlysuspect.quatlib.craftless.config.WritableConfig;
import agency.highlysuspect.quatlib.craftless.config.hdc.HalfDecentConfigFile;
import agency.highlysuspect.quatlib.craftless.fab.AfterQuatlibClientInitializer;
import agency.highlysuspect.quatlib.craftless.util.Season1CrummyConfigUpgrader;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.nio.file.Path;

public class FabricClientInit extends PackagesClient implements AfterQuatlibClientInitializer {
	private final UnbakedModel packageModel = new FrapiMeshPackageModel();
	private final UnbakedModel packageMakerModel = new FrapiMeshPackageMakerModel();
	
	@Override
	public void onInitializeClient() {
		earlySetup();
		
		//TODO: is this needed?
		onConfigReload(config);
		
		FrexCompat.onInitializeClient();
		
		//How convenient wow, the apis just magically line up, Thats crazy
		AttackBlockCallback.EVENT.register(PClientBlockEventHandlers::onHoldLeftClick);
		UseBlockCallback.EVENT.register(PClientBlockEventHandlers::onRightClick);
	}
	
	@Override
	public <T extends AbstractContainerMenu, U extends Screen & MenuAccess<T>> void registerMenuScreen(RegistryHandle<MenuType<T>> type, MyScreenConstructor<T, U> cons) {
		MenuScreens.register(type.get(), cons::create);
	}
	
	@Override
	public <T extends BlockEntity> void setBlockEntityRenderer(RegistryHandle<? extends BlockEntityType<T>> type, BlockEntityRendererProvider<? super T> renderer) {
		//BlockEntityRendererRegistry.register(type.get(), renderer); //frapi deprecated
		BlockEntityRenderers.register(type.get(), renderer); //fabric-transitive-access-wideners
	}
	
	@Override
	public void setRenderType(RegistryHandle<? extends Block> block, RenderType type) {
		BlockRenderLayerMap.INSTANCE.putBlock(block.get(), type);
	}
	
	//models
	
	@Override
	public void setupCustomModelLoaders() {
		ResourceLocation specialPackage = Packages.id("special/package");
		ResourceLocation specialPackageMaker = Packages.id("special/package_maker");
		ModelResourceLocation packageInventory = new ModelResourceLocation(Packages.id("package"), "inventory");
		ModelResourceLocation packageMakerInventory = new ModelResourceLocation(Packages.id("package_maker"), "inventory");
		
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
	
	public static FabricClientInit inst() {
		return (FabricClientInit) PackagesBaseClient.INST;
	}
}

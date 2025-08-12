package agency.highlysuspect.packages.craftful.fab;

import agency.highlysuspect.packages.craftful.Packages;
import agency.highlysuspect.packages.craftful.net.ActionPacket;
import agency.highlysuspect.quatlib.craftless.facet.Latch;
import agency.highlysuspect.packages.craftful.platform.MyMenuSupplier;
import agency.highlysuspect.packages.craftful.platform.RegistryHandle;
import agency.highlysuspect.quatlib.craftless.config.ConfigSection;
import agency.highlysuspect.quatlib.craftless.config.WritableConfig;
import agency.highlysuspect.quatlib.craftless.config.hdc.HalfDecentConfigFile;
import agency.highlysuspect.quatlib.craftless.fab.AfterQuatlibInitializer;
import agency.highlysuspect.quatlib.craftless.facet.Reg;
import agency.highlysuspect.quatlib.craftless.facet.RegType;
import agency.highlysuspect.quatlib.craftless.util.Season1CrummyConfigUpgrader;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.DispenserBlock;

import java.nio.file.Path;
import java.util.function.Supplier;

public class FabricInit extends Packages implements AfterQuatlibInitializer {
	@Override
	public void onInitialize() {
		earlySetup();
	}
	
	@Override
	public boolean isFabric() {
		return true;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected <T> Reg<T> createReg(RegType<T> type) {
		if(type == RegType.BLOCKS) return (Reg<T>) new FabricReg<>(BuiltInRegistries.BLOCK.key());
		if(type == RegType.ITEMS) return (Reg<T>) new FabricReg<>(BuiltInRegistries.ITEM.key());
		if(type == RegType.CREATIVE_TABS) return (Reg<T>) new FabricReg<>(BuiltInRegistries.CREATIVE_MODE_TAB.key());
		if(type == RegType.BLOCK_ENTITY_TYPES) return (Reg<T>) new FabricReg<>(BuiltInRegistries.BLOCK_ENTITY_TYPE.key());
		throw new UnsupportedOperationException("Don't know how to register " + type);
	}
	
	@Override
	public <T> RegistryHandle<T> register(Registry<? super T> registry, ResourceLocation id, Supplier<T> thingMaker) {
		//It's safe to initialize and register the object right away on Fabric.
		T thing = thingMaker.get();
		Registry.register(registry, id, thing);
		
		//Return a handle to it.
		return new RegistryHandle.Immediate<>(thing, id);
	}
	
	@Override
	public void registerDispenserBehavior(Latch<? extends ItemLike> item, DispenseItemBehavior behavior) {
		DispenserBlock.registerBehavior(item.get(), behavior);
	}
	
	@Override
	public <T extends AbstractContainerMenu> MenuType<T> makeMenuType(MyMenuSupplier<T> supplier) {
		return new MenuType<>(supplier::create, FeatureFlagSet.of());
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

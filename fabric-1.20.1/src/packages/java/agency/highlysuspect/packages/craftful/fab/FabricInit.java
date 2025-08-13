package agency.highlysuspect.packages.craftful.fab;

import agency.highlysuspect.packages.craftful.Packages;
import agency.highlysuspect.packages.craftful.net.ActionPacket;
import agency.highlysuspect.quatlib.craftless.facet.Latch;
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
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.DispenserBlock;

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
	
	@SuppressWarnings("unchecked")
	@Override
	protected Reg<?> createReg(RegType<?> type) {
		if(type == RegType.BLOCKS) return new FabricReg<>(BuiltInRegistries.BLOCK.key());
		if(type == RegType.ITEMS) return new FabricReg<>(BuiltInRegistries.ITEM.key());
		if(type == RegType.CREATIVE_TABS) return new FabricReg<>(BuiltInRegistries.CREATIVE_MODE_TAB.key());
		if(type == RegType.BLOCK_ENTITY_TYPES) return new FabricReg<>(BuiltInRegistries.BLOCK_ENTITY_TYPE.key());
		if(type == RegType.SOUND_EVENTS) return new FabricReg<>(BuiltInRegistries.SOUND_EVENT.key());
		if(type == RegType.MENU_TYPES) return new FabricReg<>(BuiltInRegistries.MENU.key());
		throw new UnsupportedOperationException("Don't know how to register " + type);
	}
	
	@Override
	public void registerDispenserBehavior(Latch<? extends ItemLike> item, DispenseItemBehavior behavior) {
		DispenserBlock.registerBehavior(item.get(), behavior);
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

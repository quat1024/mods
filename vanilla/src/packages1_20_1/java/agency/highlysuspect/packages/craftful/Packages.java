package agency.highlysuspect.packages.craftful;

import agency.highlysuspect.packages.craftful.block.PBlockEntityTypes;
import agency.highlysuspect.packages.craftful.block.PBlocks;
import agency.highlysuspect.packages.craftful.config.ConfigSchema;
import agency.highlysuspect.packages.craftful.config.CookedConfig;
import agency.highlysuspect.packages.craftful.item.PItems;
import agency.highlysuspect.packages.craftful.junk.PDispenserBehaviors;
import agency.highlysuspect.packages.craftful.junk.PSoundEvents;
import agency.highlysuspect.packages.craftful.junk.PTags;
import agency.highlysuspect.packages.craftful.junk.SidedProxy;
import agency.highlysuspect.packages.craftful.menu.PMenuTypes;
import agency.highlysuspect.packages.craftful.platform.BlockEntityFactory;
import agency.highlysuspect.packages.craftful.platform.MyMenuSupplier;
import agency.highlysuspect.packages.craftful.platform.RegistryHandle;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public abstract class Packages {
	public static final String MODID = "packages";
	public static final Logger LOGGER = LogManager.getLogger(MODID);
	public static Packages instance;
	
	public CookedConfig config = CookedConfig.Unset.INSTANCE;
	public SidedProxy proxy = new SidedProxy(); //reset from PackagesClient
	
	public Packages() {
		if(instance != null) throw new IllegalStateException("Initializing Packages twice!");
		instance = this;
	}
	
	public void earlySetup() {
		config = commonConfigBakery().cook(PropsCommon.visit(new ConfigSchema()));
		
		PBlocks.onInitialize();
		PBlockEntityTypes.onInitialize();
		PItems.onInitialize();
		
		PDispenserBehaviors.onInitialize();
		PTags.onInitialize();
		
		PMenuTypes.onInitialize();
		registerActionPacketHandler();
		
		PSoundEvents.onInitialize();
	}
	
	public static ResourceLocation id(String path) {
		return new ResourceLocation(MODID, path);
	}
	
	public void refreshConfig() {
		config.refresh();
	}
	
	public boolean isForge() {
		return false;
	}
	
	public boolean isFabric() {
		return false;
	}
	
	public abstract <T> RegistryHandle<T> register(Registry<? super T> registry, ResourceLocation id, Supplier<T> thingMaker);
	public abstract CreativeModeTab.Builder creativeModeTabBuilder();
	public abstract void registerDispenserBehavior(RegistryHandle<? extends ItemLike> item, DispenseItemBehavior behavior);
	public abstract <T extends BlockEntity> BlockEntityType<T> makeBlockEntityType(BlockEntityFactory<T> factory, Block... blocks);
	public abstract <T extends AbstractContainerMenu> MenuType<T> makeMenuType(MyMenuSupplier<T> supplier);
	public abstract void registerActionPacketHandler();
	
	public abstract ConfigSchema.Bakery commonConfigBakery();
}

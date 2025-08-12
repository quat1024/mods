package agency.highlysuspect.packages.craftful;

import agency.highlysuspect.packages.craftful.block.PBlockEntityTypes;
import agency.highlysuspect.packages.craftful.block.PBlocks;
import agency.highlysuspect.packages.craftful.content.PackagesGen;
import agency.highlysuspect.packages.craftful.item.PItems;
import agency.highlysuspect.packages.craftful.junk.PDispenserBehaviors;
import agency.highlysuspect.packages.craftful.junk.PSoundEvents;
import agency.highlysuspect.packages.craftful.junk.PTags;
import agency.highlysuspect.packages.craftful.junk.SidedProxy;
import agency.highlysuspect.packages.craftful.menu.PMenuTypes;
import agency.highlysuspect.packages.craftful.platform.BlockEntityFactory;
import agency.highlysuspect.packages.craftful.platform.MyMenuSupplier;
import agency.highlysuspect.packages.craftful.platform.RegistryHandle;
import agency.highlysuspect.packages.craftless.PackagesBase;
import agency.highlysuspect.quatlib.craftless.config.ConfigSection;
import agency.highlysuspect.quatlib.craftless.facet.FacetBucket;
import agency.highlysuspect.quatlib.craftless.facet.FileGenner;
import agency.highlysuspect.quatlib.craftless.facet.Gen;
import agency.highlysuspect.quatlib.craftless.facet.Period;
import agency.highlysuspect.quatlib.craftless.facet.facets.LangFacet;
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

import java.util.function.Supplier;

public abstract class Packages extends PackagesBase {
	public SidedProxy proxy = new SidedProxy(); //reset from PackagesClient
	
	public void earlySetup() {
		super.earlySetup();
		
		PBlocks.onInitialize();
		PBlockEntityTypes.onInitialize();
		PItems.onInitialize();
		
		PDispenserBehaviors.onInitialize();
		PTags.onInitialize();
		
		PMenuTypes.onInitialize();
		registerActionPacketHandler();
		
		PSoundEvents.onInitialize();
		
		//TODO: gen testing
		FacetBucket facets = new FacetBucket();
		Gen.Ctx genCtx = new Gen.Ctx(facets, Period.DATAGEN);
		Gen.run(genCtx, new PackagesGen());
		
		LOG.info("fgffffffffff");
		LangFacet.handle(new FileGenner.DebugGenner(LOG), facets.removeFacets(LangFacet.class));
		LOG.info("fgffffffffff");
	}
	
	public static ResourceLocation id(String path) {
		return new ResourceLocation(MODID, path);
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
	
	@Override
	public ConfigSection visitConfigSchema(ConfigSection root) {
		return PropsCommon.visit(root);
	}
	
	public static Packages inst() {
		return (Packages) PackagesBase.INST;
	}
}

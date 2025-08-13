package agency.highlysuspect.packages.craftful;

import agency.highlysuspect.packages.craftful.content.PackagesGen;
import agency.highlysuspect.packages.craftful.junk.PDispenserBehaviors;
import agency.highlysuspect.packages.craftful.junk.PTags;
import agency.highlysuspect.packages.craftful.junk.SidedProxy;
import agency.highlysuspect.quatlib.craftless.util.MyMenuSupplier;
import agency.highlysuspect.packages.craftless.PackagesBase;
import agency.highlysuspect.quatlib.craftless.config.ConfigSection;
import agency.highlysuspect.quatlib.craftless.facet.FacetBucket;
import agency.highlysuspect.quatlib.craftless.facet.Gen;
import agency.highlysuspect.quatlib.craftless.facet.Latch;
import agency.highlysuspect.quatlib.craftless.facet.Period;
import agency.highlysuspect.quatlib.craftless.facet.facets.BlockEntityTypeFacet;
import agency.highlysuspect.quatlib.craftless.facet.facets.RegFacet;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.ItemLike;

public abstract class Packages extends PackagesBase {
	public SidedProxy proxy = new SidedProxy(); //reset from PackagesClient
	
	public void earlySetup() {
		super.earlySetup();
		
		//TODO: gen testing
		FacetBucket facets = new FacetBucket();
		Gen.Ctx genCtx = new Gen.Ctx(facets, Period.RUNTIME);
		Gen.run(genCtx, new PackagesGen());
		
		LOG.info("fgffffffffff");
		RegFacet.handle(this, facets.removeFacets(RegFacet.class));
		BlockEntityTypeFacet.handle(this, facets.removeFacets(BlockEntityTypeFacet.class));
		LOG.info("fgffffffffff");
		
		PDispenserBehaviors.onInitialize();
		PTags.onInitialize();
		
		registerActionPacketHandler();
	}
	
	public static ResourceLocation rl(String path) {
		return new ResourceLocation(MODID, path);
	}
	
	public boolean isForge() {
		return false;
	}
	
	public boolean isFabric() {
		return false;
	}
	
	public abstract void registerDispenserBehavior(Latch<? extends ItemLike> item, DispenseItemBehavior behavior);
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

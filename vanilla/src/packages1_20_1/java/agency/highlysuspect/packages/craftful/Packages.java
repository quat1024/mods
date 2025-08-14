package agency.highlysuspect.packages.craftful;

import agency.highlysuspect.packages.craftful.client.ClientProxy;
import agency.highlysuspect.packages.craftful.content.PackagesGen;
import agency.highlysuspect.packages.craftful.junk.PTags;
import agency.highlysuspect.packages.craftful.junk.SidedProxy;
import agency.highlysuspect.packages.craftless.PackagesBase;
import agency.highlysuspect.quatlib.craftless.QuatlibBase;
import agency.highlysuspect.quatlib.craftless.config.ConfigSection;
import agency.highlysuspect.quatlib.craftless.facet.FacetBucket;
import agency.highlysuspect.quatlib.craftless.facet.Gen;
import agency.highlysuspect.quatlib.craftless.facet.dgen.DgenHelper;
import agency.highlysuspect.quatlib.craftless.facet.facets.*;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public abstract class Packages extends PackagesBase {
	public SidedProxy proxy;
	
	public void earlySetup() {
		proxy = QuatlibBase.inst().side.clientServer(() -> ClientProxy::new, () -> SidedProxy::new);
		
		super.earlySetup();
		
		//TODO: gen testing
		FacetBucket facets = new FacetBucket();
		@Nullable DgenHelper dgen = QuatlibBase.inst().dgen.createHelper(MODID);
		Gen.Ctx genCtx = new Gen.Ctx(facets, dgen);
		
		LOG.info("Running PackagesGen");
		Gen.run(genCtx, new PackagesGen());
		
		LOG.info("Got {} facets", facets.size());
		RegFacet.handle(this, facets.getFacets(RegFacet.class));
		BlockEntityTypeFacet.handle(this, facets.getFacets(BlockEntityTypeFacet.class));
		DispenserBehaviorFacet.handle(facets.getFacets(DispenserBehaviorFacet.class));
		
		if(dgen != null) {
			LOG.info("Handling datagen-relevant facets");
			TagFacet.handle(dgen, facets.getFacets(TagFacet.class));
			EmiTagExclusionFacet.handle(dgen, facets.getFacets(EmiTagExclusionFacet.class));
			LangFacet.handle(dgen, facets.getFacets(LangFacet.class));
			SoundEventFacet.handle(dgen, facets.getFacets(SoundEventFacet.class));
		}
		LOG.info("Done with gens");
		
		PTags.onInitialize();
		
		registerActionPacketHandler();
	}
	
	public static ResourceLocation rl(String path) {
		return new ResourceLocation(MODID, path);
	}
	
	public abstract void registerActionPacketHandler();
	
	@Override
	public ConfigSection visitConfigSchema(ConfigSection root) {
		return PropsCommon.visit(root);
	}
	
	public static Packages inst() {
		return (Packages) PackagesBase.INST;
	}
}

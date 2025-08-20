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
		@Nullable DgenHelper dgen = QuatlibBase.inst().dgen.createHelper(MODID);
		Gen.Ctx genCtx = new Gen.Ctx(dgen);
		
		LOG.info("Running PackagesGen");
		Gen.run(genCtx, new PackagesGen());
		
		LOG.info("Building facets");
		FacetBucket bucket = new FacetBucket();
		genCtx.buildFacets(new FacetBuilder.BuildCtx(dgen), bucket);
		
		LOG.info("Got {} facets", bucket.size());
		RegFacet.handle(this, bucket.getFacets(RegFacet.class));
		BlockEntityTypeFacet.handle(this, bucket.getFacets(BlockEntityTypeFacet.class));
		DispenserBehaviorFacet.handle(bucket.getFacets(DispenserBehaviorFacet.class));
		
		if(dgen != null) {
			LOG.info("Handling datagen-relevant facets");
			TagFacet.handle(dgen, bucket.getFacets(TagFacet.class));
			EmiTagExclusionFacet.handle(dgen, bucket.getFacets(EmiTagExclusionFacet.class));
			LangFacet.handle(dgen, bucket.getFacets(LangFacet.class));
			SoundEventFacet.handle(dgen, bucket.getFacets(SoundEventFacet.class));
		}
		LOG.info("Done with gens");
		
		PTags.onInitialize();
		
		registerActionPacketHandler();
	}
	
	public static ResourceLocation rl(String path) {
		return ResourceLocation.fromNamespaceAndPath(MODID, path);
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

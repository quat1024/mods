package agency.highlysuspect.stairdown.craftful.frg;

import agency.highlysuspect.quatlib.craftful.frg.QuatlibForge;
import agency.highlysuspect.quatlib.craftless.config.ConfigSection;
import agency.highlysuspect.quatlib.craftless.config.WritableConfig;
import agency.highlysuspect.quatlib.craftless.config.hdc.HalfDecentConfigFile;
import agency.highlysuspect.quatlib.craftless.facet.FacetBucket;
import agency.highlysuspect.quatlib.craftless.facet.Gen;
import agency.highlysuspect.quatlib.craftless.facet.facets.FacetBuilder;
import agency.highlysuspect.quatlib.craftless.facet.facets.RegFacet;
import agency.highlysuspect.stairdown.craftless.StairdownBase;
import agency.highlysuspect.stairdown.craftless.Variant;
import net.minecraft.Util;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.List;

@Mod(StairdownBase.MODID)
public class StairdownForge extends StairdownBase {
	public StairdownForge() {
		this.modBus = FMLJavaModLoadingContext.get().getModEventBus();
		
		init(); //reads config
		
		Gen.Ctx ctx = new Gen.Ctx(null);
		
		//for(Variant v : config.get(StairdownOpts.VARIANTS)) {
		for(Variant v : testtest()) {
			Gen.run(ctx, new VariantGen(v));
		}
		
		FacetBucket facets = new FacetBucket();
		ctx.buildFacets(new FacetBuilder.BuildCtx(null), facets);
		
		RegFacet.handle(QuatlibForge.inst()::createReg, facets.getFacets(RegFacet.class));
	}
	
	private static List<Variant> testtest() {
		Variant a = new Variant();
		a.idPrefix = "a";
		Variant b = new Variant();
		b.idPrefix = "b";
		return List.of(a, b);
	}
	
	public final IEventBus modBus;
	
	@Override
	protected WritableConfig makeConfig(ConfigSection schema) {
		Path config = FMLPaths.CONFIGDIR.get();
		LOG.error("fddddddddd {}", config);
		return new HalfDecentConfigFile(failures.context(), schema, config.resolve("stairdown-config.txt"), LOG, Util.ioPool());
	}
}

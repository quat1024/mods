package agency.highlysuspect.packages.craftful.block;

import agency.highlysuspect.packages.craftful.Packages;
import agency.highlysuspect.quatlib.craftless.facet.Id;
import agency.highlysuspect.quatlib.craftless.facet.Latch;
import agency.highlysuspect.quatlib.craftless.facet.RegType;

public class PBlocks {
	public static Latch<PackageBlock> PACKAGE = Latch.open(RegType.BLOCKS, new Id(Packages.MODID, "package"));
	public static Latch<PackageMakerBlock> PACKAGE_MAKER = Latch.open(RegType.BLOCKS, new Id(Packages.MODID, "package_maker"));
	
	public static void onInitialize() {
//		PACKAGE = Packages.inst().register(BuiltInRegistries.BLOCK, Packages.id("package"), () -> new PackageBlock(
//			BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).strength(1f, 1f).noOcclusion().pushReaction(PushReaction.DESTROY)
//		));
//
//		PACKAGE_MAKER = Packages.inst().register(BuiltInRegistries.BLOCK, Packages.id("package_maker"), () -> new PackageMakerBlock(
//			BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).strength(1f, 1f).noOcclusion()
//		));
	}
}

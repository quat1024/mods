package agency.highlysuspect.packages.craftful.block;

import agency.highlysuspect.packages.craftful.Packages;
import agency.highlysuspect.quatlib.craftless.facet.Id;
import agency.highlysuspect.quatlib.craftless.facet.Latch;
import agency.highlysuspect.quatlib.craftless.facet.RegType;

public class PBlocks {
	public static Latch<PackageBlock> PACKAGE = Latch.open(RegType.BLOCKS, new Id(Packages.MODID, "package"));
	public static Latch<PackageMakerBlock> PACKAGE_MAKER = Latch.open(RegType.BLOCKS, new Id(Packages.MODID, "package_maker"));
}

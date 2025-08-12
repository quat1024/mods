package agency.highlysuspect.packages.craftful.item;

import agency.highlysuspect.packages.craftful.block.PBlocks;
import agency.highlysuspect.quatlib.craftless.facet.Latch;
import agency.highlysuspect.quatlib.craftless.facet.RegType;
import net.minecraft.world.item.BlockItem;

public class PItems {
	public static Latch<PackageItem> PACKAGE = Latch.open(RegType.ITEMS, PBlocks.PACKAGE.id);
	public static Latch<BlockItem> PACKAGE_MAKER = Latch.open(RegType.ITEMS, PBlocks.PACKAGE_MAKER.id);
}

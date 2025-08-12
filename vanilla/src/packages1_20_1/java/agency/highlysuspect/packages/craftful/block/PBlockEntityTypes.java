package agency.highlysuspect.packages.craftful.block;

import agency.highlysuspect.quatlib.craftless.facet.Latch;
import agency.highlysuspect.quatlib.craftless.facet.RegType;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class PBlockEntityTypes {
	public static Latch<BlockEntityType<PackageBlockEntity>> PACKAGE = Latch.open(RegType.BLOCK_ENTITY_TYPES, PBlocks.PACKAGE.id);
	public static Latch<BlockEntityType<PackageMakerBlockEntity>> PACKAGE_MAKER = Latch.open(RegType.BLOCK_ENTITY_TYPES, PBlocks.PACKAGE_MAKER.id);
}

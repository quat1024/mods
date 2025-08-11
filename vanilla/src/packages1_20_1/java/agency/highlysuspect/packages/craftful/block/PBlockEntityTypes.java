package agency.highlysuspect.packages.craftful.block;

import agency.highlysuspect.packages.craftful.Packages;
import agency.highlysuspect.packages.craftful.platform.RegistryHandle;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class PBlockEntityTypes {
	public static RegistryHandle<BlockEntityType<PackageBlockEntity>> PACKAGE;
	public static RegistryHandle<BlockEntityType<PackageMakerBlockEntity>> PACKAGE_MAKER;
	
	public static void onInitialize() {
		PACKAGE = Packages.inst().register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Packages.id("package"), () -> Packages.inst().makeBlockEntityType(PackageBlockEntity::new, PBlocks.PACKAGE.get()));
		PACKAGE_MAKER = Packages.inst().register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Packages.id("package_maker"), () -> Packages.inst().makeBlockEntityType(PackageMakerBlockEntity::new, PBlocks.PACKAGE_MAKER.get()));
	}
}

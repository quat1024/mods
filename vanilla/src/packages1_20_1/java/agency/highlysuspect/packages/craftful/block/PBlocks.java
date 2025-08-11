package agency.highlysuspect.packages.craftful.block;

import agency.highlysuspect.packages.craftful.Packages;
import agency.highlysuspect.packages.craftful.platform.RegistryHandle;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;

public class PBlocks {
	public static RegistryHandle<PackageBlock> PACKAGE;
	public static RegistryHandle<PackageMakerBlock> PACKAGE_MAKER;
	
	public static void onInitialize() {
		PACKAGE = Packages.inst().register(BuiltInRegistries.BLOCK, Packages.id("package"), () -> new PackageBlock(
			BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).strength(1f, 1f).noOcclusion().pushReaction(PushReaction.DESTROY)
		));
		
		PACKAGE_MAKER = Packages.inst().register(BuiltInRegistries.BLOCK, Packages.id("package_maker"), () -> new PackageMakerBlock(
			BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).strength(1f, 1f).noOcclusion()
		));
	}
}

package agency.highlysuspect.packages.craftful.content;

import agency.highlysuspect.packages.craftful.block.PBlocks;
import agency.highlysuspect.packages.craftful.block.PackageMakerBlock;
import agency.highlysuspect.packages.craftless.content.PackageMakerGenBase;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class PackageMakerGen extends PackageMakerGenBase {
	public PackageMakerGen() {
		super(PBlocks.PACKAGE_MAKER);
	}
	
	@Override
	public Block constructBlock() {
		return new PackageMakerBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).strength(1f, 1f).noOcclusion());
	}
}

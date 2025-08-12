package agency.highlysuspect.packages.craftful.content;

import agency.highlysuspect.packages.craftful.block.PBlocks;
import agency.highlysuspect.packages.craftful.block.PackageBlock;
import agency.highlysuspect.packages.craftless.content.PackageGenBase;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;

public class PackageGen extends PackageGenBase {
	public PackageGen() {
		super(PBlocks.PACKAGE);
	}
	
	@Override
	public Block constructBlock() {
		return new PackageBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).strength(1f, 1f).noOcclusion().pushReaction(PushReaction.DESTROY));
	}
}

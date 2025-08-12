package agency.highlysuspect.packages.craftful.content;

import agency.highlysuspect.packages.craftful.block.PackageBlock;
import agency.highlysuspect.packages.craftless.content.PackageGenBase;
import agency.highlysuspect.quatlib.craftless.facet.Latch;
import agency.highlysuspect.quatlib.craftless.facet.RegType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;

public class PackageGen extends PackageGenBase<PackageBlock> {
	public PackageGen() {
		super(LATCH);
	}
	
	public static final Latch<PackageBlock> LATCH = Latch.open(RegType.BLOCKS, ID);
	
	@Override
	public PackageBlock constructBlock() {
		return new PackageBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).strength(1f, 1f).noOcclusion().pushReaction(PushReaction.DESTROY));
	}
}

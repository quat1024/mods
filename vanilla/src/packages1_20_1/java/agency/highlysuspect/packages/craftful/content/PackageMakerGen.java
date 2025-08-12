package agency.highlysuspect.packages.craftful.content;

import agency.highlysuspect.packages.craftful.block.PBlockEntityTypes;
import agency.highlysuspect.packages.craftful.block.PBlocks;
import agency.highlysuspect.packages.craftful.block.PackageMakerBlock;
import agency.highlysuspect.packages.craftful.block.PackageMakerBlockEntity;
import agency.highlysuspect.packages.craftful.item.PItems;
import agency.highlysuspect.packages.craftless.content.PackageMakerGenBase;
import agency.highlysuspect.quatlib.craftless.util.BlockEntityFactory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class PackageMakerGen extends PackageMakerGenBase {
	public PackageMakerGen() {
		super(PBlocks.PACKAGE_MAKER, PItems.PACKAGE_MAKER, PBlockEntityTypes.PACKAGE_MAKER);
	}
	
	@Override
	public Block constructBlock() {
		return new PackageMakerBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).strength(1f, 1f).noOcclusion());
	}
	
	@Override
	protected Item constructItem() {
		return new BlockItem(PBlocks.PACKAGE_MAKER.get(), new Item.Properties());
	}
	
	@Override
	protected BlockEntityFactory<?> blockEntityFactory() {
		return PackageMakerBlockEntity::new;
	}
}

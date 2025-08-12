package agency.highlysuspect.packages.craftful.content;

import agency.highlysuspect.packages.craftful.block.PBlockEntityTypes;
import agency.highlysuspect.packages.craftful.block.PBlocks;
import agency.highlysuspect.packages.craftful.block.PackageBlock;
import agency.highlysuspect.packages.craftful.block.PackageBlockEntity;
import agency.highlysuspect.packages.craftful.item.PItems;
import agency.highlysuspect.packages.craftful.item.PackageItem;
import agency.highlysuspect.packages.craftful.junk.PSoundEvents;
import agency.highlysuspect.quatlib.craftless.facet.Gen;
import agency.highlysuspect.quatlib.craftless.util.BlockEntityFactory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;

import java.util.function.Consumer;

public class PackageGen implements PGen {
	@Override
	public void gen(Ctx ctx, Consumer<Gen> more) {
		if(ctx.isDatagen()) {
			ctx.add(enUs().block(PBlocks.PACKAGE).value("Package"));
		}
		
		if(ctx.isRuntime()) {
			ctx.reg(PBlocks.PACKAGE, this::constructBlock);
			ctx.reg(PItems.PACKAGE, this::constructItem);
			ctx.blockEntity(PBlockEntityTypes.PACKAGE).factory(blockEntityFactory()).addBlocks(PBlocks.PACKAGE);
			
			simpleSoundEvent(ctx, PSoundEvents.INSERT_ONE);
			simpleSoundEvent(ctx, PSoundEvents.TAKE_ONE);
			simpleSoundEvent(ctx, PSoundEvents.INSERT_STACK);
			simpleSoundEvent(ctx, PSoundEvents.TAKE_STACK);
			simpleSoundEvent(ctx, PSoundEvents.INSERT_ALL);
			simpleSoundEvent(ctx, PSoundEvents.TAKE_ALL);
		}
	}
	
	public Block constructBlock() {
		return new PackageBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).strength(1f, 1f).noOcclusion().pushReaction(PushReaction.DESTROY));
	}
	
	protected Item constructItem() {
		return new PackageItem(PBlocks.PACKAGE.get(), new Item.Properties());
	}
	
	protected BlockEntityFactory<?> blockEntityFactory() {
		return PackageBlockEntity::new;
	}
}

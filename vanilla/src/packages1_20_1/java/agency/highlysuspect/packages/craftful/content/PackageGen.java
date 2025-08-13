package agency.highlysuspect.packages.craftful.content;

import agency.highlysuspect.packages.craftful.block.PackageBlock;
import agency.highlysuspect.packages.craftful.block.PackageBlockEntity;
import agency.highlysuspect.packages.craftful.item.PackageItem;
import agency.highlysuspect.packages.craftful.junk.PackageDispenseBehavior;
import agency.highlysuspect.quatlib.craftless.facet.Gen;
import agency.highlysuspect.quatlib.craftless.util.BlockEntityFactory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;

import java.util.function.Consumer;

public class PackageGen implements PGen {
	@Override
	public void gen(Ctx ctx, Consumer<Gen> more) {
		if(ctx.dgen != null) {
			ctx.add(enUs().block(PLatches.Blocks.PACKAGE).value("Package"));
		}
		
		ctx.reg(PLatches.Blocks.PACKAGE, this::constructBlock);
		ctx.reg(PLatches.Items.PACKAGE, this::constructItem);
		ctx.blockEntity(PLatches.BlockEntityTypes.PACKAGE).factory(blockEntityFactory()).addBlocks(PLatches.Blocks.PACKAGE);
		
		simpleSoundEvent(ctx, PLatches.SoundEvents.INSERT_ONE);
		simpleSoundEvent(ctx, PLatches.SoundEvents.TAKE_ONE);
		simpleSoundEvent(ctx, PLatches.SoundEvents.INSERT_STACK);
		simpleSoundEvent(ctx, PLatches.SoundEvents.TAKE_STACK);
		simpleSoundEvent(ctx, PLatches.SoundEvents.INSERT_ALL);
		simpleSoundEvent(ctx, PLatches.SoundEvents.TAKE_ALL);
		
		ctx.dispenser(PLatches.Items.PACKAGE).behavior(new PackageDispenseBehavior());
	}
	
	public Block constructBlock() {
		return new PackageBlock(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.OAK_PLANKS).strength(1f, 1f).noOcclusion().pushReaction(PushReaction.DESTROY));
	}
	
	protected Item constructItem() {
		return new PackageItem(PLatches.Blocks.PACKAGE.get(), new Item.Properties());
	}
	
	protected BlockEntityFactory<?> blockEntityFactory() {
		return PackageBlockEntity::new;
	}
}

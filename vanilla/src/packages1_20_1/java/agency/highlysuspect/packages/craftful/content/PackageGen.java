package agency.highlysuspect.packages.craftful.content;

import agency.highlysuspect.packages.craftful.block.PackageBlock;
import agency.highlysuspect.packages.craftful.block.PackageBlockEntity;
import agency.highlysuspect.packages.craftful.item.PackageItem;
import agency.highlysuspect.packages.craftful.junk.PackageDispenseBehavior;
import agency.highlysuspect.quatlib.craftless.facet.Gen;
import agency.highlysuspect.quatlib.craftless.facet.facets.EmiTagExclusionFacet;
import agency.highlysuspect.quatlib.craftless.facet.facets.TagFacet;
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
		if(ctx.dgen != null) {
			ctx.add(enUs()).block(PLatches.Blocks.PACKAGE).value("Package");
			
			ctx.soundsJson(PLatches.SoundEvents.INSERT_ONE)
				.effect("block.calcite.place").volume(0.4);
			ctx.add(enUs()).sound(PLatches.SoundEvents.INSERT_ONE).value("Item inserted");
			
			ctx.soundsJson(PLatches.SoundEvents.TAKE_ONE)
				.effect("entity.item.pickup").volume(0.2);
			ctx.add(enUs()).sound(PLatches.SoundEvents.TAKE_ONE).value("Item taken");
			
			ctx.soundsJson(PLatches.SoundEvents.INSERT_STACK)
				.effect("block.calcite.place").volume(0.4).pitch(0.74);
			ctx.add(enUs()).sound(PLatches.SoundEvents.INSERT_STACK).value("Items inserted");
			
			ctx.soundsJson(PLatches.SoundEvents.TAKE_STACK)
				.effect("entity.item.pickup").volume(0.2);
			ctx.add(enUs()).sound(PLatches.SoundEvents.TAKE_STACK).value("Items taken");
			
			ctx.soundsJson(PLatches.SoundEvents.INSERT_ALL)
				.effect("block.bone_block.place").volume(0.8).pitch(0.7);
			ctx.add(enUs()).sound(PLatches.SoundEvents.INSERT_ALL).value("Many items inserted");
			
			ctx.soundsJson(PLatches.SoundEvents.TAKE_ALL)
				.effect("block.gilded_blackstone.break").volume(0.5).pitch(0.7);
			ctx.add(enUs()).sound(PLatches.SoundEvents.TAKE_ALL).value("Many items taken");
			
			ctx.add(TagFacet.mineableAxe()).value(PLatches.Blocks.PACKAGE);
			//TODO: un-hardcode tag names
			ctx.add(TagFacet.block()).tag("packages:sticky").value("minecraft:slime_block");
			ctx.add(TagFacet.block()).tag("packages:sticky").value("minecraft:honey_block");
			ctx.add(new EmiTagExclusionFacet("packages:banned_from_package"));
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
		return new PackageBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).strength(1f, 1f).noOcclusion().pushReaction(PushReaction.DESTROY));
	}
	
	protected Item constructItem() {
		return new PackageItem(PLatches.Blocks.PACKAGE.get(), new Item.Properties());
	}
	
	protected BlockEntityFactory<?> blockEntityFactory() {
		return PackageBlockEntity::new;
	}
}

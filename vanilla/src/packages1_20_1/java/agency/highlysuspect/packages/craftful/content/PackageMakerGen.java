package agency.highlysuspect.packages.craftful.content;

import agency.highlysuspect.packages.craftful.block.PackageMakerBlock;
import agency.highlysuspect.packages.craftful.block.PackageMakerBlockEntity;
import agency.highlysuspect.packages.craftful.menu.PackageMakerMenu;
import agency.highlysuspect.quatlib.craftless.QuatlibBase;
import agency.highlysuspect.quatlib.craftless.facet.Gen;
import agency.highlysuspect.quatlib.craftless.facet.facets.EmiTagExclusionFacet;
import agency.highlysuspect.quatlib.craftless.facet.facets.TagFacet;
import agency.highlysuspect.quatlib.craftless.util.BlockEntityFactory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Consumer;

public class PackageMakerGen implements PGen {
	@Override
	public void gen(Ctx ctx, Consumer<Gen> more) {
		if(ctx.dgen != null) {
			ctx.add(enUs().block(PLatches.Blocks.PACKAGE_MAKER).value("Package Crafter"));
			
			ctx.soundsJson(PLatches.SoundEvents.PACKAGE_MAKER_CRAFT)
				.effect("ui.stonecutter.take_result");
			ctx.add(enUs()).sound(PLatches.SoundEvents.PACKAGE_MAKER_CRAFT).value("Package Crafter used");
			
			ctx.add(TagFacet.mineableAxe()).value(PLatches.Blocks.PACKAGE_MAKER);
			
			//Used in crafting recipe
			ctx.add(TagFacet.item().tag("c:wooden_chests").value("minecraft:chest"));
			ctx.add(TagFacet.item().tag("c:wooden_chests").value("#forge:chests/wooden").optional(true));
			
			//TODO: un-hardcode
			ctx.add(TagFacet.item().tag("packages:banned_from_package_maker").value(PLatches.Items.PACKAGE));
			ctx.add(TagFacet.item().tag("packages:banned_from_package_maker").value(PLatches.Items.PACKAGE_MAKER));
			ctx.add(TagFacet.item().tag("packages:things_you_need_for_package_crafting").value("minecraft:copper_ingot"));
			
			ctx.add(new EmiTagExclusionFacet("packages:banned_from_package_maker"));
		}
		
		ctx.reg(PLatches.Blocks.PACKAGE_MAKER, this::constructBlock);
		ctx.reg(PLatches.Items.PACKAGE_MAKER, this::constructItem);
		ctx.blockEntity(PLatches.BlockEntityTypes.PACKAGE_MAKER).factory(blockEntityFactory()).addBlocks(PLatches.Blocks.PACKAGE_MAKER);
		
		simpleSoundEvent(ctx, PLatches.SoundEvents.PACKAGE_MAKER_CRAFT);
		
		ctx.reg(PLatches.MenuTypes.PACKAGE_MAKER, this::constructMenuType);
	}
	
	public Block constructBlock() {
		return new PackageMakerBlock(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.OAK_PLANKS).strength(1f, 1f).noOcclusion());
	}
	
	protected Item constructItem() {
		return new BlockItem(PLatches.Blocks.PACKAGE_MAKER.get(), new Item.Properties());
	}
	
	protected BlockEntityFactory<?> blockEntityFactory() {
		return PackageMakerBlockEntity::new;
	}
	
	protected MenuType<?> constructMenuType() {
		return QuatlibBase.inst().makeMenuType(PackageMakerMenu::new);
	}
}

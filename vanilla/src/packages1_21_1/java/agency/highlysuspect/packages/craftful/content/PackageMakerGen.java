package agency.highlysuspect.packages.craftful.content;

import agency.highlysuspect.packages.craftful.block.PackageMakerBlock;
import agency.highlysuspect.packages.craftful.block.PackageMakerBlockEntity;
import agency.highlysuspect.packages.craftful.menu.PackageMakerMenu;
import agency.highlysuspect.quatlib.craftless.QuatlibBase;
import agency.highlysuspect.quatlib.craftless.facet.Gen;
import agency.highlysuspect.quatlib.craftless.facet.facets.EmiTagExclusionFacetBuilder;
import agency.highlysuspect.quatlib.craftless.facet.facets.TagFacetBuilder;
import agency.highlysuspect.quatlib.craftless.util.BlockEntityFactory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Consumer;

public class PackageMakerGen implements PGen {
	@Override
	public void gen(Ctx ctx, Consumer<Gen> more) {
		ctx.lang(EN_US).block(PLatches.Blocks.PACKAGE_MAKER, "Package Crafter");
		
		ctx.sound(PLatches.SoundEvents.PACKAGE_MAKER_CRAFT)
			.effect("ui.stonecutter.take_result")
			.subtitle(EN_US, "Package Crafter used");
		
		ctx.add(TagFacetBuilder.mineableAxe()).value(PLatches.Blocks.PACKAGE_MAKER);
		
		//Used in crafting recipe
		ctx.add(TagFacetBuilder.item("c:wooden_chests").value("minecraft:chest"));
		ctx.add(TagFacetBuilder.item("c:wooden_chests").value("#forge:chests/wooden").optional(true));
		
		//TODO: un-hardcode
		ctx.add(TagFacetBuilder.item("packages:banned_from_package_maker").value(PLatches.Items.PACKAGE));
		ctx.add(TagFacetBuilder.item("packages:banned_from_package_maker").value(PLatches.Items.PACKAGE_MAKER));
		ctx.add(TagFacetBuilder.item("packages:things_you_need_for_package_crafting").value("minecraft:copper_ingot"));
		
		ctx.add(new EmiTagExclusionFacetBuilder("packages:banned_from_package_maker"));
		
		ctx.reg(PLatches.Blocks.PACKAGE_MAKER, this::constructBlock);
		ctx.reg(PLatches.Items.PACKAGE_MAKER, this::constructItem);
		ctx.blockEntity(PLatches.BlockEntityTypes.PACKAGE_MAKER).factory(blockEntityFactory()).blocks(PLatches.Blocks.PACKAGE_MAKER);
		
		ctx.reg(PLatches.MenuTypes.PACKAGE_MAKER, this::constructMenuType);
	}
	
	public Block constructBlock() {
		return new PackageMakerBlock(BlockBehaviour.Properties.ofLegacyCopy(Blocks.OAK_PLANKS).strength(1f, 1f).noOcclusion());
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

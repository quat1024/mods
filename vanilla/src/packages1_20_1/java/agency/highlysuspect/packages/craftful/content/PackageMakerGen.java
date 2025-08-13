package agency.highlysuspect.packages.craftful.content;

import agency.highlysuspect.packages.craftful.block.PBlockEntityTypes;
import agency.highlysuspect.packages.craftful.block.PBlocks;
import agency.highlysuspect.packages.craftful.block.PackageMakerBlock;
import agency.highlysuspect.packages.craftful.block.PackageMakerBlockEntity;
import agency.highlysuspect.packages.craftful.item.PItems;
import agency.highlysuspect.packages.craftful.junk.PSoundEvents;
import agency.highlysuspect.packages.craftful.menu.PMenuTypes;
import agency.highlysuspect.packages.craftful.menu.PackageMakerMenu;
import agency.highlysuspect.quatlib.craftless.QuatlibBase;
import agency.highlysuspect.quatlib.craftless.facet.Gen;
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
		if(ctx.isDatagen()) {
			ctx.add(enUs().block(PBlocks.PACKAGE_MAKER).value("Package"));
		}
		
		if(ctx.isRuntime()) {
			ctx.reg(PBlocks.PACKAGE_MAKER, this::constructBlock);
			ctx.reg(PItems.PACKAGE_MAKER, this::constructItem);
			ctx.blockEntity(PBlockEntityTypes.PACKAGE_MAKER).factory(blockEntityFactory()).addBlocks(PBlocks.PACKAGE_MAKER);
			
			simpleSoundEvent(ctx, PSoundEvents.PACKAGE_MAKER_CRAFT);
			
			ctx.reg(PMenuTypes.PACKAGE_MAKER, this::constructMenuType);
		}
	}
	
	public Block constructBlock() {
		return new PackageMakerBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).strength(1f, 1f).noOcclusion());
	}
	
	protected Item constructItem() {
		return new BlockItem(PBlocks.PACKAGE_MAKER.get(), new Item.Properties());
	}
	
	protected BlockEntityFactory<?> blockEntityFactory() {
		return PackageMakerBlockEntity::new;
	}
	
	protected MenuType<?> constructMenuType() {
		return QuatlibBase.inst().makeMenuType(PackageMakerMenu::new);
	}
}

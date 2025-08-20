package agency.highlysuspect.stairdown.craftful.frg;

import agency.highlysuspect.quatlib.craftless.facet.Gen;
import agency.highlysuspect.quatlib.craftless.facet.Id;
import agency.highlysuspect.quatlib.craftless.facet.Latch;
import agency.highlysuspect.quatlib.craftless.facet.RegType;
import agency.highlysuspect.stairdown.craftless.StairdownBase;
import agency.highlysuspect.stairdown.craftless.Variant;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Consumer;

public class VariantGen implements Gen {
	public VariantGen(Variant variant) {
		this.variant = variant;
	}
	
	public final Variant variant;
	
	@Override
	public void gen(Ctx ctx, Consumer<Gen> more) {
		//todo use the "more" argument probably
		
		if(variant.stair) {
			Latch<StairBlock> stairBlock = Latch.open(RegType.BLOCKS, new Id(StairdownBase.MODID, variant.idPrefix + "_stairs"));
			ctx.reg(stairBlock, () -> new StairBlock(Blocks.OAK_WOOD::defaultBlockState, BlockBehaviour.Properties.copy(Blocks.OAK_STAIRS)));
			
			Latch<BlockItem> stairItem = Latch.open(RegType.ITEMS, stairBlock.id);
			ctx.reg(stairItem, () -> new BlockItem(stairBlock.get(), new Item.Properties()));
		}
		
		if(variant.slab) {
			Latch<SlabBlock> slabBlock = Latch.open(RegType.BLOCKS, new Id(StairdownBase.MODID, variant.idPrefix + "_slab"));
			ctx.reg(slabBlock, () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.OAK_SLAB)));
			
			Latch<BlockItem> slabItem = Latch.open(RegType.ITEMS, slabBlock.id);
			ctx.reg(slabItem, () -> new BlockItem(slabBlock.get(), new Item.Properties()));
		}
		
		if(variant.wall) {
			Latch<SlabBlock> wallBlock = Latch.open(RegType.BLOCKS, new Id(StairdownBase.MODID, variant.idPrefix + "_wall"));
			ctx.reg(wallBlock, () -> new WallBlock(BlockBehaviour.Properties.copy(Blocks.OAK_SLAB)));
			
			Latch<BlockItem> wallItem = Latch.open(RegType.ITEMS, wallBlock.id);
			ctx.reg(wallItem, () -> new BlockItem(wallBlock.get(), new Item.Properties()));
		}
	}
}

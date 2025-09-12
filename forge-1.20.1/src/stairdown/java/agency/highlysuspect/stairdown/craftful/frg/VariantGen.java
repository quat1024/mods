package agency.highlysuspect.stairdown.craftful.frg;

import agency.highlysuspect.quatlib.craftless.facet.*;
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
		
		LatchPool latches = LatchPool.INST;
		
		if(variant.stair) {
			Latch<StairBlock> stairBlock = latches.get(RegType.BLOCKS, new Id(StairdownBase.MODID, variant.idPrefix + "_stairs"));
			ctx.reg(stairBlock, () -> new StairBlock(Blocks.OAK_WOOD::defaultBlockState, BlockBehaviour.Properties.copy(Blocks.OAK_STAIRS)));
			
			Latch<BlockItem> stairItem = latches.get(RegType.ITEMS, stairBlock.id);
			ctx.reg(stairItem, () -> new BlockItem(stairBlock.get(), new Item.Properties()));
		}
		
		if(variant.slab) {
			Latch<SlabBlock> slabBlock = latches.get(RegType.BLOCKS, new Id(StairdownBase.MODID, variant.idPrefix + "_slab"));
			ctx.reg(slabBlock, () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.OAK_SLAB)));
			
			Latch<BlockItem> slabItem = latches.get(RegType.ITEMS, slabBlock.id);
			ctx.reg(slabItem, () -> new BlockItem(slabBlock.get(), new Item.Properties()));
		}
		
		if(variant.wall) {
			Latch<SlabBlock> wallBlock = latches.get(RegType.BLOCKS, new Id(StairdownBase.MODID, variant.idPrefix + "_wall"));
			ctx.reg(wallBlock, () -> new WallBlock(BlockBehaviour.Properties.copy(Blocks.OAK_SLAB)));
			
			Latch<BlockItem> wallItem = latches.get(RegType.ITEMS, wallBlock.id);
			ctx.reg(wallItem, () -> new BlockItem(wallBlock.get(), new Item.Properties()));
		}
	}
}

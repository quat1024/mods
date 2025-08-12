package agency.highlysuspect.quatlib.craftless.facet.gens;

import agency.highlysuspect.quatlib.craftless.QuatlibBase;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

public abstract class BlockItemGen<B extends Block, I extends BlockItem> extends ItemGen<I> {
	public BlockItemGen(BlockGen<B> block) {
		super(block.blockLatch.id);
		this.block = block;
	}
	
	protected final BlockGen<B> block;
	
	//TODO more stuff (default models, etc)
	
	public static class Basic<B extends Block> extends BlockItemGen<B, BlockItem> {
		public Basic(BlockGen<B> block) {
			super(block);
		}
		
		@Override
		public BlockItem constructItem() {
			return QuatlibBase.inst().basicBlockItem(block.blockLatch.get());
		}
	}
}

package agency.highlysuspect.packages.craftless.content;

import agency.highlysuspect.quatlib.craftless.facet.Gen;
import agency.highlysuspect.quatlib.craftless.facet.Latch;
import agency.highlysuspect.quatlib.craftless.facet.facets.BlockEntityTypeFacet;
import agency.highlysuspect.quatlib.craftless.facet.facets.RegFacet;
import agency.highlysuspect.quatlib.craftless.util.BlockEntityFactory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Consumer;

public abstract class PackageGenBase implements Gen, PackagesGenUtils {
	public PackageGenBase(Latch<? extends Block> blockLatch, Latch<? extends Item> itemLatch, Latch<? extends BlockEntityType<?>> blockEntityTypeLatch) {
		this.blockLatch = blockLatch;
		this.itemLatch = itemLatch;
		this.blockEntityTypeLatch = blockEntityTypeLatch;
	}
	
	private final Latch<? extends Block> blockLatch;
	private final Latch<? extends Item> itemLatch;
	private final Latch<? extends BlockEntityType<?>> blockEntityTypeLatch;
	
	@SuppressWarnings("unchecked")
	@Override
	public void gen(Ctx ctx, Consumer<Gen> more) {
		if(ctx.isDatagen()) {
			ctx.add(enUs().block(blockLatch).value("Package"));
		}
		
		if(ctx.isRuntime()) {
			ctx.add(new RegFacet<>().latch(blockLatch).sup(this::constructBlock));
			ctx.add(new RegFacet<>().latch(itemLatch).sup(this::constructItem));
			
			ctx.add(new BlockEntityTypeFacet().latch(blockEntityTypeLatch).factory(blockEntityFactory()).addBlocks(blockLatch));
		}
	}
	
	protected abstract Block constructBlock();
	protected abstract Item constructItem();
	protected abstract BlockEntityFactory<?> blockEntityFactory();
}

package agency.highlysuspect.packages.craftless.content;

import agency.highlysuspect.quatlib.craftless.facet.Gen;
import agency.highlysuspect.quatlib.craftless.facet.Latch;
import agency.highlysuspect.quatlib.craftless.util.BlockEntityFactory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Consumer;

public abstract class PackageMakerGenBase implements Gen, PackagesGenUtils {
	public PackageMakerGenBase(Latch<? extends Block> blockLatch, Latch<? extends Item> itemLatch, Latch<? extends BlockEntityType<?>> blockEntityTypeLatch) {
		this.blockLatch = blockLatch;
		this.itemLatch = itemLatch;
		this.blockEntityTypeLatch = blockEntityTypeLatch;
	}
	
	private final Latch<? extends Block> blockLatch;
	private final Latch<? extends Item> itemLatch;
	private final Latch<? extends BlockEntityType<?>> blockEntityTypeLatch;
	
	@Override
	public void gen(Ctx ctx, Consumer<Gen> more) {
		if(ctx.isDatagen()) {
			ctx.add(enUs().block(blockLatch).value("Package Crafter"));
		}
		
		if(ctx.isRuntime()) {
			ctx.reg(blockLatch, this::constructBlock);
			ctx.reg(itemLatch, this::constructItem);
			ctx.blockEntity(blockEntityTypeLatch).factory(blockEntityFactory()).addBlocks(blockLatch);
		}
	}
	
	protected abstract Block constructBlock();
	protected abstract Item constructItem();
	protected abstract BlockEntityFactory<?> blockEntityFactory();
}

package agency.highlysuspect.packages.craftless.content;

import agency.highlysuspect.quatlib.craftless.facet.Gen;
import agency.highlysuspect.quatlib.craftless.facet.Latch;
import agency.highlysuspect.quatlib.craftless.facet.facets.RegFacet;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

public abstract class PackageMakerGenBase implements Gen, PackagesGenUtils {
	public PackageMakerGenBase(Latch<? extends Block> blockLatch) {
		this.blockLatch = blockLatch;
	}
	
	private final Latch<? extends Block> blockLatch;
	
	@Override
	public void gen(Ctx ctx, Consumer<Gen> more) {
		if(ctx.isDatagen()) {
			ctx.add(enUs().block(blockLatch).value("Package Crafter"));
		}
		
		if(ctx.isRuntime()) {
			ctx.add(new RegFacet<>().latch(blockLatch).sup(this::constructBlock));
		}
	}
	
	protected abstract Block constructBlock();
}

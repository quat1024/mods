package agency.highlysuspect.packages.craftless.content;

import agency.highlysuspect.packages.craftless.PackagesBase;
import agency.highlysuspect.quatlib.craftless.facet.Gen;
import agency.highlysuspect.quatlib.craftless.facet.Id;
import agency.highlysuspect.quatlib.craftless.facet.Latch;
import agency.highlysuspect.quatlib.craftless.facet.gens.BlockGen;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

public abstract class PackageGenBase<B extends Block> extends BlockGen<B> implements PackagesGenUtils {
	public PackageGenBase(Latch<B> blockLatch) {
		super(blockLatch);
	}
	
	public static final Id ID = new Id(PackagesBase.MODID, "package");
	
	@Override
	public void gen(Ctx ctx, Consumer<Gen> more) {
		super.gen(ctx, more);
		
		if(ctx.isDatagen()) {
			ctx.add(lang(EN_US).value("Package"));
		}
	}
}

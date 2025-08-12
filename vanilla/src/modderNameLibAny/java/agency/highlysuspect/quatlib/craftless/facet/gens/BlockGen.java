package agency.highlysuspect.quatlib.craftless.facet.gens;

import agency.highlysuspect.quatlib.craftless.facet.Gen;
import agency.highlysuspect.quatlib.craftless.facet.Id;
import agency.highlysuspect.quatlib.craftless.facet.Latch;
import agency.highlysuspect.quatlib.craftless.facet.RegType;
import agency.highlysuspect.quatlib.craftless.facet.facets.LangFacet;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public abstract class BlockGen<T extends Block> implements Gen {
	protected BlockGen(Latch<T> blockLatch) {
		this.blockLatch = blockLatch;
	}
	
	protected BlockGen(Id id) {
		this.blockLatch = Latch.open(RegType.BLOCKS, id);
	}
	
	public final Latch<T> blockLatch;
	
	@Override
	public void gen(Ctx ctx, Consumer<Gen> more) {
		ItemGen<?> itemForm = itemForm();
		if(itemForm != null) more.accept(itemForm);
	}
	
	public abstract T constructBlock();
	public @Nullable ItemGen<?> itemForm() {
		return new BlockItemGen.Basic<>(this);
	}
	
	protected LangFacet lang() {
		return new LangFacet().block(blockLatch.id);
	}
	
	protected LangFacet lang(Id langFile) {
		return lang().file(langFile);
	}
}

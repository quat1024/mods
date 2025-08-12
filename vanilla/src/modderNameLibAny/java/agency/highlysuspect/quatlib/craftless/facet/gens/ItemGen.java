package agency.highlysuspect.quatlib.craftless.facet.gens;

import agency.highlysuspect.quatlib.craftless.facet.Gen;
import agency.highlysuspect.quatlib.craftless.facet.Id;
import agency.highlysuspect.quatlib.craftless.facet.Latch;
import agency.highlysuspect.quatlib.craftless.facet.RegType;
import agency.highlysuspect.quatlib.craftless.facet.facets.LangFacet;
import net.minecraft.world.item.Item;

import java.util.function.Consumer;

public abstract class ItemGen<T extends Item> implements Gen {
	protected ItemGen(Latch<T> itemLatch) {
		this.itemLatch = itemLatch;
	}
	
	protected ItemGen(Id id) {
		this.itemLatch = Latch.open(RegType.ITEMS, id);
	}
	
	public final Latch<T> itemLatch;
	
	public abstract T constructItem();
	
	@Override
	public void gen(Ctx ctx, Consumer<Gen> more) {
		if(ctx.isDatagen()) {
			//TODO: constructItem
		}
	}
	
	// helpers //
	
	protected LangFacet lang() {
		return new LangFacet().item(itemLatch.id);
	}
	
	protected LangFacet lang(Id langFile) {
		return lang().file(langFile);
	}
}

package agency.highlysuspect.quatlib.craftless.facet.facets;

import agency.highlysuspect.quatlib.craftless.facet.FacetBucket;
import agency.highlysuspect.quatlib.craftless.facet.Latch;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.item.Item;

import java.util.Objects;

public class DispenserBehaviorFacetBuilder implements FacetBuilder {
	public DispenserBehaviorFacetBuilder(Latch<? extends Item> latch) {
		this.latch = Objects.requireNonNull(latch);
	}
	
	public DispenserBehaviorFacetBuilder(Latch<? extends Item> latch, DispenseItemBehavior behavior) {
		this(latch);
		this.behavior = behavior;
	}
	
	final Latch<? extends Item> latch;
	DispenseItemBehavior behavior;
	
	public DispenserBehaviorFacetBuilder behavior(DispenseItemBehavior behavior) {
		this.behavior = behavior;
		return this;
	}
	
	@Override
	public void build(FacetBuilder.BuildCtx ctx, FacetBucket facets) {
		facets.add(new DispenserBehaviorFacet(latch, behavior));
	}
}

package agency.highlysuspect.quatlib.craftless.facet.facets;

import agency.highlysuspect.quatlib.craftless.QuatlibBase;
import agency.highlysuspect.quatlib.craftless.facet.Facet;
import agency.highlysuspect.quatlib.craftless.facet.Latch;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.item.Item;

import java.util.List;

@Facet
public class DispenserBehaviorFacet {
	public Latch<? extends Item> latch;
	public DispenseItemBehavior behavior;
	
	public DispenserBehaviorFacet latch(Latch<? extends Item> latch) {
		this.latch = latch;
		return this;
	}
	
	public DispenserBehaviorFacet behavior(DispenseItemBehavior behavior) {
		this.behavior = behavior;
		return this;
	}
	
	public static void handle(List<DispenserBehaviorFacet> facets) {
		for(DispenserBehaviorFacet facet : facets) QuatlibBase.inst().registerDispenserBehavior(facet.latch, facet.behavior);
	}
}

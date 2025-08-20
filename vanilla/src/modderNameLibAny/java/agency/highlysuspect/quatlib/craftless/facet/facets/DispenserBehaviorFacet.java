package agency.highlysuspect.quatlib.craftless.facet.facets;

import agency.highlysuspect.quatlib.craftless.QuatlibBase;
import agency.highlysuspect.quatlib.craftless.facet.Latch;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.item.Item;

import java.util.List;

public record DispenserBehaviorFacet(Latch<? extends Item> latch, DispenseItemBehavior behavior) {
	public static void handle(List<DispenserBehaviorFacet> facets) {
		for(DispenserBehaviorFacet facet : facets) QuatlibBase.inst().registerDispenserBehavior(facet.latch, facet.behavior);
	}
}

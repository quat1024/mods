package agency.highlysuspect.packages.craftful.fab;

import agency.highlysuspect.quatlib.craftful.QuatlibMc;
import agency.highlysuspect.quatlib.craftless.facet.Latch;
import agency.highlysuspect.quatlib.craftless.facet.Reg;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;

import java.util.function.Supplier;

public class FabricReg<T> extends Reg<T> {
	@SuppressWarnings("unchecked")
	public FabricReg(ResourceKey<? extends Registry<T>> key) {
		this.registry = (Registry<T>) BuiltInRegistries.REGISTRY.get(transmute(key));
	}
	
	//Bro I Am Straight Up Not Having A Good Time
	@SuppressWarnings("unchecked")
	private final <A, B> ResourceKey<B> transmute(A a) {
		return (ResourceKey<B>) a;
	}
	
	protected final Registry<T> registry;
	
	@Override
	protected <X extends T> void doDefer(Latch<X> latch, Supplier<X> sup) {
		//Don't actually need to defer anything on fabric.
		X thing = sup.get();
		Registry.register(registry, QuatlibMc.inst().rlBridge.fromId(latch.id), thing);
		shut(latch.id, thing);
	}
}

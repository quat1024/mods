package agency.highlysuspect.packages.craftful.frg;

import agency.highlysuspect.quatlib.craftless.QuatlibBase;
import agency.highlysuspect.quatlib.craftless.facet.Id;
import agency.highlysuspect.quatlib.craftless.facet.Latch;
import agency.highlysuspect.quatlib.craftless.facet.Reg;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.RegisterEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ForgeReg<T> extends Reg<T> {
	public ForgeReg(ResourceKey<? extends Registry<T>> key) {
		this.key = key;
	}
	
	protected final ResourceKey<? extends Registry<T>> key;
	protected Map<Id, Supplier<? extends T>> thunks = new HashMap<>();
	
	@Override
	protected <X extends T> void doDefer(Latch<X> latch, Supplier<X> sup) {
		thunks.put(latch.id, sup);
	}
	
	@SubscribeEvent
	public void onForgeRegister(RegisterEvent e) {
		if(!e.getRegistryKey().equals(key)) return;
		
		if(thunks == null) throw new IllegalStateException("ForgeReg#onForgeRegister called twice for " + e.getRegistryKey());
		
		thunks.forEach((id, thunk) -> {
			//RegistryEvent.register forces the thunk for you but doesn't return its value >.>
			T unthunked = thunk.get();
			e.register(key, QuatlibBase.inst().rlBridge.fromId(id), () -> unthunked);
			shut(id, unthunked);
		});
		
		thunks = null; //all done
	}
}

package agency.highlysuspect.quatlib.craftless.facet;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Like Holder but not mojang's
 */
public class Latch<T> implements Supplier<@NotNull T>, Idable {
	//Not public; construct through LatchPool
	Latch(RegType<? super T> type, Id id, @Nullable T thing) {
		this.type = Objects.requireNonNull(type, "null type");
		this.id = Objects.requireNonNull(id, "null id");
		this.thing = thing;
	}
	
	public final RegType<? super T> type;
	public final Id id;
	private @Nullable T thing;
	
	@Override
	public @NotNull T get() {
		if(thing == null) throw new IllegalStateException("tried to read from open latch: " + this);
		return thing;
	}
	
	public void shut(T registered) {
		if(thing != null) throw new IllegalStateException("tried to shut latch twice: " + this);
		if(registered == null) throw new IllegalArgumentException("tried to shut latch with null thing: " + this);
		thing = registered;
	}
	
	//Intended to downcast a Latch<Object> or Latch<Block> (from version-independent code) into a Latch<MyCoolBlock>
	//Totally unchecked!
	@SuppressWarnings("unchecked")
	public <X extends T> Latch<X> downcast() {
		return (Latch<X>) this;
	}
	
	@Override
	public Id getId() {
		return id;
	}
	
	@Override
	public String toString() {
		return type + "->" + id + " " + (thing == null ? "(open)" : thing);
	}
}

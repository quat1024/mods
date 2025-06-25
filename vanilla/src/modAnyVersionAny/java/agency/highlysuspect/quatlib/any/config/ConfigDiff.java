package agency.highlysuspect.quatlib.any.config;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * useful for implementing a config GUI with a "discard changes" button
 */
public class ConfigDiff implements ReadableConfig, WritableConfig, WritableConfig.Handle {
	public ConfigDiff(WritableConfig base) {
		this.base = base;
	}
	
	private final WritableConfig base;
	private final Map<ConfigOpt<?>, Object> changes = new IdentityHashMap<>();
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(ConfigOpt<? extends T> opt) {
		T changed = (T) changes.get(opt);
		if(changed != null) return changed;
		else return base.get(opt);
	}
	
	@Override
	public void modify(Consumer<Handle> modifier) {
		modifier.accept(this);
	}
	
	@Override
	public <T> Handle set(ConfigOpt<? super T> opt, T value) {
		Object underlying = base.get((ConfigOpt<?>) opt);
		if(Objects.equals(value, underlying)) changes.remove(opt); //reverting to previous value
		else changes.put(opt, value); //new change
		
		return this;
	}
	
	public int changeCount() {
		return changes.size();
	}
	
	public boolean isChanged(ConfigOpt<?> opt) {
		return changes.containsKey(opt);
	}
	
	public void forEachChange(ChangeConsumer c) {
		changes.forEach(c::acceptErased);
	}
	
	//write all changes to the underlying writableconfig
	public void flushChanges() {
		base.modify(h -> forEachChange(h::set));
		changes.clear();
	}
	
	public interface ChangeConsumer {
		<T> void accept(ConfigOpt<T> opt, T value);
		
		@SuppressWarnings("unchecked")
		default <T> void acceptErased(ConfigOpt<?> opt, Object value) {
			accept((ConfigOpt<T>) opt, (T) value);
		}
	}
}

package agency.highlysuspect.quatlib.any.config.sn;

import agency.highlysuspect.quatlib.any.failure.ContextChain;
import agency.highlysuspect.quatlib.any.util.SnocList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Set;

public interface SnView {
	SnocList<String> path();
	
	boolean isList();
	ListView asList() throws SnException;
	@Nullable ListView asListOrNull();
	
	//TODO this should return an SnView.StringView onto the string or whatever
	// useful if i ever add mutators to this interface
	boolean isString();
	String asString() throws SnException;
	@Nullable String asStringOrNull();
	
	boolean isMap();
	MapView asMap() throws SnException;
	@Nullable MapView asMapOrNull();
	
	interface ListView extends SnView {
		int size();
		SnView get(int i) throws SnException;
	}
	
	interface MapView extends SnView {
		int size();
		Set<String> keySet();
		boolean containsKey(String key);
		SnView get(String key) throws SnException;
		@Nullable SnView getOrNull(String key);
	}
	
	//TODO: remove the SnocList and make SnViews just point at each other
	// saves an allocation
	class Impl implements SnView, ListView, MapView {
		public Impl(@NotNull Sn<?> sn, @NotNull ContextChain ctx) {
			this.sn = Objects.requireNonNull(sn);
			this.ctx = Objects.requireNonNull(ctx);
		}
		
		private final @NotNull Sn<?> sn;
		private final @NotNull ContextChain ctx;
		
		@Override
		@Deprecated(forRemoval = true)
		public @NotNull SnocList<String> path() {
			throw new IllegalStateException("deleting this method");
		}
		
		@Override
		public boolean isList() {
			return sn instanceof SnList;
		}
		
		@Override
		public ListView asList() throws SnException {
			if(sn instanceof SnList) return this;
			else throw SnException.expected(SnList.class, sn, ctx);
		}
		
		@Override
		public @Nullable ListView asListOrNull() {
			return sn instanceof SnList ? this : null;
		}
		
		@Override
		public boolean isString() {
			return sn instanceof SnStr;
		}
		
		@Override
		public String asString() throws SnException {
			if(sn instanceof SnStr(String value)) return value;
			else throw SnException.expected(SnStr.class, sn, ctx);
		}
		
		@Override
		public @Nullable String asStringOrNull() {
			return sn instanceof SnStr(String value) ? value : null;
		}
		
		@Override
		public boolean isMap() {
			return sn instanceof SnMap;
		}
		
		@Override
		public MapView asMap() throws SnException {
			if(sn instanceof SnMap) return this;
			else throw SnException.expected(SnMap.class, sn, ctx);
		}
		
		@Override
		public @Nullable MapView asMapOrNull() {
			return sn instanceof SnMap ? this : null;
		}
		
		private RuntimeException impossible() {
			return new IllegalStateException(String.join(", ", ctx.collectMessages()));
		}
		
		//implements two interfaces at once
		@Override
		public int size() {
			if(sn instanceof SnList arr) return arr.size();
			else if(sn instanceof SnMap map) return map.size();
			else throw impossible();
		}
		
		@Override
		public SnView get(int i) throws SnException {
			if(!(sn instanceof SnList arr)) throw impossible();
			if(i < 0 || i >= arr.size()) throw new SnException("Index " + i + " out of bounds for range " + arr.size(), ctx);
			return new Impl(arr.get(i), ctx.detail("[" + i + "]"));
		}
		
		@Override
		public Set<String> keySet() {
			if(!(sn instanceof SnMap map)) throw impossible();
			return map.keySet();
		}
		
		@Override
		public boolean containsKey(String key) {
			if(!(sn instanceof SnMap map)) throw impossible();
			return map.containsKey(key);
		}
		
		@Override
		public SnView get(String key) throws SnException {
			if(!(sn instanceof SnMap map)) throw impossible();
			Sn<?> child = map.get(key);
			if(child == null) throw new SnException("No such key '" + key, ctx);
			return new Impl(child, ctx.detail(key));
		}
		
		@Override
		public @Nullable SnView getOrNull(String key) {
			if(!(sn instanceof SnMap map)) throw impossible();
			Sn<?> child = map.get(key);
			if(child == null) return null;
			else return new Impl(child, ctx.detail(key));
		}
		
		@Override
		public String toString() {
			return "SnView, path '" + path() + "' (looking at " + sn.getClass().getSimpleName() + ")";
		}
	}
}

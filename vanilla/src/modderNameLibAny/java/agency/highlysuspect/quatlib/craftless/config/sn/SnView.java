package agency.highlysuspect.quatlib.craftless.config.sn;

import agency.highlysuspect.quatlib.craftless.failure.CtxChain;
import agency.highlysuspect.quatlib.craftless.failure.ReportedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public interface SnView {
	CtxChain ctx();
	
	boolean isList();
	ListView asList() throws ReportedException;
	@Nullable ListView asListOrNull();
	
	//TODO this should return an SnView.StringView onto the string or whatever
	// useful if i ever add mutators to this interface
	boolean isString();
	String asString() throws ReportedException;
	@Nullable String asStringOrNull();
	
	boolean isMap();
	MapView asMap() throws ReportedException;
	@Nullable MapView asMapOrNull();
	
	interface ListView extends SnView {
		int size();
		SnView get(int i) throws ReportedException;
	}
	
	interface MapView extends SnView {
		int size();
		Set<String> keySet();
		boolean containsKey(String key);
		SnView get(String key) throws ReportedException;
		@Nullable SnView getOrNull(String key);
		
		default Optional<SnView> getOrEmpty(String key) {
			return Optional.ofNullable(getOrNull(key));
		}
	}
	
	default Optional<ListView> asListOpt() {
		return Optional.ofNullable(asListOrNull());
	}
	
	default Optional<String> asStringOpt() {
		return Optional.ofNullable(asStringOrNull());
	}
	
	default Optional<MapView> asMapOpt() {
		return Optional.ofNullable(asMapOrNull());
	}
	
	//TODO: can this *extend* ContextChain? lol. save an allocation
	class Impl implements SnView, ListView, MapView {
		public Impl(@NotNull Sn<?> sn, @NotNull CtxChain ctx) {
			this.sn = Objects.requireNonNull(sn);
			this.ctx = Objects.requireNonNull(ctx);
		}
		
		private final @NotNull Sn<?> sn;
		private final @NotNull CtxChain ctx;
		
		private ReportedException expected(Class<? extends Sn<?>> expected) {
			String expectedName = name(expected);
			String gotName = name(sn);
			return ctx.detail("Expected " + expectedName + ", but there was " + gotName).reportError();
		}
		
		private static String name(Class<?> it) {
			if(it == null) return "nothing";
			if(it == SnList.class) return "a list";
			if(it == SnStr.class) return "a string";
			if(it == SnMap.class) return "a map";
			return it.getSimpleName(); //unreachable
		}
		
		private static String name(Sn<?> s) {
			return s == null ? "null?" : name(s.getClass());
		}
		
		@Override
		public CtxChain ctx() {
			return ctx;
		}
		
		@Override
		public String toString() {
			return "SnView onto " + name(sn) + " at " + ctx;
		}
		
		@Override
		public boolean isList() {
			return sn instanceof SnList;
		}
		
		@Override
		public ListView asList() throws ReportedException {
			if(sn instanceof SnList) return this;
			else throw expected(SnList.class);
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
		public String asString() throws ReportedException {
			if(sn instanceof SnStr str) return str.value();
			else throw expected(SnStr.class);
		}
		
		@Override
		public @Nullable String asStringOrNull() {
			return sn instanceof SnStr str ? str.value() : null;
		}
		
		@Override
		public boolean isMap() {
			return sn instanceof SnMap;
		}
		
		@Override
		public MapView asMap() throws ReportedException {
			if(sn instanceof SnMap) return this;
			else throw expected(SnMap.class);
		}
		
		@Override
		public @Nullable MapView asMapOrNull() {
			return sn instanceof SnMap ? this : null;
		}
		
		private RuntimeException impossible() {
			return new IllegalStateException("Unreachable?! at " + this);
		}
		
		//implements two interfaces at once
		@Override
		public int size() {
			if(sn instanceof SnList arr) return arr.size();
			else if(sn instanceof SnMap map) return map.size();
			else throw impossible();
		}
		
		@Override
		public SnView get(int i) throws ReportedException {
			if(!(sn instanceof SnList arr)) throw impossible();
			if(i < 0 || i >= arr.size()) throw ctx.detail("Index " + i + " out of bounds for range " + arr.size()).reportError();
			return new Impl(arr.get(i), ctx.path("[" + i + "]"));
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
		public SnView get(String key) throws ReportedException {
			if(!(sn instanceof SnMap map)) throw impossible();
			Sn<?> child = map.get(key);
			if(child == null) throw ctx.detail("No such key '" + key + "'").reportError();
			return new Impl(child, ctx.path(key));
		}
		
		@Override
		public @Nullable SnView getOrNull(String key) {
			if(!(sn instanceof SnMap map)) throw impossible();
			Sn<?> child = map.get(key);
			if(child == null) return null;
			else return new Impl(child, ctx.path(key));
		}
	}
}

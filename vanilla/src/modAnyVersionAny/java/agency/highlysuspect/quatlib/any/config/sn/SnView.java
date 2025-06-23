package agency.highlysuspect.quatlib.any.config.sn;

import agency.highlysuspect.quatlib.any.util.SnocList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;

public interface SnView {
	SnocList<String> path();
	
	boolean isList();
	ListView asList() throws SnException;
	
	boolean isString();
	String asString() throws SnException;
	
	boolean isMap();
	MapView asMap() throws SnException;
	
	interface ListView extends SnView {
		int size();
		SnView get(int i) throws SnException;
	}
	
	interface MapView extends SnView {
		int size();
		Set<String> keySet();
		boolean containsKey(String key);
		SnView get(String key) throws SnException;
	}
	
	class Impl implements SnView, ListView, MapView {
		public Impl(@NotNull Sn<?> sn, @NotNull SnocList<String> path) {
			this.sn = Objects.requireNonNull(sn);
			this.path = Objects.requireNonNull(path);
		}
		
		public Impl(Sn<?> sn) {
			this(sn, SnocList.empty());
		}
		
		private final @NotNull Sn<?> sn;
		private final @NotNull SnocList<String> path;
		
		@Override
		public @NotNull SnocList<String> path() {
			return path;
		}
		
		@Override
		public boolean isList() {
			return sn instanceof SnList;
		}
		
		@Override
		public ListView asList() throws SnException {
			if(sn instanceof SnList) return this;
			else throw SnException.expected(SnList.class, sn, path);
		}
		
		@Override
		public boolean isString() {
			return sn instanceof SnStr;
		}
		
		@Override
		public String asString() throws SnException {
			if(sn instanceof SnStr(String value)) return value;
			else throw SnException.expected(SnStr.class, sn, path);
		}
		
		@Override
		public boolean isMap() {
			return sn instanceof SnMap;
		}
		
		@Override
		public MapView asMap() throws SnException {
			if(sn instanceof SnMap) return this;
			else throw SnException.expected(SnMap.class, sn, path);
		}
		
		private RuntimeException impossible() {
			return new IllegalStateException(path.toString());
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
			if(i < 0 || i >= arr.size()) throw new SnException("Index " + i + " out of bounds for range " + arr.size() + " at ", path);
			return new Impl(arr.get(i), path.snoc("[" + i + "]"));
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
			if(child == null) throw new SnException("No such key '" + key + "' at ", path);
			return new Impl(child, path.snoc(key));
		}
		
		@Override
		public String toString() {
			return "SnView, path '" + path() + "' (looking at " + sn.getClass().getSimpleName() + ")";
		}
	}
}

package agency.highlysuspect.quatlib.any.config.sn;

import agency.highlysuspect.quatlib.any.util.SnocList;

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
		SnView get(int i);
	}
	
	interface MapView extends SnView {
		int size();
		Set<String> keySet();
		boolean containsKey(String key);
		SnView get(String key);
	}
	
	class Impl implements SnView, ListView, MapView {
		public Impl(Sn<?> sn, SnocList<String> path) {
			this.sn = sn;
			this.path = path;
		}
		
		public Impl(Sn<?> sn) {
			this(sn, SnocList.empty());
		}
		
		private final Sn<?> sn;
		private final SnocList<String> path;
		
		@Override
		public SnocList<String> path() {
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
			if(sn instanceof SnStr prim) return prim.toString();
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
		public SnView get(int i) {
			if(!(sn instanceof SnList arr)) throw impossible();
			return new Impl(arr.get(i), path.snoc(String.valueOf(i)));
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
		public SnView get(String key) {
			if(!(sn instanceof SnMap map)) throw impossible();
			return new Impl(map.get(key), path.snoc(key));
		}
	}
}

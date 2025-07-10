package agency.highlysuspect.quatlib.any.config.sn;

import agency.highlysuspect.quatlib.any.failure.ContextChain;

/**
 * "String Notation", for lack of a better term.
 * It's JSON but the only things left are strings and collections.
 */
public sealed interface Sn<S extends Sn<S>> permits SnStr, SnList, SnMap {
	S copy();
	
	default SnView view(ContextChain ctx) {
		return new SnView.Impl(this, ctx);
	}
	
	static SnStr str(String s) {
		return SnStr.of(s);
	}
	
	static SnList list(Object... contents) {
		if(contents.length == 0) return new SnList();
		
		SnList arr = new SnList();
		for(Object obj : contents) {
			if(obj instanceof String s) arr.add(SnStr.of(s));
			else if(obj instanceof Sn<?> s) arr.add(s);
			else throw new IllegalArgumentException(obj.getClass().getSimpleName());
		}
		return arr;
	}
	
	static SnMap map(Object... contents) {
		if(contents.length == 0) return new SnMap();
		if(contents.length % 2 != 0) throw new IllegalArgumentException("odd number of arguments to SnDsl.obj");
		
		SnMap obj = new SnMap();
		for(int i = 0; i < contents.length; i += 2) {
			String key = (String) contents[i];
			Object value = contents[i + 1];
			
			if(value instanceof String s) obj.put(key, SnStr.of(s));
			else if(value instanceof Sn<?> s) obj.put(key, s);
			else throw new IllegalArgumentException(obj.getClass().getSimpleName());
		}
		return obj;
	}
}

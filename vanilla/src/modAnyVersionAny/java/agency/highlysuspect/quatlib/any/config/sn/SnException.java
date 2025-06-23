package agency.highlysuspect.quatlib.any.config.sn;

import agency.highlysuspect.quatlib.any.config.ConfigException;
import agency.highlysuspect.quatlib.any.util.SnocList;

public class SnException extends ConfigException {
	//TODO: temp
	public SnException(String message, SnocList<String> path) {
		super(message + "'" + path.toString() + "'");
	}
	
	public static SnException expected(Class<? extends Sn<?>> expected, Sn<?> got, SnocList<String> path) {
		return new SnException("Expected " + name(expected) + ", but there was " + name(got.getClass()) + " at ", path);
	}
	
	private static String name(Class<?> it) {
		if(it == SnList.class) return "a list";
		if(it == SnStr.class) return "a string";
		if(it == SnMap.class) return "a map";
		return it.getSimpleName(); //unreachable
	}
}

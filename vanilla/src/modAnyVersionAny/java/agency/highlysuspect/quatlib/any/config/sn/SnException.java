package agency.highlysuspect.quatlib.any.config.sn;

import agency.highlysuspect.quatlib.any.config.ConfigException;
import agency.highlysuspect.quatlib.any.util.SnocList;

public class SnException extends ConfigException {
	//TODO: temp
	public SnException(String message, SnocList<String> path) {
		super(message + "'" + path.toString() + "'");
	}
	
	public static SnException expected(Class<? extends Sn<?>> expected, Sn<?> got, SnocList<String> path) {
		String expectedName = name(expected);
		String gotName = got == null ? name(null) : name(got.getClass());
		
		return new SnException("Expected " + expectedName + ", but there was " + gotName + " at ", path);
	}
	
	private static String name(Class<?> it) {
		if(it == null) return "nothing";
		if(it == SnList.class) return "a list";
		if(it == SnStr.class) return "a string";
		if(it == SnMap.class) return "a map";
		return it.getSimpleName(); //unreachable
	}
}

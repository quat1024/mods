package agency.highlysuspect.quatlib.any.config.sn;

import agency.highlysuspect.quatlib.any.config.failure.Report;
import agency.highlysuspect.quatlib.any.util.SnocList;

//TODO: this could probably be removed and folded into the Report mechanism now
public class SnException extends Report {
	public SnException(String message, SnocList<String> path) {
		super(message + "'" + path + "'");
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

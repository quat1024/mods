package agency.highlysuspect.quatlib.any.config.sn;

import agency.highlysuspect.quatlib.any.failure.ContextChain;
import agency.highlysuspect.quatlib.any.failure.Report2;

//TODO: this could probably be removed and folded into the Report2 mechanism now
public class SnException extends Report2 {
	public SnException(String message, ContextChain ctx) {
		super(ctx.detail(message), null);
	}
	
	public static SnException expected(Class<? extends Sn<?>> expected, Sn<?> got, ContextChain ctx) {
		String expectedName = name(expected);
		String gotName = got == null ? name(null) : name(got.getClass());
		
		return new SnException("Expected " + expectedName + ", but there was " + gotName, ctx);
	}
	
	private static String name(Class<?> it) {
		if(it == null) return "nothing";
		if(it == SnList.class) return "a list";
		if(it == SnStr.class) return "a string";
		if(it == SnMap.class) return "a map";
		return it.getSimpleName(); //unreachable
	}
}

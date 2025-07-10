package agency.highlysuspect.quatlib.craftless.bridge;

import java.util.function.UnaryOperator;

public interface Id<RESOURCELOCATION> {
	RESOURCELOCATION id(String namespace, String path);
	
	String ns(RESOURCELOCATION rl);
	String path(RESOURCELOCATION rl);
	
	default RESOURCELOCATION mapPath(RESOURCELOCATION in, UnaryOperator<String> op) {
		return id(ns(in), op.apply(path(in)));
	}
}

package agency.highlysuspect.quatlib.craftless.bridge;

import agency.highlysuspect.quatlib.craftless.facet.Id;

import java.util.function.UnaryOperator;

/**
 * trying a different approach
 */
public interface ResourceLocationBridge<RL> {
	RL make(String ns, String path);
	
	String ns(RL rl);
	String path(RL rl);
	
	default RL mapPath(RL rl, UnaryOperator<String> op) {
		return make(ns(rl), op.apply(path(rl)));
	}
	
	default RL fromId(Id id) {
		return make(id.ns(), id.path());
	}
	
	default Id toId(RL rl) {
		return new Id(ns(rl), path(rl));
	}
}

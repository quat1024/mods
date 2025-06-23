package agency.highlysuspect.quatlib.any.config.sn;

import java.util.IdentityHashMap;
import java.util.List;

//TODO: probably the wrong idea
public class ConcreteInfo {
	IdentityHashMap<Sn<?>, List<String>> prefixComments = new IdentityHashMap<>();
	
	public List<String> getComment(Sn<?> node) {
		List<String> comments = prefixComments.get(node);
		return comments == null ? List.of() : comments;
	}
	
	public void assignComment(Sn<?> node, List<String> comments) {
		prefixComments.put(node, comments);
	}
}

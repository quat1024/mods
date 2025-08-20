package agency.highlysuspect.quatlib.craftless.facet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class FacetBucket {
	private final Map<Class<?>, List<Object>> facets = new HashMap<>();
	
	public <T> T add(T facet) {
		facets.computeIfAbsent(facet.getClass(), __ -> new ArrayList<>(4)).add(facet);
		return facet;
	}
	
	// handling facets
	
	public <T> List<T> getFacets(Class<T> facetType) {
		List<T> f = (List<T>) facets.get(facetType);
		if(f == null) return List.of();
		else return f;
	}
	
	public int size() {
		int size = 0;
		for(List<Object> f : facets.values()) size += f.size();
		return size;
	}
}

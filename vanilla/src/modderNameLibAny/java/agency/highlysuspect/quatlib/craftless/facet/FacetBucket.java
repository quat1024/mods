package agency.highlysuspect.quatlib.craftless.facet;

import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Ideally all of the Facet metadata falls away to get garbage-collected.
 * Usages of this class should probalby stay within one method basically.
 */
@SuppressWarnings("unchecked")
public class FacetBucket {
	private final Map<Class<?>, List<Object>> facets = new HashMap<>();
	
	// "facet keys" = a class annotated with @Facet
	// the facet key for a given class, is the class in its superclass hierarchy with this annotation
	
	private static Class<?> getFacetKey(Class<?> clazz) {
		return Objects.requireNonNull(getFacetKeyOrNull(clazz), () -> "Not a facet: " + clazz.getName());
	}
	
	@Nullable
	private static Class<?> getFacetKeyOrNull(Class<?> clazz) {
		if(clazz == null) return null;
		else if(Record.class.isAssignableFrom(clazz)) return clazz;
		else if(clazz.isAnnotationPresent(Facet.class)) return clazz;
		else return getFacetKeyOrNull(clazz.getSuperclass());
	}
	
	private static Class<?> assertFacetKey(Class<?> clazz) {
		if(!Record.class.isAssignableFrom(clazz) && !clazz.isAnnotationPresent(Facet.class)) throw new IllegalArgumentException("Not a facet key: " + clazz.getName());
		return clazz;
	}
	
	// registering facets
	
	public <T> T add(T facet) {
		facets.computeIfAbsent(getFacetKey(facet.getClass()), __ -> new ArrayList<>(4)).add(facet);
		return facet;
	}
	
	// handling facets
	
	public <T> List<T> getFacets(Class<T> facetKey) {
		List<T> f = (List<T>) facets.get(assertFacetKey(facetKey));
		if(f == null) return List.of();
		else return f;
	}
	
	public int size() {
		int size = 0;
		for(List<Object> f : facets.values()) size += f.size();
		return size;
	}
}

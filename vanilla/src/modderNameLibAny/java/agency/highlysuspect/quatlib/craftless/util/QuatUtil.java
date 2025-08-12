package agency.highlysuspect.quatlib.craftless.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class QuatUtil {
	public static final Gson BASIC_PRETTY_GSON = new GsonBuilder()
		.disableHtmlEscaping()
		.setPrettyPrinting()
		.create();
	
	public static int clamp(int n, int min, int max) {
		if(n < min) return min;
		else return Math.min(n, max);
	}
	
	public static <K, T> Map<K, List<T>> collate(Iterable<T> things, Function<T, K> collator) {
		Map<K, List<T>> res = new HashMap<>();
		for(T thing : things) res.computeIfAbsent(collator.apply(thing), __ -> new ArrayList<>()).add(thing);
		return res;
	}
}

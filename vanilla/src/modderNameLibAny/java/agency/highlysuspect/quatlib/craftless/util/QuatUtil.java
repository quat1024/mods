package agency.highlysuspect.quatlib.craftless.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

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
	
	public static final Direction[] DIRECTIONS_AND_NULL = new Direction[]{
		Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, null
	};
	
	public static int clamp(int n, int min, int max) {
		if(n < min) return min;
		else return Math.min(n, max);
	}
	
	//blame processing.org for getting me addicted to this function
	public static float rangeRemap(float value, float low1, float high1, float low2, float high2) {
		float value2 = Mth.clamp(value, low1, high1);
		return low2 + (value2 - low1) * (high2 - low2) / (high1 - low1);
	}
	
	public static <K, T> Map<K, List<T>> collate(Iterable<T> things, Function<T, K> collator) {
		Map<K, List<T>> res = new HashMap<>();
		for(T thing : things) res.computeIfAbsent(collator.apply(thing), __ -> new ArrayList<>()).add(thing);
		return res;
	}
}

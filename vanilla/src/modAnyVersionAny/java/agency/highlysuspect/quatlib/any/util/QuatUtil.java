package agency.highlysuspect.quatlib.any.util;

public class QuatUtil {
	public static int clamp(int n, int min, int max) {
		if(n < min) return min;
		else return Math.min(n, max);
	}
}

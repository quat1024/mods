package agency.highlysuspect.modsetup;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class NothingToSeeHere {
	public static final Unsafe unsafe;
	
	static {
		try {
			Field uField = Unsafe.class.getDeclaredField("theUnsafe");
			uField.setAccessible(true);
			unsafe = (Unsafe) uField.get(null);
		} catch (Exception e) {
			throw new RuntimeException("Failed to unsafe", e);
		}
	}
	
	@SuppressWarnings("deprecation") //WORKS ON MY MACHINE
	public static void theFinalModifierIsAMereSuggestion(Object inst, Field field, Object newValue) {
		long off;
		if(inst == null) {
			inst = unsafe.staticFieldBase(field);
			off = unsafe.staticFieldOffset(field);
		} else {
			off = unsafe.objectFieldOffset(field);
		}
		unsafe.putObject(inst, off, newValue);
	}
}

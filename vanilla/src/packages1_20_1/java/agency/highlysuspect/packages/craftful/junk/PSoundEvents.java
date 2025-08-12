package agency.highlysuspect.packages.craftful.junk;

import agency.highlysuspect.packages.craftful.Packages;
import agency.highlysuspect.quatlib.craftless.facet.Latch;
import agency.highlysuspect.quatlib.craftless.facet.RegType;
import net.minecraft.sounds.SoundEvent;

public class PSoundEvents {
	public static Latch<SoundEvent> PACKAGE_MAKER_CRAFT = RegType.SOUND_EVENTS.latch(Packages.id("package_maker_craft"));
	public static Latch<SoundEvent> INSERT_ONE = RegType.SOUND_EVENTS.latch(Packages.id("insert_one"));
	public static Latch<SoundEvent> TAKE_ONE = RegType.SOUND_EVENTS.latch(Packages.id("take_one"));
	public static Latch<SoundEvent> INSERT_STACK = RegType.SOUND_EVENTS.latch(Packages.id("insert_stack"));
	public static Latch<SoundEvent> TAKE_STACK = RegType.SOUND_EVENTS.latch(Packages.id("take_stack"));
	public static Latch<SoundEvent> INSERT_ALL = RegType.SOUND_EVENTS.latch(Packages.id("insert_all"));
	public static Latch<SoundEvent> TAKE_ALL = RegType.SOUND_EVENTS.latch(Packages.id("take_all"));
}
